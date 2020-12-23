package models.blockchainTransaction

import exceptions.BaseException
import models.Abstract.BaseTransaction
import models.Trait.Logged
import models.common.Serializable.{BaseProperty, Immutables, Mutables, Properties}
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class IdentityNub(from: String, nubID: String, gas: MicroNumber, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends BaseTransaction[IdentityNub] with Logged {
  def mutateTicketID(newTicketID: String): IdentityNub = IdentityNub(from = from, nubID = nubID, gas = gas, status = status, txHash = txHash, ticketID = newTicketID, mode = mode, code = code)
}

@Singleton
class IdentityNubs @Inject()(
                              transaction: utilities.Transaction,
                              protected val databaseConfigProvider: DatabaseConfigProvider,
                              utilitiesNotification: utilities.Notification,
                              masterAccounts: master.Accounts,
                              blockchainAccounts: blockchain.Accounts,
                              blockchainIdentities: blockchain.Identities,
                              masterProperties: master.Properties
                            )(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  case class IdentityNubSerialized(from: String, nubID: String, gas: String, status: Option[Boolean], txHash: Option[String], ticketID: String, mode: String, code: Option[String], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: IdentityNub = IdentityNub(from = from, nubID = nubID, gas = new MicroNumber(BigInt(gas)), status = status, txHash = txHash, ticketID = ticketID, mode = mode, code = code, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedOn = updatedOn, updatedBy = updatedBy, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(identityNub: IdentityNub): IdentityNubSerialized = IdentityNubSerialized(from = identityNub.from, nubID = identityNub.nubID, gas = identityNub.gas.toMicroString, status = identityNub.status, txHash = identityNub.txHash, ticketID = identityNub.ticketID, mode = identityNub.mode, code = identityNub.code, createdBy = identityNub.createdBy, createdOn = identityNub.createdOn, createdOnTimeZone = identityNub.createdOnTimeZone, updatedBy = identityNub.updatedBy, updatedOn = identityNub.updatedOn, updatedOnTimeZone = identityNub.updatedOnTimeZone)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  private implicit val logger: Logger = Logger(this.getClass)
  val db = databaseConfig.db

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_IDENTITY_PROVISION

  private val schedulerExecutionContext: ExecutionContext = actors.Service.actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val identityNubTable = TableQuery[IdentityNubTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").second

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(identityNub: IdentityNub): Future[String] = db.run((identityNubTable returning identityNubTable.map(_.ticketID) += serialize(identityNub)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(identityNubTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(identityNubTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusByTicketID(ticketID: String, status: Option[Boolean]): Future[Int] = db.run(identityNubTable.filter(_.ticketID === ticketID).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findByTicketID(ticketID: String): Future[IdentityNubSerialized] = db.run(identityNubTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(identityNubTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(identityNubTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(identityNubTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(identityNubTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class IdentityNubTable(tag: Tag) extends Table[IdentityNubSerialized](tag, "IdentityNub") {

    def * = (from, nubID, gas, status.?, txHash.?, ticketID, mode, code.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (IdentityNubSerialized.tupled, IdentityNubSerialized.unapply)

    def from = column[String]("from")

    def nubID = column[String]("nubID")

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

    def create(identityNub: IdentityNub): Future[String] = add(identityNub)

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def resetTransactionStatus(ticketID: String): Future[Int] = updateStatusByTicketID(ticketID, status = null)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[IdentityNub] = findByTicketID(ticketID).map(_.deserialize)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

  }

  object Utility {

    private val chainID = configuration.get[String]("blockchain.chainID")

    def onSuccess(ticketID: String, txHash: String): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, txHash)
      val identityNub = Service.getTransaction(ticketID)

      def getAccountID(from: String) = blockchainAccounts.Service.tryGetUsername(from)

      def insertClassificationProperties(identityNub: IdentityNub) = masterProperties.Utilities.upsertProperties(entityID = utilities.IDGenerator.getClassificationID(chainID = chainID, Immutables(Properties(Seq(blockchainIdentities.Utility.getNubMetaProperty(identityNub.nubID).removeData()))), Mutables(Properties(Seq.empty))),
        entityType = constants.Blockchain.Entity.IDENTITY_DEFINITION, immutableMetas = Seq.empty, immutables = Seq(BaseProperty(dataType = constants.Blockchain.DataType.ID_DATA, dataName = constants.Blockchain.Properties.NubID, dataValue = None)), mutableMetas = Seq.empty, mutables = Seq.empty)

      def insertIdentityProperties(identityNub: IdentityNub, classificationID: String) = masterProperties.Utilities.upsertProperties(entityID = utilities.IDGenerator.getIdentityID(classificationID = classificationID, Immutables(Properties(Seq(blockchainIdentities.Utility.getNubMetaProperty(identityNub.nubID).removeData())))),
        entityType = constants.Blockchain.Entity.IDENTITY, immutableMetas = Seq(BaseProperty(dataType = constants.Blockchain.DataType.ID_DATA, dataName = constants.Blockchain.Properties.NubID, dataValue = Some(identityNub.nubID))), immutables = Seq.empty, mutableMetas = Seq.empty, mutables = Seq.empty)

      def sendNotifications(accountID: String, identityID: String) = utilitiesNotification.send(accountID, constants.Notification.IDENTITY_NUB, identityID, txHash)(txHash)

      (for {
        _ <- markTransactionSuccessful
        identityNub <- identityNub
        classificationID <- insertClassificationProperties(identityNub)
        identityID <- insertIdentityProperties(identityNub, classificationID)
        accountID <- getAccountID(identityNub.from)
        _ <- sendNotifications(accountID = accountID, identityID = identityID)
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