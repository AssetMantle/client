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

case class IdentityDefine(from: String, fromID: String, immutableMetaTraits: Seq[BaseProperty], immutableTraits: Seq[BaseProperty], mutableMetaTraits: Seq[BaseProperty], mutableTraits: Seq[BaseProperty], gas: MicroNumber, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends BaseTransaction[IdentityDefine] with Logged {
  def mutateTicketID(newTicketID: String): IdentityDefine = IdentityDefine(from = from, fromID = fromID, immutableMetaTraits = immutableMetaTraits, immutableTraits = immutableTraits, mutableMetaTraits = mutableMetaTraits, mutableTraits = mutableTraits, gas = gas, status = status, txHash = txHash, ticketID = newTicketID, mode = mode, code = code)
}

@Singleton
class IdentityDefines @Inject()(
                                 transaction: utilities.Transaction,
                                 protected val databaseConfigProvider: DatabaseConfigProvider,
                                 utilitiesNotification: utilities.Notification,
                                 masterAccounts: master.Accounts,
                                 masterProperties: master.Properties,
                                 blockchainAccounts: blockchain.Accounts
                               )(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  case class IdentityDefineSerialized(from: String, fromID: String, immutableMetaTraits: String, immutableTraits: String, mutableMetaTraits: String, mutableTraits: String, gas: String, status: Option[Boolean], txHash: Option[String], ticketID: String, mode: String, code: Option[String], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: IdentityDefine = IdentityDefine(from = from, fromID = fromID, immutableMetaTraits = utilities.JSON.convertJsonStringToObject[Seq[BaseProperty]](immutableMetaTraits), immutableTraits = utilities.JSON.convertJsonStringToObject[Seq[BaseProperty]](immutableTraits), mutableMetaTraits = utilities.JSON.convertJsonStringToObject[Seq[BaseProperty]](mutableMetaTraits), mutableTraits = utilities.JSON.convertJsonStringToObject[Seq[BaseProperty]](mutableTraits), gas = new MicroNumber(BigInt(gas)), status = status, txHash = txHash, ticketID = ticketID, mode = mode, code = code, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedOn = updatedOn, updatedBy = updatedBy, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(identityDefine: IdentityDefine): IdentityDefineSerialized = IdentityDefineSerialized(from = identityDefine.from, fromID = identityDefine.fromID, immutableMetaTraits = Json.toJson(identityDefine.immutableMetaTraits).toString, immutableTraits = Json.toJson(identityDefine.immutableTraits).toString, mutableMetaTraits = Json.toJson(identityDefine.mutableMetaTraits).toString, mutableTraits = Json.toJson(identityDefine.mutableTraits).toString, gas = identityDefine.gas.toMicroString, status = identityDefine.status, txHash = identityDefine.txHash, ticketID = identityDefine.ticketID, mode = identityDefine.mode, code = identityDefine.code, createdBy = identityDefine.createdBy, createdOn = identityDefine.createdOn, createdOnTimeZone = identityDefine.createdOnTimeZone, updatedBy = identityDefine.updatedBy, updatedOn = identityDefine.updatedOn, updatedOnTimeZone = identityDefine.updatedOnTimeZone)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  private implicit val logger: Logger = Logger(this.getClass)
  val db = databaseConfig.db

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_IDENTITY_DEFINE

  private val schedulerExecutionContext: ExecutionContext = actors.Service.actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val identityDefineTable = TableQuery[IdentityDefineTable]

  private def add(identityDefine: IdentityDefine): Future[String] = db.run((identityDefineTable returning identityDefineTable.map(_.ticketID) += serialize(identityDefine)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(identityDefineTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(identityDefineTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateStatusByTicketID(ticketID: String, status: Option[Boolean]): Future[Int] = db.run(identityDefineTable.filter(_.ticketID === ticketID).map(_.status.?).update(status).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByTicketID(ticketID: String): Future[IdentityDefineSerialized] = db.run(identityDefineTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(identityDefineTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(identityDefineTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(identityDefineTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(identityDefineTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def getTransactionListByFromAddress(fromAddress: String): Future[Seq[IdentityDefineSerialized]] = db.run(identityDefineTable.filter(_.from === fromAddress).result)

  private[models] class IdentityDefineTable(tag: Tag) extends Table[IdentityDefineSerialized](tag, "IdentityDefine") {

    def * = (from, fromID, immutableMetaTraits, immutableTraits, mutableMetaTraits, mutableTraits, gas, status.?, txHash.?, ticketID, mode, code.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (IdentityDefineSerialized.tupled, IdentityDefineSerialized.unapply)

    def from = column[String]("from")

    def fromID = column[String]("fromID")

    def immutableMetaTraits = column[String]("immutableMetaTraits")

    def immutableTraits = column[String]("immutableTraits")

    def mutableMetaTraits = column[String]("mutableMetaTraits")

    def mutableTraits = column[String]("mutableTraits")

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

    def create(identityDefine: IdentityDefine): Future[String] = add(identityDefine)

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def resetTransactionStatus(ticketID: String): Future[Int] = updateStatusByTicketID(ticketID, status = null)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def tryGet(ticketID: String): Future[IdentityDefine] = findByTicketID(ticketID).map(_.deserialize)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

    def getTransactionList(fromAddress: String) = getTransactionListByFromAddress(fromAddress).map(_.map(_.deserialize))
  }

  object Utility {

    def onSuccess(ticketID: String, txHash: String): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, txHash)
      val identityDefine = Service.tryGet(ticketID)

      def getAccountID(from: String) = blockchainAccounts.Service.tryGetUsername(from)

      def insertProperties(identityDefine: IdentityDefine) = masterProperties.Utilities.upsertProperties(entityID = utilities.IDGenerator.getClassificationID(chainID = constants.Blockchain.ChainID, Immutables(Properties((identityDefine.immutableMetaTraits ++ identityDefine.immutableTraits).map(_.toProperty))), Mutables(Properties((identityDefine.mutableMetaTraits ++ identityDefine.mutableTraits).map(_.toProperty)))),
        entityType = constants.Blockchain.Entity.IDENTITY_DEFINITION, immutableMetas = identityDefine.immutableMetaTraits, immutables = identityDefine.immutableTraits, mutableMetas = identityDefine.mutableMetaTraits, mutables = identityDefine.mutableTraits)

      def insertMaintainerProperties(classificationID: String, identityDefine: IdentityDefine) = masterProperties.Utilities.upsertProperties(entityID = utilities.IDGenerator.getMaintainerID(classificationID = classificationID, identityID = identityDefine.fromID), entityType = constants.Blockchain.Entity.MAINTAINER, immutableMetas = Seq.empty, immutables = Seq.empty, mutableMetas = Seq.empty, mutables = identityDefine.mutableMetaTraits ++ identityDefine.mutableTraits)

      def sendNotifications(accountID: String, classificationID: String) = utilitiesNotification.send(accountID, constants.Notification.IDENTITY_DEFINED, classificationID, txHash)(s"'$txHash'")

      (for {
        _ <- markTransactionSuccessful
        identityDefine <- identityDefine
        classificationID <- insertProperties(identityDefine)
        maintainerID <- insertMaintainerProperties(classificationID, identityDefine)
        accountID <- getAccountID(identityDefine.from)
        _ <- sendNotifications(accountID = accountID, classificationID = classificationID)
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

  if ((constants.Blockchain.KafkaEnabled || constants.Blockchain.TransactionMode != constants.Transactions.BLOCK_MODE) && constants.Blockchain.EnableTxSchemaActor) {
    actors.Service.actorSystem.scheduler.scheduleWithFixedDelay(initialDelay = constants.Blockchain.KafkaTxIteratorInitialDelay, delay = constants.Blockchain.KafkaTxIteratorInterval)(txRunnable)(schedulerExecutionContext)
  }
}