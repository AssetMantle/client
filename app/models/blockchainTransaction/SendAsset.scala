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
import queries.GetOrder
import queries.responses.AccountResponse
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.Response
import utilities.PushNotifications

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SendAsset(from: String, to: String, pegHash: String, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

@Singleton
class SendAssets @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, getOrder: GetOrder, blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks, blockchainAssets: blockchain.Assets, blockchainOrders: blockchain.Orders, blockchainNegotiations: blockchain.Negotiations, transactionSendAsset: transactions.SendAsset, actorSystem: ActorSystem, pushNotifications: PushNotifications, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SEND_ASSET

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val sendAssetTable = TableQuery[SendAssetTable]

  private def add(sendAsset: SendAsset)(implicit executionContext: ExecutionContext): Future[String] = db.run((sendAssetTable returning sendAssetTable.map(_.ticketID) += sendAsset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def insertOrUpdate(sendAsset: SendAsset)(implicit executionContext: ExecutionContext): Future[Int] = db.run(sendAssetTable.insertOrUpdate(sendAsset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[SendAsset] = db.run(sendAssetTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndResponseOnTicketID(ticketID: String, status: Boolean, responseCode: String): Future[Int] = db.run(sendAssetTable.filter(_.ticketID === ticketID).map(x => (x.status, x.responseCode)).update((status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus()(implicit executionContext: ExecutionContext): Future[Seq[String]] = db.run(sendAssetTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashStatusAndResponseOnTicketID(ticketID: String, txHash: String, status: Boolean, responseCode: String): Future[Int] = db.run(sendAssetTable.filter(_.ticketID === ticketID).map(x => (x.txHash, x.status, x.responseCode)).update((txHash, status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByTicketID(ticketID: String)(implicit executionContext: ExecutionContext) = db.run(sendAssetTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
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

    def addSendAsset(from: String, to: String, pegHash: String, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])(implicit executionContext: ExecutionContext): String = Await.result(add(SendAsset(from = from, to = to, pegHash = pegHash, gas = gas, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def updateTxHashStatusResponseCode(ticketID: String, txHash: String, status: Boolean, responseCode: String): Int = Await.result(updateTxHashStatusAndResponseOnTicketID(ticketID, txHash, status, responseCode), Duration.Inf)

    def updateStatusAndResponseCode(ticketID: String, status: Boolean, responseCode: String): Int = Await.result(updateStatusAndResponseOnTicketID(ticketID, status, responseCode), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus(), Duration.Inf)

    def getTransaction(ticketID: String)(implicit executionContext: ExecutionContext): SendAsset = Await.result(findByTicketID(ticketID), Duration.Inf)

  }

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  object Utility {
    def onSuccess(ticketID: String, response: Response): Future[Unit] = Future {
      try {
        Service.updateTxHashStatusResponseCode(ticketID, response.TxHash, status = true, response.Code)
        val sendAsset = Service.getTransaction(ticketID)
        val fromAddress = masterAccounts.Service.getAddress(sendAsset.from)
        val negotiationID = blockchainNegotiations.Service.getNegotiationID(buyerAddress = sendAsset.to, sellerAddress = fromAddress, pegHash = sendAsset.pegHash)
        blockchainOrders.Service.insertOrUpdateOrder(id = negotiationID, null, null, false)
        Thread.sleep(sleepTime)
        val orderResponse = getOrder.Service.get(negotiationID)
        orderResponse.value.assetPegWallet.get.map { responseAssetPeg: AccountResponse.Asset => blockchainAssets.Service.insertOrUpdateAsset(pegHash = responseAssetPeg.pegHash, documentHash = responseAssetPeg.documentHash, assetType = responseAssetPeg.assetType, assetQuantity = responseAssetPeg.assetQuantity, assetPrice = responseAssetPeg.assetPrice, quantityUnit = responseAssetPeg.quantityUnit, ownerAddress = negotiationID, moderated = responseAssetPeg.moderated, locked = responseAssetPeg.locked, dirtyBit = false) }
        blockchainAccounts.Service.updateDirtyBit(fromAddress, true)
        blockchainTransactionFeedbacks.Service.updateDirtyBit(fromAddress, true)
        pushNotifications.sendNotification(masterAccounts.Service.getId(sendAsset.to), constants.Notification.SUCCESS, response.TxHash)
        pushNotifications.sendNotification(sendAsset.from, constants.Notification.SUCCESS, response.TxHash)
      } catch {
        case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
          throw new BaseException(constants.Error.PSQL_EXCEPTION)
        case connectException: ConnectException => logger.error(constants.Error.CONNECT_EXCEPTION, connectException)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.updateStatusAndResponseCode(ticketID, status = false, message)
        val sendAsset = Service.getTransaction(ticketID)
        blockchainTransactionFeedbacks.Service.updateDirtyBit(masterAccounts.Service.getAddress(sendAsset.from), true)
        pushNotifications.sendNotification(masterAccounts.Service.getId(sendAsset.to), constants.Notification.FAILURE, message)
        pushNotifications.sendNotification(sendAsset.from, constants.Notification.FAILURE, message)
      } catch {
        case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
      }
    }
  }


  if (kafkaEnabled) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      utilities.TicketUpdater.start(Service.getTicketIDsOnStatus, transactionSendAsset.Service.getTxFromWSResponse, Utility.onSuccess, Utility.onFailure)
    }
  }
}