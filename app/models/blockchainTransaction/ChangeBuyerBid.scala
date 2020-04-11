package models.blockchainTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.master.{Negotiation, Negotiations}
import models.{blockchain, master, masterTransaction}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.NegotiationResponse
import queries.{GetNegotiation, GetNegotiationID}
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse
import java.net.ConnectException
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ChangeBuyerBid(from: String, to: String, bid: Int, time: Int, pegHash: String, gas: Int, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None) extends BaseTransaction[ChangeBuyerBid] {
  def mutateTicketID(newTicketID: String): ChangeBuyerBid = ChangeBuyerBid(from = from, to = to, bid = bid, time = time, pegHash = pegHash, gas = gas, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}


@Singleton
class ChangeBuyerBids @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks, getNegotiation: GetNegotiation, getNegotiationID: GetNegotiationID, blockchainNegotiations: blockchain.Negotiations, transactionChangeBuyerBid: transactions.ChangeBuyerBid, utilitiesNotification: utilities.Notification, masterAccounts: master.Accounts, masterNegotiations: Negotiations, blockchainAccounts: blockchain.Accounts, masterTransactionTradeActivities: masterTransaction.TradeActivities)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_CHANGE_BUYER_BID

  private implicit val logger: Logger = Logger(this.getClass)
  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db
  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val changeBuyerBidTable = TableQuery[ChangeBuyerBidTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")
  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(changeBuyerBid: ChangeBuyerBid): Future[String] = db.run((changeBuyerBidTable returning changeBuyerBidTable.map(_.ticketID) += changeBuyerBid).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(changeBuyerBid: ChangeBuyerBid): Future[Int] = db.run(changeBuyerBidTable.insertOrUpdate(changeBuyerBid).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String): Future[ChangeBuyerBid] = db.run(changeBuyerBidTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(changeBuyerBidTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(changeBuyerBidTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(changeBuyerBidTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(changeBuyerBidTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(changeBuyerBidTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(changeBuyerBidTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByTicketID(ticketID: String) = db.run(changeBuyerBidTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class ChangeBuyerBidTable(tag: Tag) extends Table[ChangeBuyerBid](tag, "ChangeBuyerBid") {

    def * = (from, to, bid, time, pegHash, gas, status.?, txHash.?, ticketID, mode, code.?) <> (ChangeBuyerBid.tupled, ChangeBuyerBid.unapply)

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

    def create(changeBuyerBid: ChangeBuyerBid): Future[String] = add(ChangeBuyerBid(from = changeBuyerBid.from, to = changeBuyerBid.to, bid = changeBuyerBid.bid, time = changeBuyerBid.time, pegHash = changeBuyerBid.pegHash, gas = changeBuyerBid.gas, status = changeBuyerBid.status, txHash = changeBuyerBid.txHash, ticketID = changeBuyerBid.ticketID, mode = changeBuyerBid.mode, code = changeBuyerBid.code))

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[ChangeBuyerBid] = findByTicketID(ticketID)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val changeBuyerBid = Service.getTransaction(ticketID)
      val negotiation = masterNegotiations.Service.tryGetByTicketID(ticketID)

      def buyerAccountID(fromAddress: String): Future[String] = masterAccounts.Service.getId(fromAddress)

      def negotiationID(changeBuyerBid: ChangeBuyerBid): Future[String] = blockchainNegotiations.Service.getNegotiationID(buyerAddress = changeBuyerBid.from, sellerAddress = changeBuyerBid.to, pegHash = changeBuyerBid.pegHash)

      def negotiationResponse(negotiationID: String, changeBuyerBid: ChangeBuyerBid): Future[NegotiationResponse.Response] = if (negotiationID == "") {
        val negotiationIDResponse = getNegotiationID.Service.get(buyerAddress = changeBuyerBid.from, sellerAddress = changeBuyerBid.to, pegHash = changeBuyerBid.pegHash)

        def getBlockchainNegotiation(negotiationID: String): Future[NegotiationResponse.Response] = getNegotiation.Service.get(negotiationID)

        for {
          negotiationIDResponse <- negotiationIDResponse
          negotiation <- getBlockchainNegotiation(negotiationIDResponse.negotiationID)
        } yield negotiation
      } else getNegotiation.Service.get(negotiationID)

      def insertOrUpdate(negotiationResponse: NegotiationResponse.Response): Future[Int] = blockchainNegotiations.Service.insertOrUpdate(id = negotiationResponse.value.negotiationID, buyerAddress = negotiationResponse.value.buyerAddress, sellerAddress = negotiationResponse.value.sellerAddress, assetPegHash = negotiationResponse.value.pegHash, bid = negotiationResponse.value.bid, time = negotiationResponse.value.time, buyerSignature = negotiationResponse.value.buyerSignature, sellerSignature = negotiationResponse.value.sellerSignature, buyerBlockHeight = negotiationResponse.value.buyerBlockHeight, sellerBlockHeight = negotiationResponse.value.sellerBlockHeight, buyerContractHash = negotiationResponse.value.buyerContractHash, sellerContractHash = negotiationResponse.value.sellerContractHash, dirtyBit = true)

      def markNegotiationAcceptedAndUpdateNegotiationID(negotiation: Negotiation, negotiationID: String): Future[Int] = if (negotiation.status == constants.Status.Negotiation.REQUEST_SENT) {
        masterNegotiations.Service.markAcceptedAndUpdateNegotiationID(id = negotiation.id, negotiationID = negotiationID)
      } else {
        Future(0)
      }

      def updatePriceAndQuantity(id: String, price: Int, quantity: Int): Future[Int] = masterNegotiations.Service.updatePriceAndQuantity(id = id, price = price, quantity = quantity)

      def markDirty(changeBuyerBid: ChangeBuyerBid): Future[Unit] = {
        val markDirtyFromAddressBlockchainAccounts = blockchainAccounts.Service.markDirty(changeBuyerBid.from)
        val markDirtyFromAddressInBlockchainTransactionFeedbacks = blockchainTransactionFeedbacks.Service.markDirty(changeBuyerBid.from)
        val markDirtyToAddressInBlockchainTransactionFeedbacks = blockchainTransactionFeedbacks.Service.markDirty(changeBuyerBid.to)
        for {
          _ <- markDirtyFromAddressBlockchainAccounts
          _ <- markDirtyFromAddressInBlockchainTransactionFeedbacks
          _ <- markDirtyToAddressInBlockchainTransactionFeedbacks
        } yield Unit
      }

      def getID(address: String): Future[String] = masterAccounts.Service.getId(address)

      def sendNegotiationRequestAcceptedNotifications(fromAccountID: String, toAccountID: String, negotiation: Negotiation): Future[Unit] = {
        if (negotiation.status == constants.Status.Negotiation.REQUEST_SENT) {
          for {
            _ <- utilitiesNotification.send(fromAccountID, constants.Notification.NEGOTIATION_ACCEPTED, negotiation.id)
            _ <- utilitiesNotification.send(toAccountID, constants.Notification.NEGOTIATION_ACCEPTED, negotiation.id)
            _ <- masterTransactionTradeActivities.Service.insert(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.NEGOTIATION_STARTED, negotiation.buyerTraderID, negotiation.sellerTraderID, negotiation.assetDescription)
            _ <- masterTransactionTradeActivities.Service.insert(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.NEGOTIATION_STARTED, negotiation.buyerTraderID, negotiation.sellerTraderID, negotiation.assetDescription)
            _ <- masterTransactionTradeActivities.Service.insert(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.NEGOTIATION_STARTED, negotiation.buyerTraderID, negotiation.sellerTraderID, negotiation.assetDescription)
            _ <- masterTransactionTradeActivities.Service.insert(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.NEGOTIATION_STARTED, negotiation.buyerTraderID, negotiation.sellerTraderID, negotiation.assetDescription)
            _ <- masterTransactionTradeActivities.Service.insert(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.NEGOTIATION_STARTED, negotiation.buyerTraderID, negotiation.sellerTraderID, negotiation.assetDescription)
            _ <- masterTransactionTradeActivities.Service.insert(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.NEGOTIATION_STARTED, negotiation.buyerTraderID, negotiation.sellerTraderID, negotiation.assetDescription)
            _ <- masterTransactionTradeActivities.Service.insert(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.NEGOTIATION_STARTED, negotiation.buyerTraderID, negotiation.sellerTraderID, negotiation.assetDescription)
            _ <- masterTransactionTradeActivities.Service.insert(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.NEGOTIATION_STARTED, negotiation.buyerTraderID, negotiation.sellerTraderID, negotiation.assetDescription)
            _ <- masterTransactionTradeActivities.Service.insert(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.NEGOTIATION_STARTED, negotiation.buyerTraderID, negotiation.sellerTraderID, negotiation.assetDescription)
            _ <- masterTransactionTradeActivities.Service.insert(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.NEGOTIATION_STARTED, negotiation.buyerTraderID, negotiation.sellerTraderID, negotiation.assetDescription)
            _ <- masterTransactionTradeActivities.Service.insert(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.NEGOTIATION_STARTED, negotiation.buyerTraderID, negotiation.sellerTraderID, negotiation.assetDescription)
            _ <- masterTransactionTradeActivities.Service.insert(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.NEGOTIATION_STARTED, negotiation.buyerTraderID, negotiation.sellerTraderID, negotiation.assetDescription)
            _ <- masterTransactionTradeActivities.Service.insert(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.NEGOTIATION_STARTED, negotiation.buyerTraderID, negotiation.sellerTraderID, negotiation.assetDescription)
            _ <- masterTransactionTradeActivities.Service.insert(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.NEGOTIATION_STARTED, negotiation.buyerTraderID, negotiation.sellerTraderID, negotiation.assetDescription)
            _ <- masterTransactionTradeActivities.Service.insert(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.NEGOTIATION_STARTED, negotiation.buyerTraderID, negotiation.sellerTraderID, negotiation.assetDescription)
            _ <- masterTransactionTradeActivities.Service.insert(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.NEGOTIATION_STARTED, negotiation.buyerTraderID, negotiation.sellerTraderID, negotiation.assetDescription)
            _ <- masterTransactionTradeActivities.Service.insert(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.NEGOTIATION_STARTED, negotiation.buyerTraderID, negotiation.sellerTraderID, negotiation.assetDescription)
          } yield Unit
        }
        Future(Unit)
      }

      (for {
        _ <- markTransactionSuccessful
        changeBuyerBid <- changeBuyerBid
        negotiation <- negotiation
        negotiationID <- negotiationID(changeBuyerBid)
        negotiationResponse <- negotiationResponse(negotiationID, changeBuyerBid)
        _ <- insertOrUpdate(negotiationResponse)
        _ <- markNegotiationAcceptedAndUpdateNegotiationID(negotiation = negotiation, negotiationID = negotiationResponse.value.negotiationID)
        _ <- updatePriceAndQuantity(id = negotiation.id, price = negotiationResponse.value.bid.toInt, quantity = negotiation.quantity) //TODO Change quantity = negotiation.quantity if in future comes from blockchain
        _ <- markDirty(changeBuyerBid)
        fromAccountID <- getID(changeBuyerBid.from)
        toAccountID <- getID(changeBuyerBid.to)
        _ <- utilitiesNotification.send(fromAccountID, constants.Notification.NEGOTIATION_UPDATED, blockResponse.txhash)
        _ <- utilitiesNotification.send(toAccountID, constants.Notification.NEGOTIATION_UPDATED, blockResponse.txhash)
        _ <- sendNegotiationRequestAcceptedNotifications(fromAccountID = fromAccountID, toAccountID = toAccountID, negotiation = negotiation)
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
        case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = {
      val markTransactionFailed = Service.markTransactionFailed(ticketID, message)
      val changeBuyerBid = Service.getTransaction(ticketID)

      def markDirty(changeBuyerBid: ChangeBuyerBid): Future[Unit] = {
        val markDirtyFromAddressInBlockchainTransactionFeedbacks = blockchainTransactionFeedbacks.Service.markDirty(changeBuyerBid.from)
        val markDirtyToAddressInBlockchainTransactionFeedbacks = blockchainTransactionFeedbacks.Service.markDirty(changeBuyerBid.to)
        for {
          _ <- markDirtyFromAddressInBlockchainTransactionFeedbacks
          _ <- markDirtyToAddressInBlockchainTransactionFeedbacks
        } yield Unit
      }

      def getID(address: String): Future[String] = masterAccounts.Service.getId(address)

      (for {
        _ <- markTransactionFailed
        changeBuyerBid <- changeBuyerBid
        _ <- markDirty(changeBuyerBid)
        fromAccountID <- getID(changeBuyerBid.from)
        toAccountID <- getID(changeBuyerBid.to)
        _ <- utilitiesNotification.send(toAccountID, constants.Notification.NEGOTIATION_UPDATE_FAILED, message)
        _ <- utilitiesNotification.send(fromAccountID, constants.Notification.NEGOTIATION_UPDATE_FAILED, message)
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