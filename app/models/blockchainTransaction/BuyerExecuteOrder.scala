package models.blockchainTransaction

import java.sql.Timestamp

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.Trait.Logged
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse
import utilities.MicroNumber

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class BuyerExecuteOrder(from: String, buyerAddress: String, sellerAddress: String, fiatProofHash: String, pegHash: String, gas: MicroNumber, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends BaseTransaction[BuyerExecuteOrder] with Logged {
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

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  case class BuyerExecuteOrderSerialized(from: String, buyerAddress: String, sellerAddress: String, fiatProofHash: String, pegHash: String, gas: String, status: Option[Boolean], txHash: Option[String], ticketID: String, mode: String, code: Option[String], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: BuyerExecuteOrder = BuyerExecuteOrder(from = from, buyerAddress = buyerAddress, sellerAddress = sellerAddress, fiatProofHash = fiatProofHash, pegHash = pegHash, gas = new MicroNumber(BigInt(gas)), status = status, txHash = txHash, ticketID = ticketID, mode = mode, code = code, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedOn = updatedOn, updatedBy = updatedBy, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(buyerExecuteOrder: BuyerExecuteOrder): BuyerExecuteOrderSerialized = BuyerExecuteOrderSerialized(from = buyerExecuteOrder.from, buyerAddress = buyerExecuteOrder.buyerAddress, sellerAddress = buyerExecuteOrder.sellerAddress, fiatProofHash = buyerExecuteOrder.fiatProofHash, pegHash = buyerExecuteOrder.pegHash, gas = buyerExecuteOrder.gas.toMicroString, status = buyerExecuteOrder.status, txHash = buyerExecuteOrder.txHash, ticketID = buyerExecuteOrder.ticketID, mode = buyerExecuteOrder.mode, code = buyerExecuteOrder.code, createdBy = buyerExecuteOrder.createdBy, createdOn = buyerExecuteOrder.createdOn, createdOnTimeZone = buyerExecuteOrder.createdOnTimeZone, updatedBy = buyerExecuteOrder.updatedBy, updatedOn = buyerExecuteOrder.updatedOn, updatedOnTimeZone = buyerExecuteOrder.updatedOnTimeZone)

  private def add(buyerExecuteOrder: BuyerExecuteOrder): Future[String] = db.run((buyerExecuteOrderTable returning buyerExecuteOrderTable.map(_.ticketID) += serialize(buyerExecuteOrder)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByTicketID(ticketID: String): Future[BuyerExecuteOrderSerialized] = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusByTicketID(ticketID: String, status: Option[Boolean]): Future[Int] = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(buyerExecuteOrderTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def getTransactionByBuyerSellerAddressesAndPegHash(buyerAddress: String, sellerAddress: String, pegHash: String) = db.run(buyerExecuteOrderTable.filter(x => x.buyerAddress === buyerAddress && x.sellerAddress === sellerAddress && x.pegHash === pegHash).sortBy(x => x.updatedOn.ifNull(x.createdOn).desc).result.headOption)

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByTicketID(ticketID: String) = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class BuyerExecuteOrderTable(tag: Tag) extends Table[BuyerExecuteOrderSerialized](tag, "BuyerExecuteOrder") {

    def * = (from, buyerAddress, sellerAddress, fiatProofHash, pegHash, gas, status.?, txHash.?, ticketID, mode, code.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (BuyerExecuteOrderSerialized.tupled, BuyerExecuteOrderSerialized.unapply)

    def from = column[String]("from")

    def buyerAddress = column[String]("buyerAddress")

    def sellerAddress = column[String]("sellerAddress")

    def fiatProofHash = column[String]("fiatProofHash")

    def pegHash = column[String]("pegHash")

    def gas = column[String]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def mode = column[String]("mode")

    def code = column[String]("code")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(buyerExecuteOrder: BuyerExecuteOrder): Future[String] = add(BuyerExecuteOrder(from = buyerExecuteOrder.from, buyerAddress = buyerExecuteOrder.buyerAddress, sellerAddress = buyerExecuteOrder.sellerAddress, fiatProofHash = buyerExecuteOrder.fiatProofHash, pegHash = buyerExecuteOrder.pegHash, gas = buyerExecuteOrder.gas, status = buyerExecuteOrder.status, txHash = buyerExecuteOrder.txHash, ticketID = buyerExecuteOrder.ticketID, mode = buyerExecuteOrder.mode, code = buyerExecuteOrder.code))

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def resetTransactionStatus(ticketID: String): Future[Int] = updateStatusByTicketID(ticketID, status = null)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[BuyerExecuteOrder] = findByTicketID(ticketID).map(_.deserialize)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

    def getTransactionStatus(buyerAddress: String, sellerAddress: String, pegHash: String) = getTransactionByBuyerSellerAddressesAndPegHash(buyerAddress, sellerAddress, pegHash).map(x => if (x.isDefined) x.get.status else Option(false))
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

  val scheduledTask = new Runnable {
    override def run(): Unit = {
      try {
       Await.result(transaction.ticketUpdater(Service.getTicketIDsOnStatus, Service.getTransactionHash, Service.getMode, Utility.onSuccess, Utility.onFailure), Duration.Inf)
      } catch {
        case exception: Exception => logger.error(exception.getMessage, exception)
      }
    }
  }

  if (kafkaEnabled || transactionMode != constants.Transactions.BLOCK_MODE) {
    actorSystem.scheduler.scheduleWithFixedDelay(initialDelay = schedulerInitialDelay, delay = schedulerInterval)(scheduledTask)(schedulerExecutionContext)
  }
}