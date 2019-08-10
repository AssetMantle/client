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
import utilities.{PushNotification, TransactionEntity}

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class BuyerExecuteOrder(from: String, buyerAddress: String, sellerAddress: String, fiatProofHash: String, pegHash: String, status: Option[Boolean], txHash: Option[String], ticketID: String, mode: String, code: Option[String]) extends TransactionEntity[BuyerExecuteOrder] {
  def mutateTicketID(newTicketID: String): BuyerExecuteOrder = BuyerExecuteOrder(from = from, buyerAddress = buyerAddress, sellerAddress = sellerAddress, fiatProofHash = fiatProofHash, pegHash = pegHash, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}

@Singleton
class BuyerExecuteOrders @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks, blockchainNegotiations: blockchain.Negotiations, blockchainOrders: blockchain.Orders, transactionBuyerExecuteOrder: transactions.BuyerExecuteOrder, pushNotification: PushNotification, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_BUYER_EXECUTE_ORDER

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val buyerExecuteOrderTable = TableQuery[BuyerExecuteOrderTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")
  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(buyerExecuteOrder: BuyerExecuteOrder): Future[String] = db.run((buyerExecuteOrderTable returning buyerExecuteOrderTable.map(_.ticketID) += buyerExecuteOrder).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(buyerExecuteOrder: BuyerExecuteOrder): Future[Int] = db.run(buyerExecuteOrderTable.insertOrUpdate(buyerExecuteOrder).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String): Future[BuyerExecuteOrder] = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(buyerExecuteOrderTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByTicketID(ticketID: String)(implicit executionContext: ExecutionContext) = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class BuyerExecuteOrderTable(tag: Tag) extends Table[BuyerExecuteOrder](tag, "BuyerExecuteOrder") {

    def * = (from, buyerAddress, sellerAddress, fiatProofHash, pegHash, status.?, txHash.?, ticketID, mode, code.?) <> (BuyerExecuteOrder.tupled, BuyerExecuteOrder.unapply)

    def from = column[String]("from")

    def buyerAddress = column[String]("buyerAddress")

    def sellerAddress = column[String]("sellerAddress")

    def fiatProofHash = column[String]("fiatProofHash")

    def pegHash = column[String]("pegHash")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def mode = column[String]("mode")

    def code = column[String]("code")
  }

  object Service {

    def create(buyerExecuteOrder: BuyerExecuteOrder): String = Await.result(add(BuyerExecuteOrder(from = buyerExecuteOrder.from, buyerAddress = buyerExecuteOrder.buyerAddress, sellerAddress = buyerExecuteOrder.sellerAddress, fiatProofHash = buyerExecuteOrder.fiatProofHash, pegHash = buyerExecuteOrder.pegHash, status = buyerExecuteOrder.status, txHash = buyerExecuteOrder.txHash, ticketID = buyerExecuteOrder.ticketID, mode = buyerExecuteOrder.mode, code = buyerExecuteOrder.code)), Duration.Inf)

    def markTransactionSuccessful(ticketID: String, txHash: String): Int = Await.result(updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true)), Duration.Inf)

    def markTransactionFailed(ticketID: String, code: String): Int = Await.result(updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus, Duration.Inf)

    def getTransaction(ticketID: String): BuyerExecuteOrder = Await.result(findByTicketID(ticketID), Duration.Inf)

    def getTransactionHash(ticketID: String): Option[String] = Await.result(findByTicketID(ticketID), Duration.Inf).txHash

    def updateTransactionHash(ticketID: String, txHash: String): Int = Await.result(updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash)), Duration.Inf)

  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = Future {
      try {
        Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
        val buyerExecuteOrder = Service.getTransaction(ticketID)
        val negotiationID = blockchainNegotiations.Service.getNegotiationID(buyerAddress = buyerExecuteOrder.buyerAddress, sellerAddress = buyerExecuteOrder.sellerAddress, pegHash = buyerExecuteOrder.pegHash)
        blockchainOrders.Service.markDirty(id = negotiationID)
        blockchainAccounts.Service.markDirty(buyerExecuteOrder.buyerAddress)
        blockchainTransactionFeedbacks.Service.markDirty(buyerExecuteOrder.buyerAddress)
        blockchainTransactionFeedbacks.Service.markDirty(buyerExecuteOrder.sellerAddress)
        pushNotification.sendNotification(masterAccounts.Service.getId(buyerExecuteOrder.sellerAddress), constants.Notification.SUCCESS, blockResponse.txhash)
        pushNotification.sendNotification(masterAccounts.Service.getId(buyerExecuteOrder.buyerAddress), constants.Notification.SUCCESS, blockResponse.txhash)
        if (buyerExecuteOrder.from != buyerExecuteOrder.buyerAddress) {
          blockchainAccounts.Service.markDirty(buyerExecuteOrder.from)
          pushNotification.sendNotification(masterAccounts.Service.getId(buyerExecuteOrder.from), constants.Notification.SUCCESS, blockResponse.txhash)
        }
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.markTransactionFailed(ticketID, message)
        val buyerExecuteOrder = Service.getTransaction(ticketID)
        blockchainTransactionFeedbacks.Service.markDirty(buyerExecuteOrder.buyerAddress)
        blockchainTransactionFeedbacks.Service.markDirty(buyerExecuteOrder.sellerAddress)
        pushNotification.sendNotification(masterAccounts.Service.getId(buyerExecuteOrder.sellerAddress), constants.Notification.FAILURE, message)
        pushNotification.sendNotification(masterAccounts.Service.getId(buyerExecuteOrder.buyerAddress), constants.Notification.FAILURE, message)
        if (buyerExecuteOrder.from != buyerExecuteOrder.buyerAddress) {
          blockchainAccounts.Service.markDirty(buyerExecuteOrder.from)
          pushNotification.sendNotification(masterAccounts.Service.getId(buyerExecuteOrder.from), constants.Notification.FAILURE, message)
        }
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  if (kafkaEnabled || transactionMode != constants.Transactions.BLOCK_MODE) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      transaction.ticketUpdater(Service.getTicketIDsOnStatus, Service.getTransactionHash, Utility.onSuccess, Utility.onFailure)
    }
  }
}