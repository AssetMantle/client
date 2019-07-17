package models.blockchainTransaction

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.AssetCometMessage
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.AccountResponse
import queries.{GetAccount, GetOrder}
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.Response
import utilities.PushNotification
import akka.actor.ActorSystem

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SendAsset(from: String, to: String, pegHash: String, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

@Singleton
class SendAssets @Inject()(actorSystem: ActorSystem, protected val databaseConfigProvider: DatabaseConfigProvider, getAccount: GetAccount, getOrder: GetOrder, blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks, blockchainAssets: blockchain.Assets, blockchainOrders: blockchain.Orders, blockchainNegotiations: blockchain.Negotiations, transactionSendAsset: transactions.SendAsset, pushNotification: PushNotification, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SEND_ASSET

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val sendAssetTable = TableQuery[SendAssetTable]

  private implicit val assetWrites: OWrites[blockchain.Asset] = Json.writes[blockchain.Asset]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private def add(sendAsset: SendAsset)(implicit executionContext: ExecutionContext): Future[String] = db.run((sendAssetTable returning sendAssetTable.map(_.ticketID) += sendAsset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(sendAsset: SendAsset)(implicit executionContext: ExecutionContext): Future[Int] = db.run(sendAssetTable.insertOrUpdate(sendAsset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[SendAsset] = db.run(sendAssetTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndResponseCodeOnTicketID(ticketID: String, status: Option[Boolean], responseCode: String): Future[Int] = db.run(sendAssetTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.responseCode)).update((status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(sendAssetTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashStatusAndResponseCodeOnTicketID(ticketID: String, txHash: String, status: Option[Boolean], responseCode: String): Future[Int] = db.run(sendAssetTable.filter(_.ticketID === ticketID).map(x => (x.txHash, x.status.?, x.responseCode)).update((txHash, status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByTicketID(ticketID: String)(implicit executionContext: ExecutionContext) = db.run(sendAssetTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class SendAssetTable(tag: Tag) extends Table[SendAsset](tag, "SendAsset") {

    def * = (from, to, pegHash, gas, status.?, txHash.?, ticketID, responseCode.?) <> (SendAsset.tupled, SendAsset.unapply)

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

    def create(from: String, to: String, pegHash: String, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])(implicit executionContext: ExecutionContext): String = Await.result(add(SendAsset(from = from, to = to, pegHash = pegHash, gas = gas, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def markTransactionSuccessful(ticketID: String, txHash: String, responseCode: String): Int = Await.result(updateTxHashStatusAndResponseCodeOnTicketID(ticketID, txHash, status = Option(true), responseCode), Duration.Inf)

    def markTransactionFailed(ticketID: String, responseCode: String): Int = Await.result(updateStatusAndResponseCodeOnTicketID(ticketID, status = Option(false), responseCode), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus, Duration.Inf)

    def getTransaction(ticketID: String)(implicit executionContext: ExecutionContext): SendAsset = Await.result(findByTicketID(ticketID), Duration.Inf)

  }

  object Utility {
    def onSuccess(ticketID: String, response: Response): Future[Unit] = Future {
      try {
        Service.markTransactionSuccessful(ticketID, response.TxHash, response.Code)
        val sendAsset = Service.getTransaction(ticketID)
        val sellerAddress = masterAccounts.Service.getAddress(sendAsset.from)
        val negotiationID = blockchainNegotiations.Service.getNegotiationID(buyerAddress = sendAsset.to, sellerAddress = sellerAddress, pegHash = sendAsset.pegHash)
        blockchainOrders.Service.insertOrUpdate(id = negotiationID, null, null, false)
        Thread.sleep(sleepTime)
        val orderResponse = getOrder.Service.get(negotiationID)
        orderResponse.value.assetPegWallet.foreach(assets => assets.foreach(asset => blockchainAssets.Service.insertOrUpdate(pegHash = asset.pegHash, documentHash = asset.documentHash, assetType = asset.assetType, assetPrice = asset.assetPrice, assetQuantity = asset.assetQuantity, quantityUnit = asset.quantityUnit, locked = asset.locked, unmoderated = asset.unmoderated, ownerAddress = negotiationID, dirtyBit = false)))
        val assetPegWallet = getAccount.Service.get(sellerAddress).value.assetPegWallet.getOrElse(Seq[AccountResponse.Asset]())
        blockchainAssets.mainAssetActor ! AssetCometMessage(ownerAddress = sellerAddress, message = Json.toJson(assetPegWallet.map { asset => Json.toJson(blockchain.Asset(pegHash = asset.pegHash, documentHash = asset.documentHash, assetType = asset.assetType, assetPrice = asset.assetPrice, assetQuantity = asset.assetQuantity, quantityUnit = asset.quantityUnit, ownerAddress = sellerAddress, locked = asset.locked, unmoderated = asset.unmoderated, dirtyBit = false)) }))
        blockchainAccounts.Service.markDirty(sellerAddress)
        blockchainTransactionFeedbacks.Service.markDirty(sellerAddress)
        pushNotification.sendNotification(masterAccounts.Service.getId(sendAsset.to), constants.Notification.SUCCESS, response.TxHash)
        pushNotification.sendNotification(sendAsset.from, constants.Notification.SUCCESS, response.TxHash)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
        case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.markTransactionFailed(ticketID, message)
        val sendAsset = Service.getTransaction(ticketID)
        blockchainTransactionFeedbacks.Service.markDirty(masterAccounts.Service.getAddress(sendAsset.from))
        pushNotification.sendNotification(masterAccounts.Service.getId(sendAsset.to), constants.Notification.FAILURE, message)
        pushNotification.sendNotification(sendAsset.from, constants.Notification.FAILURE, message)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }


  if (kafkaEnabled) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      utilities.TicketUpdater.start(Service.getTicketIDsOnStatus, transactionSendAsset.Service.getTxFromWSResponse, Utility.onSuccess, Utility.onFailure)
    }
  }
}