package models.blockchainTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class BuyerExecuteOrder(from: String, buyerAddress: String, sellerAddress: String, fiatProofHash: String, pegHash: String, gas: Int, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None) extends BaseTransaction[BuyerExecuteOrder] {
  def mutateTicketID(newTicketID: String): BuyerExecuteOrder = BuyerExecuteOrder(from = from, buyerAddress = buyerAddress, sellerAddress = sellerAddress, fiatProofHash = fiatProofHash, pegHash = pegHash, gas = gas, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}

@Singleton
class BuyerExecuteOrders @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks, blockchainNegotiations: blockchain.Negotiations, blockchainOrders: blockchain.Orders, transactionBuyerExecuteOrder: transactions.BuyerExecuteOrder, utilitiesNotification: utilities.Notification, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_BUYER_EXECUTE_ORDER

  private implicit val logger: Logger = Logger(this.getClass)
  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db
  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

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

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
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

  private def updateStatusByTicketID(ticketID: String, status: Option[Boolean]): Future[Int] = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).map(_.status.?).update(status).asTry).map {
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

  private def deleteByTicketID(ticketID: String) = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class BuyerExecuteOrderTable(tag: Tag) extends Table[BuyerExecuteOrder](tag, "BuyerExecuteOrder") {

    def * = (from, buyerAddress, sellerAddress, fiatProofHash, pegHash, gas, status.?, txHash.?, ticketID, mode, code.?) <> (BuyerExecuteOrder.tupled, BuyerExecuteOrder.unapply)

    def from = column[String]("from")

    def buyerAddress = column[String]("buyerAddress")

    def sellerAddress = column[String]("sellerAddress")

    def fiatProofHash = column[String]("fiatProofHash")

    def pegHash = column[String]("pegHash")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def mode = column[String]("mode")

    def code = column[String]("code")
  }

  object Service {

    def create(buyerExecuteOrder: BuyerExecuteOrder): Future[String] = add(BuyerExecuteOrder(from = buyerExecuteOrder.from, buyerAddress = buyerExecuteOrder.buyerAddress, sellerAddress = buyerExecuteOrder.sellerAddress, fiatProofHash = buyerExecuteOrder.fiatProofHash, pegHash = buyerExecuteOrder.pegHash, gas = buyerExecuteOrder.gas, status = buyerExecuteOrder.status, txHash = buyerExecuteOrder.txHash, ticketID = buyerExecuteOrder.ticketID, mode = buyerExecuteOrder.mode, code = buyerExecuteOrder.code))

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def resetTransactionStatus(ticketID: String): Future[Int] = updateStatusByTicketID(ticketID, status = null)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[BuyerExecuteOrder] = findByTicketID(ticketID)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val buyerExecuteOrder = Service.getTransaction(ticketID)

      def getNegotiationID(buyerExecuteOrder: BuyerExecuteOrder): Future[String] = blockchainNegotiations.Service.tryGetID(buyerAddress = buyerExecuteOrder.buyerAddress, sellerAddress = buyerExecuteOrder.sellerAddress, pegHash = buyerExecuteOrder.pegHash)

      def markDirty(negotiationID: String, buyerExecuteOrder: BuyerExecuteOrder): Future[Unit] = {
        val markOrderDirty = blockchainOrders.Service.markDirty(id = negotiationID)
        val markBuyerAccountDirty = blockchainAccounts.Service.markDirty(buyerExecuteOrder.buyerAddress)
        val markBuyerTransactionFeedbackDirty = blockchainTransactionFeedbacks.Service.markDirty(buyerExecuteOrder.buyerAddress)
        val markFromAddressAccountDirty = if (buyerExecuteOrder.from != buyerExecuteOrder.buyerAddress) blockchainAccounts.Service.markDirty(buyerExecuteOrder.from) else Future(0)

        for {
          _ <- markOrderDirty
          _ <- markBuyerAccountDirty
          _ <- markBuyerTransactionFeedbackDirty
          _ <- markFromAddressAccountDirty
        } yield ()
      }

      def getAccountID(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

      def sendFromAccountNotifications(buyerExecuteOrder: BuyerExecuteOrder): Future[Unit] = if (buyerExecuteOrder.from != buyerExecuteOrder.buyerAddress) {
        for {
          fromAccountID <- getAccountID(buyerExecuteOrder.from)
          _ <- utilitiesNotification.send(fromAccountID, constants.Notification.BUYER_EXECUTE_ORDER_SUCCESSFUL, blockResponse.txhash)
        } yield ()
      } else Future()


      (for {
        _ <- markTransactionSuccessful
        buyerExecuteOrder <- buyerExecuteOrder
        negotiationID <- getNegotiationID(buyerExecuteOrder)
        _ <- markDirty(negotiationID, buyerExecuteOrder)
        buyerAccountID <- getAccountID(buyerExecuteOrder.buyerAddress)
        sellAccountID <- getAccountID(buyerExecuteOrder.sellerAddress)
        _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.BUYER_EXECUTE_ORDER_SUCCESSFUL, blockResponse.txhash)
        _ <- utilitiesNotification.send(sellAccountID, constants.Notification.BUYER_EXECUTE_ORDER_SUCCESSFUL, blockResponse.txhash)
        _ <- sendFromAccountNotifications(buyerExecuteOrder)
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          if (baseException.failure == constants.Response.CONNECT_EXCEPTION) {
            (for {
              _ <- Service.resetTransactionStatus(ticketID)
            } yield ()
              ).recover {
              case baseException: BaseException => logger.error(baseException.failure.message, baseException)
                throw baseException
            }
          }
          throw baseException
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = {
      val markTransactionFailed = Service.markTransactionFailed(ticketID, message)
      val buyerExecuteOrder = Service.getTransaction(ticketID)

      def markDirty(buyerExecuteOrder: BuyerExecuteOrder): Future[Int] = if (buyerExecuteOrder.from != buyerExecuteOrder.buyerAddress) blockchainAccounts.Service.markDirty(buyerExecuteOrder.from) else blockchainTransactionFeedbacks.Service.markDirty(buyerExecuteOrder.buyerAddress)

      def getAccountID(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

      (for {
        _ <- markTransactionFailed
        buyerExecuteOrder <- buyerExecuteOrder
        _ <- markDirty(buyerExecuteOrder)
        fromAccountID <- getAccountID(buyerExecuteOrder.from)
        _ <- utilitiesNotification.send(fromAccountID, constants.Notification.BUYER_EXECUTE_ORDER_FAILED, message)
      } yield ()).recover {
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