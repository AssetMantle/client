package models.blockchainTransaction

import java.net.ConnectException

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.{GetAccount, GetOrder}
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse
import utilities.PushNotification

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SendAsset(from: String, to: String, pegHash: String, gas: Int,  status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None) extends BaseTransaction[SendAsset] {
  def mutateTicketID(newTicketID: String): SendAsset = SendAsset(from = from, to = to, pegHash = pegHash, gas = gas, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}


@Singleton
class SendAssets @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, getAccount: GetAccount, getOrder: GetOrder, blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks, blockchainAssets: blockchain.Assets, blockchainOrders: blockchain.Orders, blockchainNegotiations: blockchain.Negotiations, transactionSendAsset: transactions.SendAsset, pushNotification: PushNotification, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SEND_ASSET

  private implicit val logger: Logger = Logger(this.getClass)

  private val schedulerExecutionContext:ExecutionContext= actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val sendAssetTable = TableQuery[SendAssetTable]

  private implicit val assetWrites: OWrites[blockchain.Asset] = Json.writes[blockchain.Asset]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")
  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(sendAsset: SendAsset): Future[String] = db.run((sendAssetTable returning sendAssetTable.map(_.ticketID) += sendAsset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(sendAsset: SendAsset): Future[Int] = db.run(sendAssetTable.insertOrUpdate(sendAsset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String): Future[SendAsset] = db.run(sendAssetTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(sendAssetTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(sendAssetTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(sendAssetTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByTicketID(ticketID: String) = db.run(sendAssetTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(sendAssetTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class SendAssetTable(tag: Tag) extends Table[SendAsset](tag, "SendAsset") {

    def * = (from, to, pegHash, gas, status.?, txHash.?, ticketID, mode, code.?) <> (SendAsset.tupled, SendAsset.unapply)

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

    def create(sendAsset: SendAsset): String = Await.result(add(SendAsset(from = sendAsset.from, to = sendAsset.to, pegHash = sendAsset.pegHash, gas=sendAsset.gas, status = sendAsset.status, txHash = sendAsset.txHash, ticketID = sendAsset.ticketID, mode = sendAsset.mode, code = sendAsset.code)), Duration.Inf)

    def markTransactionSuccessful(ticketID: String, txHash: String): Int = Await.result(updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true)), Duration.Inf)

    def markTransactionFailed(ticketID: String, code: String): Int = Await.result(updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus, Duration.Inf)

    def getTransaction(ticketID: String): SendAsset = Await.result(findByTicketID(ticketID), Duration.Inf)

    def getTransactionHash(ticketID: String): Option[String] = Await.result(findByTicketID(ticketID), Duration.Inf).txHash

    def updateTransactionHash(ticketID: String, txHash: String): Int = Await.result(updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash)), Duration.Inf)

  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = Future {
      try {
        Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
        val sendAsset = Service.getTransaction(ticketID)
        val negotiationID = blockchainNegotiations.Service.getNegotiationID(buyerAddress = sendAsset.to, sellerAddress = sendAsset.from, pegHash = sendAsset.pegHash)
        blockchainOrders.Service.insertOrUpdate(id = negotiationID, None, None, dirtyBit = true)
        Thread.sleep(sleepTime)
        val orderResponse = getOrder.Service.get(negotiationID)
        orderResponse.value.assetPegWallet.foreach(assets => assets.foreach(asset => blockchainAssets.Service.insertOrUpdate(pegHash = asset.pegHash, documentHash = asset.documentHash, assetType = asset.assetType, assetPrice = asset.assetPrice, assetQuantity = asset.assetQuantity, quantityUnit = asset.quantityUnit, locked = asset.locked, moderated = asset.moderated, ownerAddress = negotiationID, takerAddress = if (asset.takerAddress == "") None else Option(asset.takerAddress), dirtyBit = false)))
        blockchainAccounts.Service.markDirty(sendAsset.from)
        blockchainTransactionFeedbacks.Service.markDirty(sendAsset.from)
        pushNotification.sendNotification(masterAccounts.Service.getId(sendAsset.to), constants.Notification.SUCCESS, blockResponse.txhash)
        pushNotification.sendNotification(masterAccounts.Service.getId(sendAsset.from), constants.Notification.SUCCESS, blockResponse.txhash)
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
        pushNotification.sendNotification(masterAccounts.Service.getId(sendAsset.from), constants.Notification.FAILURE, message)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  if (kafkaEnabled || transactionMode != constants.Transactions.BLOCK_MODE) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      transaction.ticketUpdater(Service.getTicketIDsOnStatus, Service.getTransactionHash, Utility.onSuccess, Utility.onFailure)
    }(schedulerExecutionContext)
  }
}