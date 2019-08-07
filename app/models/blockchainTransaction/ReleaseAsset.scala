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
import transactions.responses.TransactionResponse.BlockResponse
import utilities.PushNotification

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ReleaseAsset(from: String, to: String, pegHash: String, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, mode: String, code: Option[String])

@Singleton
class ReleaseAssets @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, transactionReleaseAsset: transactions.ReleaseAsset, blockchainAssets: blockchain.Assets, blockchainAccounts: blockchain.Accounts, pushNotification: PushNotification, masterAccounts: master.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_RELEASE_ASSET

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val releaseAssetTable = TableQuery[ReleaseAssetTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private def add(releaseAsset: ReleaseAsset): Future[String] = db.run((releaseAssetTable returning releaseAssetTable.map(_.ticketID) += releaseAsset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(releaseAsset: ReleaseAsset): Future[Int] = db.run(releaseAssetTable.insertOrUpdate(releaseAsset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String): Future[ReleaseAsset] = db.run(releaseAssetTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(releaseAssetTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndResponseCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(releaseAssetTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(releaseAssetTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def deleteByTicketID(ticketID: String)(implicit executionContext: ExecutionContext) = db.run(releaseAssetTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class ReleaseAssetTable(tag: Tag) extends Table[ReleaseAsset](tag, "ReleaseAsset") {

    def * = (from, to, pegHash, gas, status.?, txHash.?, ticketID, mode, code.?) <> (ReleaseAsset.tupled, ReleaseAsset.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def pegHash = column[String]("pegHash")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def mode = column[String]("mode")

    def code = column[String]("code")
  }

  object Service {

    def create(releaseAsset: ReleaseAsset): String = Await.result(add(ReleaseAsset(from = releaseAsset.from, to = releaseAsset.to, pegHash = releaseAsset.pegHash, gas = releaseAsset.gas, status = releaseAsset.status, txHash = releaseAsset.txHash, ticketID = releaseAsset.ticketID, mode = releaseAsset.mode, code = releaseAsset.code)), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus, Duration.Inf)

    def markTransactionSuccessful(ticketID: String, txHash: String): Int = Await.result(updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true)), Duration.Inf)

    def markTransactionFailed(ticketID: String, code: String): Int = Await.result(updateStatusAndResponseCodeOnTicketID(ticketID, status = Option(false), code), Duration.Inf)

    def getTransaction(ticketID: String): ReleaseAsset = Await.result(findByTicketID(ticketID), Duration.Inf)

    def getTransactionHash(ticketID: String): Option[String] = Await.result(findByTicketID(ticketID), Duration.Inf).txHash
  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = Future {
      try {
        Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
        val releaseAsset = Service.getTransaction(ticketID)
        blockchainAssets.Service.markDirty(releaseAsset.pegHash)
        blockchainAccounts.Service.markDirty(masterAccounts.Service.getAddress(releaseAsset.from))
        pushNotification.sendNotification(masterAccounts.Service.getId(releaseAsset.to), constants.Notification.SUCCESS, blockResponse.txhash)
        pushNotification.sendNotification(releaseAsset.from, constants.Notification.SUCCESS, blockResponse.txhash)
      }
      catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.markTransactionFailed(ticketID, message)
        val releaseAsset = Service.getTransaction(ticketID)
        pushNotification.sendNotification(masterAccounts.Service.getId(releaseAsset.to), constants.Notification.FAILURE, message)
        pushNotification.sendNotification(releaseAsset.from, constants.Notification.FAILURE, message)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }


  if (kafkaEnabled) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      transaction.ticketUpdater(Service.getTicketIDsOnStatus, Service.getTransactionHash, Utility.onSuccess, Utility.onFailure)
    }
  }
}