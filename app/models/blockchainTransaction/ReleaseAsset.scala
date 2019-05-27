package models.blockchainTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.Response
import utilities.PushNotification

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ReleaseAsset(from: String, to: String, pegHash: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

@Singleton
class ReleaseAssets @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, transactionReleaseAsset: transactions.ReleaseAsset, blockchainAssets: blockchain.Assets, blockchainAccounts: blockchain.Accounts, actorSystem: ActorSystem, pushNotification: PushNotification, masterAccounts: master.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_RELEASE_ASSET

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val releaseAssetTable = TableQuery[ReleaseAssetTable]

  private def add(releaseAsset: ReleaseAsset)(implicit executionContext: ExecutionContext): Future[String] = db.run((releaseAssetTable returning releaseAssetTable.map(_.ticketID) += releaseAsset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def upsert(releaseAsset: ReleaseAsset)(implicit executionContext: ExecutionContext): Future[Int] = db.run(releaseAssetTable.insertOrUpdate(releaseAsset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }
  private def findByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[ReleaseAsset] = db.run(releaseAssetTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashStatusAndResponseCodeOnTicketID(ticketID: String, txHash: String, status: Option[Boolean], responseCode: String): Future[Int] = db.run(releaseAssetTable.filter(_.ticketID === ticketID).map(x => (x.txHash, x.status.?, x.responseCode)).update((txHash, status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndResponseOnTicketID(ticketID: String, status: Option[Boolean], responseCode: String): Future[Int] = db.run(releaseAssetTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.responseCode)).update((status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus()(implicit executionContext: ExecutionContext): Future[Seq[String]] = db.run(releaseAssetTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def deleteByTicketID(ticketID: String)(implicit executionContext: ExecutionContext) = db.run(releaseAssetTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class ReleaseAssetTable(tag: Tag) extends Table[ReleaseAsset](tag, "ReleaseAsset") {

    def * = (from, to, pegHash, gas, status.?, txHash.?, ticketID, responseCode.?) <> (ReleaseAsset.tupled, ReleaseAsset.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def pegHash = column[String]("pegHash")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }

  object Service {

    def create(from: String, to: String, pegHash: String, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])(implicit executionContext: ExecutionContext): String = Await.result(add(ReleaseAsset(from = from, to = to, pegHash = pegHash, gas = gas, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus(), Duration.Inf)

    def markTransactionSuccessful(ticketID: String, txHash: String, responseCode: String): Int = Await.result(updateTxHashStatusAndResponseCodeOnTicketID(ticketID, txHash, status = Option(true), responseCode), Duration.Inf)

    def markTransactionFailed(ticketID: String, responseCode: String): Int = Await.result(updateStatusAndResponseOnTicketID(ticketID, status = Option(false), responseCode), Duration.Inf)

    def getTransaction(ticketID: String)(implicit executionContext: ExecutionContext): ReleaseAsset = Await.result(findByTicketID(ticketID), Duration.Inf)

  }

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval =  configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  object Utility {
    def onSuccess(ticketID: String, response: Response): Future[Unit] = Future {
      try {
        Service.markTransactionSuccessful(ticketID, response.TxHash, response.Code)
        val releaseAsset = Service.getTransaction(ticketID)
        blockchainAssets.Service.markDirty(releaseAsset.pegHash)
        blockchainAccounts.Service.markDirty(masterAccounts.Service.getAddress(releaseAsset.from))
        pushNotification.sendNotification(masterAccounts.Service.getId(releaseAsset.to), constants.Notification.SUCCESS, response.TxHash)
        pushNotification.sendNotification(releaseAsset.from, constants.Notification.SUCCESS, response.TxHash)
      }
      catch {
        case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
          throw new BaseException(constants.Error.PSQL_EXCEPTION)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.markTransactionFailed(ticketID, message)
        val releaseAsset = Service.getTransaction(ticketID)
        pushNotification.sendNotification(masterAccounts.Service.getId(releaseAsset.to), constants.Notification.FAILURE, message)
        pushNotification.sendNotification(releaseAsset.from, constants.Notification.FAILURE, message)
      } catch {
        case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
      }
    }
  }


  if (kafkaEnabled) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      utilities.TicketUpdater.start(Service.getTicketIDsOnStatus, transactionReleaseAsset.Service.getTxFromWSResponse, Utility.onSuccess, Utility.onFailure)
    }
  }
}