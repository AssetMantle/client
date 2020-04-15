package models.blockchainTransaction

import java.net.ConnectException

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.GetOrder
import queries.responses.OrderResponse
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SendFiat(from: String, to: String, amount: Int, pegHash: String, gas: Int, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None) extends BaseTransaction[SendFiat] {
  def mutateTicketID(newTicketID: String): SendFiat = SendFiat(from = from, to = to, amount = amount, pegHash = pegHash, gas = gas, status = status, txHash = txHash, ticketID = newTicketID, mode = mode, code = code)
}


@Singleton
class SendFiats @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks, getOrder: GetOrder, transactionSendFiat: transactions.SendFiat, blockchainFiats: blockchain.Fiats, blockchainOrders: blockchain.Orders, blockchainNegotiations: blockchain.Negotiations, utilitiesNotification: utilities.Notification, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SEND_FIAT

  private implicit val logger: Logger = Logger(this.getClass)
  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db
  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val sendFiatTable = TableQuery[SendFiatTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")
  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

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

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(sendFiatTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(sendFiatTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
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

  private def deleteByTicketID(ticketID: String) = db.run(sendFiatTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(sendFiatTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
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

    def create(sendFiat: SendFiat): Future[String] = add(SendFiat(from = sendFiat.from, to = sendFiat.to, amount = sendFiat.amount, pegHash = sendFiat.pegHash, gas = sendFiat.gas, status = sendFiat.status, txHash = sendFiat.txHash, ticketID = sendFiat.ticketID, mode = sendFiat.mode, code = sendFiat.code))

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[SendFiat] = findByTicketID(ticketID)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val sendFiat = Service.getTransaction(ticketID)

      def getNegotiationID(sendFiat: SendFiat): Future[String] = blockchainNegotiations.Service.getNegotiationID(buyerAddress = sendFiat.from, sellerAddress = sendFiat.to, pegHash = sendFiat.pegHash).map(_.getOrElse(throw new BaseException(constants.Response.NEGOTIATION_NOT_FOUND)))

      def insertOrUpdate(negotiationID: String): Future[Int] = blockchainOrders.Service.insertOrUpdate(id = negotiationID, None, None, dirtyBit = true)

      def orderResponse(negotiationID: String): Future[OrderResponse.Response] = getOrder.Service.get(negotiationID)

      def insertOrUpdateFiats(orderResponse: queries.responses.OrderResponse.Response, negotiationID: String) = {
        orderResponse.value.fiatPegWallet match {
          case Some(fiats) => Future.sequence(fiats.map(fiatPeg => blockchainFiats.Service.insertOrUpdate(pegHash = fiatPeg.pegHash, ownerAddress = negotiationID, transactionID = fiatPeg.transactionID, transactionAmount = fiatPeg.transactionAmount, redeemedAmount = fiatPeg.redeemedAmount, dirtyBit = false)))
          case None => Future {}
        }
      }

      def markDirty(sendFiat: SendFiat): Future[Unit] = {
        val markDirtyBlockchainFiats = blockchainFiats.Service.markDirty(sendFiat.from)
        val markDirtyBlockchainTransactionFeedbacks = blockchainTransactionFeedbacks.Service.markDirty(sendFiat.from)
        val markDirtyBlockchainAccounts = blockchainAccounts.Service.markDirty(sendFiat.from)
        for {
          _ <- markDirtyBlockchainFiats
          _ <- markDirtyBlockchainTransactionFeedbacks
          _ <- markDirtyBlockchainAccounts
        } yield {}
      }

      def getIDs(sendFiat: SendFiat): Future[(String, String)] = {
        val toAccountID = masterAccounts.Service.getId(sendFiat.to)
        val fromAccountID = masterAccounts.Service.getId(sendFiat.from)
        for {
          toAccountID <- toAccountID
          fromAccountID <- fromAccountID
        } yield (toAccountID, fromAccountID)
      }

      (for {
        _ <- markTransactionSuccessful
        sendFiat <- sendFiat
        negotiationID <- getNegotiationID(sendFiat)
        _ <- insertOrUpdate(negotiationID)
        orderResponse <- orderResponse(negotiationID)
        _ <- insertOrUpdateFiats(orderResponse, negotiationID)
        _ <- markDirty(sendFiat)
        (toAccountID, fromAccountID) <- getIDs(sendFiat)
        _ <- utilitiesNotification.send(toAccountID, constants.Notification.SUCCESS, blockResponse.txhash)
        _ <- utilitiesNotification.send(fromAccountID, constants.Notification.SUCCESS, blockResponse.txhash)
      } yield {}).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
        case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = {
      val markTransactionFailed = Service.markTransactionFailed(ticketID, message)
      val sendFiat = Service.getTransaction(ticketID)

      def markDirty(fromAddress: String): Future[Int] = blockchainTransactionFeedbacks.Service.markDirty(fromAddress)

      def getIDs(sendFiat: SendFiat): Future[(String, String)] = {
        val toAccountID = masterAccounts.Service.getId(sendFiat.to)
        val fromAccountID = masterAccounts.Service.getId(sendFiat.from)
        for {
          toAccountID <- toAccountID
          fromAccountID <- fromAccountID
        } yield (toAccountID, fromAccountID)
      }

      (for {
        _ <- markTransactionFailed
        sendFiat <- sendFiat
        _ <- markDirty(sendFiat.from)
        (toAccountID, fromAccountID) <- getIDs(sendFiat)
        _ <- utilitiesNotification.send(toAccountID, constants.Notification.FAILURE, message)
        _ <- utilitiesNotification.send(fromAccountID, constants.Notification.FAILURE, message)
      } yield {}).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  if (kafkaEnabled || transactionMode != constants.Transactions.BLOCK_MODE) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      transaction.ticketUpdater(Service.getTicketIDsOnStatus, Service.getTransactionHash, Service.getMode, Utility.onSuccess, Utility.onFailure)
    }(schedulerExecutionContext)
  }
}