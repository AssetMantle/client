package models.blockchainTransaction

import exceptions.BaseException
import models.Abstract.BaseTransaction
import models.Trait.Logged
import models.common.Serializable._
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class MaintainerDeputize(from: String, fromID: String, toID: String, classificationID: String, maintainedTraits: Seq[BaseProperty], addMaintainer: Boolean, removeMaintainer: Boolean, mutateMaintainer: Boolean, gas: MicroNumber, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends BaseTransaction[MaintainerDeputize] with Logged {
  def mutateTicketID(newTicketID: String): MaintainerDeputize = copy(ticketID = newTicketID)
}

@Singleton
class MaintainerDeputizes @Inject()(
                                     transaction: utilities.Transaction,
                                     protected val databaseConfigProvider: DatabaseConfigProvider,
                                     utilitiesNotification: utilities.Notification,
                                     masterAccounts: master.Accounts,
                                     blockchainAccounts: blockchain.Accounts,
                                     masterProperties: master.Properties
                                   )(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  case class MaintainerDeputizeSerialized(from: String, fromID: String, toID: String, classificationID: String, maintainedTraits: String, addMaintainer: Boolean, removeMaintainer: Boolean, mutateMaintainer: Boolean, gas: String, status: Option[Boolean], txHash: Option[String], ticketID: String, mode: String, code: Option[String], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: MaintainerDeputize = MaintainerDeputize(from = from, fromID = fromID, toID = toID, classificationID = classificationID, maintainedTraits = utilities.JSON.convertJsonStringToObject[Seq[BaseProperty]](maintainedTraits), addMaintainer = addMaintainer, removeMaintainer = removeMaintainer, mutateMaintainer = mutateMaintainer, gas = new MicroNumber(BigInt(gas)), status = status, txHash = txHash, ticketID = ticketID, mode = mode, code = code, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedOn = updatedOn, updatedBy = updatedBy, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(maintainerDeputize: MaintainerDeputize): MaintainerDeputizeSerialized = MaintainerDeputizeSerialized(from = maintainerDeputize.from, fromID = maintainerDeputize.fromID, toID = maintainerDeputize.toID, classificationID = maintainerDeputize.classificationID, maintainedTraits = Json.toJson(maintainerDeputize.maintainedTraits).toString, addMaintainer = maintainerDeputize.addMaintainer, removeMaintainer = maintainerDeputize.removeMaintainer, mutateMaintainer = maintainerDeputize.mutateMaintainer, gas = maintainerDeputize.gas.toMicroString, status = maintainerDeputize.status, txHash = maintainerDeputize.txHash, ticketID = maintainerDeputize.ticketID, mode = maintainerDeputize.mode, code = maintainerDeputize.code, createdBy = maintainerDeputize.createdBy, createdOn = maintainerDeputize.createdOn, createdOnTimeZone = maintainerDeputize.createdOnTimeZone, updatedBy = maintainerDeputize.updatedBy, updatedOn = maintainerDeputize.updatedOn, updatedOnTimeZone = maintainerDeputize.updatedOnTimeZone)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  private implicit val logger: Logger = Logger(this.getClass)
  val db = databaseConfig.db

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_MAINTAINER_DEPUTIZE

  private val schedulerExecutionContext: ExecutionContext = actors.Service.actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val maintainerDeputizeTable = TableQuery[MaintainerDeputizeTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").second

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(maintainerDeputize: MaintainerDeputize): Future[String] = db.run((maintainerDeputizeTable returning maintainerDeputizeTable.map(_.ticketID) += serialize(maintainerDeputize)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(maintainerDeputizeTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(maintainerDeputizeTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateStatusByTicketID(ticketID: String, status: Option[Boolean]): Future[Int] = db.run(maintainerDeputizeTable.filter(_.ticketID === ticketID).map(_.status.?).update(status).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByTicketID(ticketID: String): Future[MaintainerDeputizeSerialized] = db.run(maintainerDeputizeTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(maintainerDeputizeTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(maintainerDeputizeTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(maintainerDeputizeTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(maintainerDeputizeTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private[models] class MaintainerDeputizeTable(tag: Tag) extends Table[MaintainerDeputizeSerialized](tag, "MaintainerDeputize") {

    def * = (from, fromID, toID, classificationID, maintainedTraits, addMaintainer, removeMaintainer, mutateMaintainer, gas, status.?, txHash.?, ticketID, mode, code.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (MaintainerDeputizeSerialized.tupled, MaintainerDeputizeSerialized.unapply)

    def from = column[String]("from")

    def fromID = column[String]("fromID")

    def toID = column[String]("toID")

    def classificationID = column[String]("classificationID")

    def maintainedTraits = column[String]("maintainedTraits")

    def addMaintainer = column[Boolean]("addMaintainer")

    def removeMaintainer = column[Boolean]("removeMaintainer")

    def mutateMaintainer = column[Boolean]("mutateMaintainer")

    def gas = column[String]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def mode = column[String]("mode")

    def code = column[String]("code")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(maintainerDeputize: MaintainerDeputize): Future[String] = add(maintainerDeputize)

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def resetTransactionStatus(ticketID: String): Future[Int] = updateStatusByTicketID(ticketID, status = null)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[MaintainerDeputize] = findByTicketID(ticketID).map(_.deserialize)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

  }

  object Utility {
    def onSuccess(ticketID: String, txHash: String): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, txHash)
      val maintainerDeputize = Service.getTransaction(ticketID)

      def insertProperties(maintainerDeputize: MaintainerDeputize) = masterProperties.Utilities.upsertProperties(entityID = utilities.IDGenerator.getMaintainerID(classificationID = maintainerDeputize.classificationID, identityID = maintainerDeputize.toID), entityType = constants.Blockchain.Entity.MAINTAINER, immutableMetas = Seq.empty, immutables = Seq.empty, mutableMetas = Seq.empty, mutables = maintainerDeputize.maintainedTraits)

      def getAccountID(from: String) = blockchainAccounts.Service.tryGetUsername(from)

      def sendNotifications(accountID: String, classificationID: String) = utilitiesNotification.send(accountID, constants.Notification.MAINTAINER_DEPUTIZED, classificationID, txHash)(s"'$txHash'")

      (for {
        _ <- markTransactionSuccessful
        maintainerDeputize <- maintainerDeputize
        _ <- insertProperties(maintainerDeputize)
        accountID <- getAccountID(maintainerDeputize.from)
        _ <- sendNotifications(accountID = accountID, classificationID = maintainerDeputize.classificationID)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = {
      val markTransactionFailed = Service.markTransactionFailed(ticketID, message)

      (for {
        _ <- markTransactionFailed
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  private val txRunnable = new Runnable {
    def run(): Unit = Await.result(transaction.ticketUpdater(Service.getTicketIDsOnStatus, Service.getTransactionHash, Service.getMode, Utility.onSuccess, Utility.onFailure), Duration.Inf)
  }

  if (kafkaEnabled || transactionMode != constants.Transactions.BLOCK_MODE) {
    actors.Service.actorSystem.scheduler.scheduleWithFixedDelay(initialDelay = schedulerInitialDelay, delay = schedulerInterval)(txRunnable)(schedulerExecutionContext)
  }
}