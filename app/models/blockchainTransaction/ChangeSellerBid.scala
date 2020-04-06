package models.blockchainTransaction

import java.net.ConnectException

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.{blockchain, master, masterTransaction}
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

case class ChangeSellerBid(from: String, to: String, bid: Int, time: Int, pegHash: String, gas: Int, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None) extends BaseTransaction[ChangeSellerBid] {
  def mutateTicketID(newTicketID: String): ChangeSellerBid = ChangeSellerBid(from = from, to = to, bid = bid, time = time, pegHash = pegHash, gas = gas, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}


@Singleton
class ChangeSellerBids @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks, getNegotiation: GetNegotiation, getNegotiationID: GetNegotiationID, masterTransactionNegotiationRequests: masterTransaction.NegotiationRequests, blockchainNegotiations: blockchain.Negotiations, transactionChangeSellerBid: transactions.ChangeSellerBid, utilitiesNotification: utilities.Notification, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_CHANGE_SELLER_BID

  private implicit val logger: Logger = Logger(this.getClass)
  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db
  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val changeSellerBidTable = TableQuery[ChangeSellerBidTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")
  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(changeSellerBid: ChangeSellerBid): Future[String] = db.run((changeSellerBidTable returning changeSellerBidTable.map(_.ticketID) += changeSellerBid).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(changeSellerBid: ChangeSellerBid): Future[Int] = db.run(changeSellerBidTable.insertOrUpdate(changeSellerBid).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String): Future[ChangeSellerBid] = db.run(changeSellerBidTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(changeSellerBidTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(changeSellerBidTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(changeSellerBidTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(changeSellerBidTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(changeSellerBidTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(changeSellerBidTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByTicketID(ticketID: String) = db.run(changeSellerBidTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class ChangeSellerBidTable(tag: Tag) extends Table[ChangeSellerBid](tag, "ChangeSellerBid") {

    def * = (from, to, bid, time, pegHash, gas, status.?, txHash.?, ticketID, mode, code.?) <> (ChangeSellerBid.tupled, ChangeSellerBid.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def bid = column[Int]("bid")

    def time = column[Int]("time")

    def pegHash = column[String]("pegHash")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def mode = column[String]("mode")

    def code = column[String]("code")
  }

  object Service {

    def create(changeSellerBid: ChangeSellerBid): Future[String] = add(ChangeSellerBid(from = changeSellerBid.from, to = changeSellerBid.to, bid = changeSellerBid.bid, time = changeSellerBid.time, pegHash = changeSellerBid.pegHash, gas = changeSellerBid.gas, status = changeSellerBid.status, txHash = changeSellerBid.txHash, ticketID = changeSellerBid.ticketID, mode = changeSellerBid.mode, code = changeSellerBid.code))

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[ChangeSellerBid] = findByTicketID(ticketID)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))
  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val changeSellerBid = Service.getTransaction(ticketID)

      def negotiationID(changeSellerBid: ChangeSellerBid): Future[String] = blockchainNegotiations.Service.getNegotiationID(buyerAddress = changeSellerBid.to, sellerAddress = changeSellerBid.from, pegHash = changeSellerBid.pegHash)

      def negotiationResponse(negotiationID: String, changeSellerBid: ChangeSellerBid): Future[NegotiationResponse.Response] = if (negotiationID == "") {
        val negotiationIDResponse = getNegotiationID.Service.get(buyerAddress = changeSellerBid.to, sellerAddress = changeSellerBid.from, pegHash = changeSellerBid.pegHash)

        def getBlockchainNegotiation(negotiationID: String): Future[NegotiationResponse.Response] = getNegotiation.Service.get(negotiationID)

        for {
          negotiationIDResponse <- negotiationIDResponse
          negotiation <- getBlockchainNegotiation(negotiationIDResponse.negotiationID)
        } yield negotiation
      } else getNegotiation.Service.get(negotiationID)

      def insertOrUpdate(negotiationResponse: NegotiationResponse.Response): Future[Int] = blockchainNegotiations.Service.insertOrUpdate(id = negotiationResponse.value.negotiationID, buyerAddress = negotiationResponse.value.buyerAddress, sellerAddress = negotiationResponse.value.sellerAddress, assetPegHash = negotiationResponse.value.pegHash, bid = negotiationResponse.value.bid, time = negotiationResponse.value.time, buyerSignature = negotiationResponse.value.buyerSignature, sellerSignature = negotiationResponse.value.sellerSignature, buyerBlockHeight = negotiationResponse.value.buyerBlockHeight, sellerBlockHeight = negotiationResponse.value.sellerBlockHeight, buyerContractHash = negotiationResponse.value.buyerContractHash, sellerContractHash = negotiationResponse.value.sellerContractHash, dirtyBit = true)

      def updateAmountForNegotiationID(negotiationResponse: NegotiationResponse.Response, bid: Int): Future[Int] = masterTransactionNegotiationRequests.Service.updateAmountForNegotiationID(negotiationResponse.value.negotiationID, bid)

      def markDirty(changeSellerBid: ChangeSellerBid): Future[Unit] = {
        val markDirtyFromAddressBlockchainAccounts = blockchainAccounts.Service.markDirty(changeSellerBid.from)
        val markDirtyFromAddressInBlockchainTransactionFeedbacks = blockchainTransactionFeedbacks.Service.markDirty(changeSellerBid.from)
        val markDirtyToAddressInBlockchainTransactionFeedbacks = blockchainTransactionFeedbacks.Service.markDirty(changeSellerBid.to)
        for {
          _ <- markDirtyFromAddressBlockchainAccounts
          _ <- markDirtyFromAddressInBlockchainTransactionFeedbacks
          _ <- markDirtyToAddressInBlockchainTransactionFeedbacks
        } yield Unit
      }

      def getIDs(changeSellerBid: ChangeSellerBid): Future[(String, String)] = {
        val toAccountID = masterAccounts.Service.getId(changeSellerBid.to)
        val fromAccountID = masterAccounts.Service.getId(changeSellerBid.from)
        for {
          toAccountID <- toAccountID
          fromAccountID <- fromAccountID
        } yield (toAccountID, fromAccountID)
      }

      (for {
        _ <- markTransactionSuccessful
        changeSellerBid <- changeSellerBid
        negotiationID <- negotiationID(changeSellerBid)
        negotiationResponse <- negotiationResponse(negotiationID, changeSellerBid)
        _ <- insertOrUpdate(negotiationResponse)
        _ <- updateAmountForNegotiationID(negotiationResponse, changeSellerBid.bid)
        _ <- markDirty(changeSellerBid)
        (toAccountID, fromAccountID) <- getIDs(changeSellerBid)
        _ <- utilitiesNotification.send(fromAccountID, constants.Notification.SUCCESS, blockResponse.txhash)
        _ <- utilitiesNotification.send(toAccountID, constants.Notification.SUCCESS, blockResponse.txhash)
      } yield {}).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
        case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = {
      val markTransactionFailed = Service.markTransactionFailed(ticketID, message)
      val changeSellerBid = Service.getTransaction(ticketID)

      def markDirty(changeSellerBid: ChangeSellerBid): Future[Unit] = {
        val markDirtyFromAddressInBlockchainTransactionFeedbacks = blockchainTransactionFeedbacks.Service.markDirty(changeSellerBid.from)
        val markDirtyToAddressInBlockchainTransactionFeedbacks = blockchainTransactionFeedbacks.Service.markDirty(changeSellerBid.to)
        for {
          _ <- markDirtyFromAddressInBlockchainTransactionFeedbacks
          _ <- markDirtyToAddressInBlockchainTransactionFeedbacks
        } yield {}
      }

      def getIDs(changeSellerBid: ChangeSellerBid): Future[(String, String)] = {
        val toAccountID = masterAccounts.Service.getId(changeSellerBid.to)
        val fromAccountID = masterAccounts.Service.getId(changeSellerBid.from)
        for {
          toAccountID <- toAccountID
          fromAccountID <- fromAccountID
        } yield (toAccountID, fromAccountID)
      }

      (for {
        _ <- markTransactionFailed
        changeSellerBid <- changeSellerBid
        _ <- markDirty(changeSellerBid)
        (toAccountID, fromAccountID) <- getIDs(changeSellerBid)
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