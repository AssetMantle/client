package models.blockchainTransaction

import java.net.ConnectException

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.GetAccount
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.Response
import utilities.PushNotification

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class IssueAsset(from: String, to: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, unmoderated: Boolean, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

@Singleton
class IssueAssets @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, getAccount: GetAccount, blockchainAssets: blockchain.Assets, transactionIssueAsset: transactions.IssueAsset, actorSystem: ActorSystem, pushNotification: PushNotification, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_ISSUE_ASSET

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val issueAssetTable = TableQuery[IssueAssetTable]
  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private def add(issueAsset: IssueAsset)(implicit executionContext: ExecutionContext): Future[String] = db.run((issueAssetTable returning issueAssetTable.map(_.ticketID) += issueAsset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def upsert(issueAsset: IssueAsset)(implicit executionContext: ExecutionContext): Future[Int] = db.run(issueAssetTable.insertOrUpdate(issueAsset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[IssueAsset] = db.run(issueAssetTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndResponseOnTicketID(ticketID: String, status: Option[Boolean], responseCode: String): Future[Int] = db.run(issueAssetTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.responseCode)).update((status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus()(implicit executionContext: ExecutionContext): Future[Seq[String]] = db.run(issueAssetTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashStatusAndResponseCodeOnTicketID(ticketID: String, txHash: String, status: Option[Boolean], responseCode: String): Future[Int] = db.run(issueAssetTable.filter(_.ticketID === ticketID).map(x => (x.txHash, x.status.?, x.responseCode)).update((txHash, status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByTicketID(ticketID: String)(implicit executionContext: ExecutionContext) = db.run(issueAssetTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class IssueAssetTable(tag: Tag) extends Table[IssueAsset](tag, "IssueAsset") {

    def * = (from, to, documentHash, assetType, assetPrice, quantityUnit, assetQuantity, unmoderated, gas, status.?, txHash.?, ticketID, responseCode.?) <> (IssueAsset.tupled, IssueAsset.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def documentHash = column[String]("documentHash")

    def assetType = column[String]("assetType")

    def assetPrice = column[Int]("assetPrice")

    def quantityUnit = column[String]("quantityUnit")

    def assetQuantity = column[Int]("assetQuantity")

    def unmoderated = column[Boolean]("unmoderated")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }

  object Service {

    def create(from: String, to: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, unmoderated: Boolean, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])(implicit executionContext: ExecutionContext): String = Await.result(add(IssueAsset(from = from, to = to, documentHash = documentHash, assetType = assetType, assetPrice = assetPrice, quantityUnit = quantityUnit, assetQuantity = assetQuantity, gas = gas, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode, unmoderated = unmoderated)), Duration.Inf)

    def markTransactionSuccessful(ticketID: String, txHash: String, responseCode: String): Int = Await.result(updateTxHashStatusAndResponseCodeOnTicketID(ticketID, txHash, status = Option(true), responseCode), Duration.Inf)

    def markTransactionFailed(ticketID: String, responseCode: String): Int = Await.result(updateStatusAndResponseOnTicketID(ticketID, status = Option(false), responseCode), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus(), Duration.Inf)

    def getTransaction(ticketID: String)(implicit executionContext: ExecutionContext): IssueAsset = Await.result(findByTicketID(ticketID), Duration.Inf)

  }

  object Utility {
    def onSuccess(ticketID: String, response: Response): Future[Unit] = Future {
      try {
        Service.markTransactionSuccessful(ticketID, response.TxHash, response.Code)
        val issueAsset = Service.getTransaction(ticketID)
        Thread.sleep(sleepTime)
        val responseAccount = getAccount.Service.get(issueAsset.to)
        responseAccount.value.assetPegWallet.foreach(assets => assets.foreach(asset => blockchainAssets.Service.insertOrUpdate(pegHash = asset.pegHash, documentHash = asset.documentHash, assetType = asset.assetType, assetPrice = asset.assetPrice, assetQuantity = asset.assetQuantity, quantityUnit = asset.quantityUnit, locked = asset.locked, unmoderated = asset.unmoderated, ownerAddress = issueAsset.to, dirtyBit = true)))
        blockchainAccounts.Service.markDirty(masterAccounts.Service.getAddress(issueAsset.from))
        pushNotification.sendNotification(masterAccounts.Service.getId(issueAsset.to), constants.Notification.SUCCESS, response.TxHash)
        pushNotification.sendNotification(issueAsset.from, constants.Notification.SUCCESS, response.TxHash)
      } catch {
        case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
          throw new BaseException(constants.Error.PSQL_EXCEPTION)
        case connectException: ConnectException => logger.error(constants.Error.CONNECT_EXCEPTION, connectException)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.markTransactionFailed(ticketID, message)
        val issueAsset = Service.getTransaction(ticketID)
        pushNotification.sendNotification(masterAccounts.Service.getId(issueAsset.to), constants.Notification.FAILURE, message)
        pushNotification.sendNotification(issueAsset.from, constants.Notification.FAILURE, message)
      } catch {
        case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
      }
    }
  }


  if (kafkaEnabled) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      utilities.TicketUpdater.start(Service.getTicketIDsOnStatus, transactionIssueAsset.Service.getTxFromWSResponse, Utility.onSuccess, Utility.onFailure)
    }
  }
}