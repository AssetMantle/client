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

case class AssetMutate(from: String, fromID: String, assetID: String, mutableMetaProperties: Seq[BaseProperty], mutableProperties: Seq[BaseProperty], gas: MicroNumber, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends BaseTransaction[AssetMutate] with Logged {
  def mutateTicketID(newTicketID: String): AssetMutate = AssetMutate(from = from, fromID = fromID, assetID = assetID, mutableMetaProperties = mutableMetaProperties, mutableProperties = mutableProperties, gas = gas, status = status, txHash = txHash, ticketID = newTicketID, mode = mode, code = code)
}

@Singleton
class AssetMutates @Inject()(
                              transaction: utilities.Transaction,
                              protected val databaseConfigProvider: DatabaseConfigProvider,
                              utilitiesNotification: utilities.Notification,
                              masterAccounts: master.Accounts,
                              blockchainAccounts: blockchain.Accounts
                            )(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  case class AssetMutateSerialized(from: String, fromID: String, assetID: String, mutableMetaProperties: String, mutableProperties: String, gas: String, status: Option[Boolean], txHash: Option[String], ticketID: String, mode: String, code: Option[String], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: AssetMutate = AssetMutate(from = from, fromID = fromID, assetID = assetID, mutableMetaProperties = utilities.JSON.convertJsonStringToObject[Seq[BaseProperty]](mutableMetaProperties), mutableProperties = utilities.JSON.convertJsonStringToObject[Seq[BaseProperty]](mutableProperties), gas = new MicroNumber(BigInt(gas)), status = status, txHash = txHash, ticketID = ticketID, mode = mode, code = code, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedOn = updatedOn, updatedBy = updatedBy, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(assetMutate: AssetMutate): AssetMutateSerialized = AssetMutateSerialized(from = assetMutate.from, fromID = assetMutate.fromID, assetID = assetMutate.assetID, mutableMetaProperties = Json.toJson(assetMutate.mutableMetaProperties).toString, mutableProperties = Json.toJson(assetMutate.mutableProperties).toString, gas = assetMutate.gas.toMicroString, status = assetMutate.status, txHash = assetMutate.txHash, ticketID = assetMutate.ticketID, mode = assetMutate.mode, code = assetMutate.code, createdBy = assetMutate.createdBy, createdOn = assetMutate.createdOn, createdOnTimeZone = assetMutate.createdOnTimeZone, updatedBy = assetMutate.updatedBy, updatedOn = assetMutate.updatedOn, updatedOnTimeZone = assetMutate.updatedOnTimeZone)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  private implicit val logger: Logger = Logger(this.getClass)
  val db = databaseConfig.db

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_ASSET_MUTATE

  private val schedulerExecutionContext: ExecutionContext = actors.Service.actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val assetMutateTable = TableQuery[AssetMutateTable]

  private def add(assetMutate: AssetMutate): Future[String] = db.run((assetMutateTable returning assetMutateTable.map(_.ticketID) += serialize(assetMutate)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(assetMutateTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(assetMutateTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateStatusByTicketID(ticketID: String, status: Option[Boolean]): Future[Int] = db.run(assetMutateTable.filter(_.ticketID === ticketID).map(_.status.?).update(status).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByTicketID(ticketID: String): Future[AssetMutateSerialized] = db.run(assetMutateTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(assetMutateTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(assetMutateTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(assetMutateTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(assetMutateTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def getTransactionListByFromAddress(fromAddress: String): Future[Seq[AssetMutateSerialized]] = db.run(assetMutateTable.filter(_.from === fromAddress).result)

  private[models] class AssetMutateTable(tag: Tag) extends Table[AssetMutateSerialized](tag, "AssetMutate") {

    def * = (from, fromID, assetID, mutableMetaProperties, mutableProperties, gas, status.?, txHash.?, ticketID, mode, code.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (AssetMutateSerialized.tupled, AssetMutateSerialized.unapply)

    def from = column[String]("from")

    def fromID = column[String]("fromID")

    def assetID = column[String]("assetID")

    def mutableMetaProperties = column[String]("mutableMetaProperties")

    def mutableProperties = column[String]("mutableProperties")

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

    def create(assetMutate: AssetMutate): Future[String] = add(assetMutate)

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def resetTransactionStatus(ticketID: String): Future[Int] = updateStatusByTicketID(ticketID, status = null)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[AssetMutate] = findByTicketID(ticketID).map(_.deserialize)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

    def getTransactionList(fromAddress: String) = getTransactionListByFromAddress(fromAddress).map(_.map(_.deserialize))
  }

  object Utility {
    def onSuccess(ticketID: String, txHash: String): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, txHash)
      val assetMutate = Service.getTransaction(ticketID)

      def getAccountID(from: String) = blockchainAccounts.Service.tryGetUsername(from)

      def sendNotifications(accountID: String, assetID: String) = utilitiesNotification.send(accountID, constants.Notification.ASSET_MUTATED, assetID, txHash)(s"'$txHash'")

      (for {
        _ <- markTransactionSuccessful
        assetMutate <- assetMutate
        accountID <- getAccountID(assetMutate.from)
        _ <- sendNotifications(accountID = accountID, assetID = assetMutate.assetID)
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