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
import queries.responses.NegotiationResponse
import queries.{GetNegotiation, GetNegotiationID}
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ConfirmSellerBid(from: String, to: String, bid: Int, time: Int, pegHash: String, sellerContractHash: String, gas: Int, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None) extends BaseTransaction[ConfirmSellerBid] {
  def mutateTicketID(newTicketID: String): ConfirmSellerBid = ConfirmSellerBid(from = from, to = to, bid = bid, time = time, pegHash = pegHash, sellerContractHash = sellerContractHash, gas = gas, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}


@Singleton
class ConfirmSellerBids @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks, getNegotiationID: GetNegotiationID, getNegotiation: GetNegotiation, blockchainNegotiations: blockchain.Negotiations, transactionConfirmSellerBid: transactions.ConfirmSellerBid, utilitiesNotification: utilities.Notification, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_CONFIRM_SELLER_BID

  private implicit val logger: Logger = Logger(this.getClass)
  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db
  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val confirmSellerBidTable = TableQuery[ConfirmSellerBidTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")
  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(confirmSellerBid: ConfirmSellerBid): Future[String] = db.run((confirmSellerBidTable returning confirmSellerBidTable.map(_.ticketID) += confirmSellerBid).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(confirmSellerBid: ConfirmSellerBid): Future[Int] = db.run(confirmSellerBidTable.insertOrUpdate(confirmSellerBid).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String): Future[ConfirmSellerBid] = db.run(confirmSellerBidTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(confirmSellerBidTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(confirmSellerBidTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(confirmSellerBidTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(confirmSellerBidTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(confirmSellerBidTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(confirmSellerBidTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByTicketID(ticketID: String) = db.run(confirmSellerBidTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class ConfirmSellerBidTable(tag: Tag) extends Table[ConfirmSellerBid](tag, "ConfirmSellerBid") {

    def * = (from, to, bid, time, pegHash, sellerContractHash, gas, status.?, txHash.?, ticketID, mode, code.?) <> (ConfirmSellerBid.tupled, ConfirmSellerBid.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def bid = column[Int]("bid")

    def time = column[Int]("time")

    def pegHash = column[String]("pegHash")

    def sellerContractHash = column[String]("sellerContractHash")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def mode = column[String]("mode")

    def code = column[String]("code")
  }

  object Service {

    def create(confirmSellerBid: ConfirmSellerBid): Future[String] = add(ConfirmSellerBid(from = confirmSellerBid.from, to = confirmSellerBid.to, bid = confirmSellerBid.bid, time = confirmSellerBid.time, pegHash = confirmSellerBid.pegHash, sellerContractHash = confirmSellerBid.sellerContractHash, gas = confirmSellerBid.gas, status = confirmSellerBid.status, txHash = confirmSellerBid.txHash, ticketID = confirmSellerBid.ticketID, mode = confirmSellerBid.mode, code = confirmSellerBid.code))

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[ConfirmSellerBid] = findByTicketID(ticketID)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

  }

  object Utility {


    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = {

      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val confirmSellerBid = Service.getTransaction(ticketID)

      def negotiationID(confirmSellerBid: ConfirmSellerBid) = blockchainNegotiations.Service.getNegotiationID(buyerAddress = confirmSellerBid.to, sellerAddress = confirmSellerBid.from, pegHash = confirmSellerBid.pegHash)

      def negotiationResponse(negotiationID: String, confirmSellerBid: ConfirmSellerBid) = if (negotiationID == "") getNegotiation.Service.get(getNegotiationID.Service.get(buyerAddress = confirmSellerBid.from, sellerAddress = confirmSellerBid.to, pegHash = confirmSellerBid.pegHash).negotiationID) else getNegotiation.Service.get(negotiationID)

      def insertOrUpdate(negotiationResponse: NegotiationResponse.Response) = blockchainNegotiations.Service.insertOrUpdate(id = negotiationResponse.value.negotiationID, buyerAddress = negotiationResponse.value.buyerAddress, sellerAddress = negotiationResponse.value.sellerAddress, assetPegHash = negotiationResponse.value.pegHash, bid = negotiationResponse.value.bid, time = negotiationResponse.value.time, buyerSignature = negotiationResponse.value.buyerSignature, sellerSignature = negotiationResponse.value.sellerSignature, buyerBlockHeight = negotiationResponse.value.buyerBlockHeight, sellerBlockHeight = negotiationResponse.value.sellerBlockHeight, buyerContractHash = negotiationResponse.value.buyerContractHash, sellerContractHash = negotiationResponse.value.sellerContractHash, dirtyBit = true)

      def markDirty(confirmSellerBid: ConfirmSellerBid) = {
        val markDirtyFromAddressBlockchainAccounts = blockchainAccounts.Service.markDirty(confirmSellerBid.from)
        val markDirtyFromAddressInBlockchainTransactionFeedbacks = blockchainTransactionFeedbacks.Service.markDirty(confirmSellerBid.from)
        val markDirtyToAddressInBlockchainTransactionFeedbacks = blockchainTransactionFeedbacks.Service.markDirty(confirmSellerBid.to)
        for {
          _ <- markDirtyFromAddressBlockchainAccounts
          _ <- markDirtyFromAddressInBlockchainTransactionFeedbacks
          _ <- markDirtyToAddressInBlockchainTransactionFeedbacks
        } yield {}
      }

      def getIDs(confirmSellerBid: ConfirmSellerBid) = {
        val toAccountID = masterAccounts.Service.getId(confirmSellerBid.to)
        val fromAccountID = masterAccounts.Service.getId(confirmSellerBid.from)
        for {
          toAccountID <- toAccountID
          fromAccountID <- fromAccountID
        } yield (toAccountID, fromAccountID)
      }

      (for {
        _ <- markTransactionSuccessful
        confirmSellerBid <- confirmSellerBid
        negotiationID <- negotiationID(confirmSellerBid)
        negotiationResponse <- negotiationResponse(negotiationID, confirmSellerBid)
        _ <- insertOrUpdate(negotiationResponse)
        _ <- markDirty(confirmSellerBid)
        (toAccountID, fromAccountID) <- getIDs(confirmSellerBid)
      } yield {
        utilitiesNotification.send(fromAccountID, constants.Notification.SUCCESS, blockResponse.txhash)
        utilitiesNotification.send(toAccountID, constants.Notification.SUCCESS, blockResponse.txhash)
      }).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
        case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = {

      val markTransactionFailed = Service.markTransactionFailed(ticketID, message)
      val confirmSellerBid = Service.getTransaction(ticketID)

      def markDirty(confirmSellerBid: ConfirmSellerBid) = {
        val markDirtyFromAddressInBlockchainTransactionFeedbacks = blockchainTransactionFeedbacks.Service.markDirty(confirmSellerBid.from)
        val markDirtyToAddressInBlockchainTransactionFeedbacks = blockchainTransactionFeedbacks.Service.markDirty(confirmSellerBid.to)
        for {
          _ <- markDirtyFromAddressInBlockchainTransactionFeedbacks
          _ <- markDirtyToAddressInBlockchainTransactionFeedbacks
        } yield {}
      }

      def getIDs(confirmSellerBid: ConfirmSellerBid) = {
        val toAccountID = masterAccounts.Service.getId(confirmSellerBid.to)
        val fromAccountID = masterAccounts.Service.getId(confirmSellerBid.from)
        for {
          toAccountID <- toAccountID
          fromAccountID <- fromAccountID
        } yield (toAccountID, fromAccountID)
      }

      (for {
        _ <- markTransactionFailed
        confirmSellerBid <- confirmSellerBid
        _ <- markDirty(confirmSellerBid)
        (toAccountID, fromAccountID) <- getIDs(confirmSellerBid)
      } yield {
        utilitiesNotification.send(fromAccountID, constants.Notification.FAILURE, message)
        utilitiesNotification.send(toAccountID, constants.Notification.FAILURE, message)
      }).recover {
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