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
import transactions.responses.TransactionResponse.Response
import utilities.PushNotification

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SendFiat(from: String, to: String, amount: Int, pegHash: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

@Singleton
class SendFiats @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks, getOrder: GetOrder, transactionSendFiat: transactions.SendFiat, blockchainFiats: blockchain.Fiats, blockchainOrders: blockchain.Orders, blockchainNegotiations: blockchain.Negotiations, actorSystem: ActorSystem, pushNotification: PushNotification, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SEND_FIAT

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val sendFiatTable = TableQuery[SendFiatTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval =  configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private def add(sendFiat: SendFiat)(implicit executionContext: ExecutionContext): Future[String] = db.run((sendFiatTable returning sendFiatTable.map(_.ticketID) += sendFiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def update(sendFiat: SendFiat)(implicit executionContext: ExecutionContext): Future[Int] = db.run(sendFiatTable.insertOrUpdate(sendFiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[SendFiat] = db.run(sendFiatTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class SendFiatTable(tag: Tag) extends Table[SendFiat](tag, "SendFiat") {

    def * = (from, to, amount, pegHash, gas, status.?, txHash.?, ticketID, responseCode.?) <> (SendFiat.tupled, SendFiat.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def amount = column[Int]("amount")

    def pegHash = column[String]("pegHash")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }

  object Service {

    def addSendFiat(from: String, to: String, amount: Int, pegHash: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String]) (implicit executionContext: ExecutionContext): String = Await.result(add(SendFiat(from = from , to = to, amount = amount, pegHash = pegHash, gas = gas, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def updateTxHashStatusResponseCode(ticketID: String, txHash: String, status: Boolean, responseCode: String): Int = Await.result(updateTxHashStatusAndResponseOnTicketID(ticketID, txHash, status, responseCode), Duration.Inf)

    def updateStatusAndResponseCode(ticketID: String, status: Boolean, responseCode: String): Int = Await.result(updateStatusAndResponseOnTicketID(ticketID, status, responseCode), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus(), Duration.Inf)

    def getTransaction(ticketID: String)(implicit executionContext: ExecutionContext): SendFiat = Await.result(findByTicketID(ticketID), Duration.Inf)

  }

  private def updateStatusAndResponseOnTicketID(ticketID: String, status: Boolean, responseCode: String): Future[Int] = db.run(sendFiatTable.filter(_.ticketID === ticketID).map(x => (x.status, x.responseCode)).update((status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus()(implicit executionContext: ExecutionContext): Future[Seq[String]] = db.run(sendFiatTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashStatusAndResponseOnTicketID(ticketID: String, txHash: String, status: Boolean, responseCode: String): Future[Int] = db.run(sendFiatTable.filter(_.ticketID === ticketID).map(x => (x.txHash, x.status, x.responseCode)).update((txHash, status, responseCode)).asTry).map {
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

  object Utility {
    def onSuccess(ticketID: String, response: Response): Future[Unit] = Future {
      try {
        Service.updateTxHashStatusResponseCode(ticketID, response.TxHash, status = true, response.Code)
        val sendFiat = Service.getTransaction(ticketID)
        val fromAddress = masterAccounts.Service.getAddress(sendFiat.from)
        val negotiationID = blockchainNegotiations.Service.getNegotiationID(buyerAddress = fromAddress, sellerAddress = sendFiat.to, pegHash = sendFiat.pegHash)
        blockchainOrders.Service.insertOrUpdateOrder(id = negotiationID, null, null, false)
        blockchainFiats.Service.updateDirtyBit(fromAddress, true)
        blockchainTransactionFeedbacks.Service.updateDirtyBit(fromAddress, true)
        Thread.sleep(sleepTime)
        val orderResponse = getOrder.Service.get(negotiationID)
        orderResponse.value.fiatPegWallet.foreach(fiats => fiats.foreach(fiatPeg => blockchainFiats.Service.insertOrUpdateFiat(pegHash = fiatPeg.pegHash, ownerAddress = negotiationID, transactionID = fiatPeg.transactionID, transactionAmount = fiatPeg.transactionAmount, redeemedAmount = fiatPeg.redeemedAmount, dirtyBit = false)))
        blockchainAccounts.Service.updateDirtyBit(fromAddress, dirtyBit = true)
        pushNotification.sendNotification(masterAccounts.Service.getId(sendFiat.to), constants.Notification.SUCCESS, response.TxHash)
        pushNotification.sendNotification(sendFiat.from, constants.Notification.SUCCESS, response.TxHash)
      }
      catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
        case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.updateStatusAndResponseCode(ticketID, status = false, message)
        val sendFiat = Service.getTransaction(ticketID)
        blockchainTransactionFeedbacks.Service.updateDirtyBit(masterAccounts.Service.getAddress(sendFiat.from), true)
        pushNotification.sendNotification(masterAccounts.Service.getId(sendFiat.to), constants.Notification.FAILURE, message)
        pushNotification.sendNotification(sendFiat.from, constants.Notification.FAILURE, message)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }


  if (kafkaEnabled) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      utilities.TicketUpdater.start(Service.getTicketIDsOnStatus, transactionSendFiat.Service.getTxFromWSResponse, Utility.onSuccess, Utility.onFailure)
    }
  }
}