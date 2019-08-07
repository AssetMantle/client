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
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse
import utilities.PushNotification

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SendFiat(from: String, to: String, amount: Int, pegHash: String, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, mode: String, code: Option[String])

@Singleton
class SendFiats @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks, getOrder: GetOrder, transactionSendFiat: transactions.SendFiat, blockchainFiats: blockchain.Fiats, blockchainOrders: blockchain.Orders, blockchainNegotiations: blockchain.Negotiations, pushNotification: PushNotification, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SEND_FIAT

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val sendFiatTable = TableQuery[SendFiatTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private def add(sendFiat: SendFiat): Future[String] = db.run((sendFiatTable returning sendFiatTable.map(_.ticketID) += sendFiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(sendFiat: SendFiat): Future[Int] = db.run(sendFiatTable.insertOrUpdate(sendFiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String): Future[SendFiat] = db.run(sendFiatTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(sendFiatTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(sendFiatTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(sendFiatTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByTicketID(ticketID: String)(implicit executionContext: ExecutionContext) = db.run(sendFiatTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class SendFiatTable(tag: Tag) extends Table[SendFiat](tag, "SendFiat") {

    def * = (from, to, amount, pegHash, gas, status.?, txHash.?, ticketID, mode, code.?) <> (SendFiat.tupled, SendFiat.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def amount = column[Int]("amount")

    def pegHash = column[String]("pegHash")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def mode = column[String]("mode")

    def code = column[String]("code")
  }

  object Service {

    def create(sendFiat: SendFiat): String = Await.result(add(SendFiat(from = sendFiat.from, to = sendFiat.to, amount = sendFiat.amount, pegHash = sendFiat.pegHash, gas = sendFiat.gas, status = sendFiat.status, txHash = sendFiat.txHash, ticketID = sendFiat.ticketID, mode = sendFiat.mode, code = sendFiat.code)), Duration.Inf)

    def markTransactionSuccessful(ticketID: String, txHash: String): Int = Await.result(updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true)), Duration.Inf)

    def markTransactionFailed(ticketID: String, code: String): Int = Await.result(updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus, Duration.Inf)

    def getTransaction(ticketID: String): SendFiat = Await.result(findByTicketID(ticketID), Duration.Inf)

    def getTransactionHash(ticketID: String): Option[String] = Await.result(findByTicketID(ticketID), Duration.Inf).txHash

  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = Future {
      try {
        Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
        val sendFiat = Service.getTransaction(ticketID)
        val fromAddress = masterAccounts.Service.getAddress(sendFiat.from)
        val negotiationID = blockchainNegotiations.Service.getNegotiationID(buyerAddress = fromAddress, sellerAddress = sendFiat.to, pegHash = sendFiat.pegHash)
        blockchainOrders.Service.insertOrUpdate(id = negotiationID, null, null, false)
        blockchainFiats.Service.markDirty(fromAddress)
        blockchainTransactionFeedbacks.Service.markDirty(fromAddress)
        Thread.sleep(sleepTime)
        val orderResponse = getOrder.Service.get(negotiationID)
        orderResponse.value.fiatPegWallet.foreach(fiats => fiats.foreach(fiatPeg => blockchainFiats.Service.insertOrUpdate(pegHash = fiatPeg.pegHash, ownerAddress = negotiationID, transactionID = fiatPeg.transactionID, transactionAmount = fiatPeg.transactionAmount, redeemedAmount = fiatPeg.redeemedAmount, dirtyBit = false)))
        blockchainAccounts.Service.markDirty(fromAddress)
        pushNotification.sendNotification(masterAccounts.Service.getId(sendFiat.to), constants.Notification.SUCCESS, blockResponse.txhash)
        pushNotification.sendNotification(sendFiat.from, constants.Notification.SUCCESS, blockResponse.txhash)
      }
      catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
        case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.markTransactionFailed(ticketID, message)
        val sendFiat = Service.getTransaction(ticketID)
        blockchainTransactionFeedbacks.Service.markDirty(masterAccounts.Service.getAddress(sendFiat.from))
        pushNotification.sendNotification(masterAccounts.Service.getId(sendFiat.to), constants.Notification.FAILURE, message)
        pushNotification.sendNotification(sendFiat.from, constants.Notification.FAILURE, message)
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