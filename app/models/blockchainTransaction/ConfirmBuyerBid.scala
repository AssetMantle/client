package models.blockchainTransaction

import java.sql.Timestamp

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.Trait.Logged
import models.{blockchain, master, masterTransaction}
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

case class ConfirmBuyerBid(from: String, to: String, bid: MicroNumber, time: Int, pegHash: String, buyerContractHash: String, gas: MicroNumber, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends BaseTransaction[ConfirmBuyerBid] with Logged {
  def mutateTicketID(newTicketID: String): ConfirmBuyerBid = ConfirmBuyerBid(from = from, to = to, bid = bid, time = time, pegHash = pegHash, buyerContractHash = buyerContractHash, gas = gas, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}

@Singleton
class ConfirmBuyerBids @Inject()(
                                  actorSystem: ActorSystem,
                                  transaction: utilities.Transaction,
                                  protected val databaseConfigProvider: DatabaseConfigProvider,
                                  blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks,
                                  blockchainNegotiations: blockchain.Negotiations,
                                  utilitiesNotification: utilities.Notification,
                                  blockchainAccounts: blockchain.Accounts,
                                  masterTransactionTradeActivities: masterTransaction.TradeActivities,
                                  masterNegotiations: master.Negotiations,
                                )(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  case class ConfirmBuyerBidSerialized(from: String, to: String, bid: String, time: Int, pegHash: String, buyerContractHash: String, gas: String, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: ConfirmBuyerBid = ConfirmBuyerBid(from = from, to = to, bid = new MicroNumber(BigInt(bid)), time = time, pegHash = pegHash, buyerContractHash = buyerContractHash, gas = new MicroNumber(BigInt(gas)), status = status, txHash = txHash, ticketID = ticketID, mode = mode, code = code, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(confirmBuyerBid: ConfirmBuyerBid): ConfirmBuyerBidSerialized = ConfirmBuyerBidSerialized(from = confirmBuyerBid.from, to = confirmBuyerBid.to, bid = confirmBuyerBid.bid.toMicroString, time = confirmBuyerBid.time, pegHash = confirmBuyerBid.pegHash, buyerContractHash = confirmBuyerBid.buyerContractHash, gas = confirmBuyerBid.gas.toMicroString, status = confirmBuyerBid.status, txHash = confirmBuyerBid.txHash, ticketID = confirmBuyerBid.ticketID, mode = confirmBuyerBid.mode, code = confirmBuyerBid.code, createdBy = confirmBuyerBid.createdBy, createdOn = confirmBuyerBid.createdOn, createdOnTimeZone = confirmBuyerBid.createdOnTimeZone, updatedBy = confirmBuyerBid.updatedBy, updatedOn = confirmBuyerBid.updatedOn, updatedOnTimeZone = confirmBuyerBid.updatedOnTimeZone)

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_CONFIRM_BUYER_BID

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val confirmBuyerBidTable = TableQuery[ConfirmBuyerBidTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(confirmBuyerBidSerialized: ConfirmBuyerBidSerialized): Future[String] = db.run((confirmBuyerBidTable returning confirmBuyerBidTable.map(_.ticketID) += confirmBuyerBidSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByTicketID(ticketID: String): Future[ConfirmBuyerBidSerialized] = db.run(confirmBuyerBidTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(confirmBuyerBidTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(confirmBuyerBidTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(confirmBuyerBidTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusByTicketID(ticketID: String, status: Option[Boolean]): Future[Int] = db.run(confirmBuyerBidTable.filter(_.ticketID === ticketID).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(confirmBuyerBidTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def getTransactionByFromToAndPegHash(from: String, to: String, pegHash: String) = db.run(confirmBuyerBidTable.filter(x => x.from === from && x.to === to && x.pegHash === pegHash).sortBy(x => x.updatedOn.ifNull(x.createdOn).desc).result.headOption)

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(confirmBuyerBidTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(confirmBuyerBidTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByTicketID(ticketID: String) = db.run(confirmBuyerBidTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class ConfirmBuyerBidTable(tag: Tag) extends Table[ConfirmBuyerBidSerialized](tag, "ConfirmBuyerBid") {

    def * = (from, to, bid, time, pegHash, buyerContractHash, gas, status.?, txHash.?, ticketID, mode, code.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (ConfirmBuyerBidSerialized.tupled, ConfirmBuyerBidSerialized.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def bid = column[String]("bid")

    def time = column[Int]("time")

    def pegHash = column[String]("pegHash")

    def buyerContractHash = column[String]("buyerContractHash")

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

    def create(confirmBuyerBid: ConfirmBuyerBid): Future[String] = add(serialize(ConfirmBuyerBid(from = confirmBuyerBid.from, to = confirmBuyerBid.to, bid = confirmBuyerBid.bid, time = confirmBuyerBid.time, pegHash = confirmBuyerBid.pegHash, gas = confirmBuyerBid.gas, buyerContractHash = confirmBuyerBid.buyerContractHash, status = confirmBuyerBid.status, txHash = confirmBuyerBid.txHash, ticketID = confirmBuyerBid.ticketID, mode = confirmBuyerBid.mode, code = confirmBuyerBid.code)))

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def resetTransactionStatus(ticketID: String): Future[Int] = updateStatusByTicketID(ticketID, status = null)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[ConfirmBuyerBid] = findByTicketID(ticketID).map(_.deserialize)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

    def getTransactionStatus(from: String, to: String, pegHash: String) = getTransactionByFromToAndPegHash(from, to, pegHash).map(x => if (x.isDefined) x.get.status else Option(false))
  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val confirmBuyerBid = Service.getTransaction(ticketID)

      def negotiationID(confirmBuyerBid: ConfirmBuyerBid): Future[String] = blockchainNegotiations.Service.tryGetID(buyerAddress = confirmBuyerBid.from, sellerAddress = confirmBuyerBid.to, pegHash = confirmBuyerBid.pegHash)

      def markDirty(negotiationID: String, confirmBuyerBid: ConfirmBuyerBid): Future[Unit] = {
        val markNegotiationDirty = blockchainNegotiations.Service.markDirty(negotiationID)
        val markBuyerAccountDirty = blockchainAccounts.Service.markDirty(confirmBuyerBid.from)
        val markBuyerTransactionFeedbackDirty = blockchainTransactionFeedbacks.Service.markDirty(confirmBuyerBid.from)
        for {
          _ <- markNegotiationDirty
          _ <- markBuyerAccountDirty
          _ <- markBuyerTransactionFeedbackDirty
        } yield ()
      }

      def getAccountID(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

      def masterNegotiationID(bcNegotiationID: String): Future[String] = masterNegotiations.Service.tryGetIDByNegotiationID(bcNegotiationID)

      (for {
        _ <- markTransactionSuccessful
        confirmBuyerBid <- confirmBuyerBid
        negotiationID <- negotiationID(confirmBuyerBid)
        _ <- markDirty(negotiationID = negotiationID, confirmBuyerBid = confirmBuyerBid)
        buyerAccountID <- getAccountID(confirmBuyerBid.from)
        sellerAccountID <- getAccountID(confirmBuyerBid.to)
        _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.BUYER_BID_CONFIRMED, ticketID, blockResponse.txhash)
        _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.BUYER_BID_CONFIRMED, ticketID, blockResponse.txhash)
        masterNegotiationID <- masterNegotiationID(negotiationID)
        _ <- masterTransactionTradeActivities.Service.create(masterNegotiationID, constants.TradeActivity.BUYER_BID_CONFIRMED, ticketID, blockResponse.txhash)
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
      val confirmBuyerBid = Service.getTransaction(ticketID)

      def markDirty(confirmBuyerBid: ConfirmBuyerBid): Future[Int] = blockchainTransactionFeedbacks.Service.markDirty(confirmBuyerBid.from)

      def getID(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

      (for {
        _ <- markTransactionFailed
        confirmBuyerBid <- confirmBuyerBid
        _ <- markDirty(confirmBuyerBid)
        fromAccountID <- getID(confirmBuyerBid.from)
        _ <- utilitiesNotification.send(fromAccountID, constants.Notification.BUYER_BID_CONFIRMATION_FAILED, message)
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