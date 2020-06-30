package models.blockchainTransaction

import java.sql.Timestamp

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.Trait.Logged
import models.blockchain.Negotiation
import models.master.{Organization, Trader, Negotiation => masterNegotiation}
import models.{blockchain, master, masterTransaction}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.NegotiationResponse
import queries.{GetNegotiation, GetNegotiationID}
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse
import utilities.MicroLong

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ChangeBuyerBid(from: String, to: String, bid: MicroLong, time: Int, pegHash: String, gas: Int, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends BaseTransaction[ChangeBuyerBid] with Logged {
  def mutateTicketID(newTicketID: String): ChangeBuyerBid = ChangeBuyerBid(from = from, to = to, bid = bid, time = time, pegHash = pegHash, gas = gas, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}

@Singleton
class ChangeBuyerBids @Inject()(actorSystem: ActorSystem,
                                transaction: utilities.Transaction,
                                protected val databaseConfigProvider: DatabaseConfigProvider,
                                blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks,
                                getNegotiation: GetNegotiation,
                                getNegotiationID: GetNegotiationID,
                                blockchainNegotiations: blockchain.Negotiations,
                                utilitiesNotification: utilities.Notification,
                                masterAccounts: master.Accounts,
                                masterNegotiations: master.Negotiations,
                                masterAssets: master.Assets,
                                masterTraders: master.Traders,
                                masterOrganizations: master.Organizations,
                                blockchainAccounts: blockchain.Accounts,
                                masterTransactionTradeActivities: masterTransaction.TradeActivities
                               )(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {


  def serialize(changeBuyerBid: ChangeBuyerBid): ChangeBuyerBidSerialized = ChangeBuyerBidSerialized(from = changeBuyerBid.from, to = changeBuyerBid.to, bid = changeBuyerBid.bid.value, time = changeBuyerBid.time,pegHash = changeBuyerBid.pegHash, gas = changeBuyerBid.gas, status = changeBuyerBid.status, txHash = changeBuyerBid.txHash, ticketID = changeBuyerBid.ticketID, mode = changeBuyerBid.mode, code = changeBuyerBid.code, createdBy = changeBuyerBid.createdBy, createdOn = changeBuyerBid.createdOn, createdOnTimeZone = changeBuyerBid.createdOnTimeZone, updatedBy = changeBuyerBid.updatedBy, updatedOn = changeBuyerBid.updatedOn, updatedOnTimeZone = changeBuyerBid.updatedOnTimeZone)

  case class ChangeBuyerBidSerialized(from: String, to: String, bid: Long, time: Int, pegHash: String, gas: Int, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize(): ChangeBuyerBid = ChangeBuyerBid(from = from, to = to, bid = new MicroLong(bid), time =time,pegHash=pegHash, gas = gas, status = status, txHash = txHash, ticketID = ticketID, mode = mode, code = code, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_CHANGE_BUYER_BID

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val changeBuyerBidTable = TableQuery[ChangeBuyerBidTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(changeBuyerBidSerialized: ChangeBuyerBidSerialized): Future[String] = db.run((changeBuyerBidTable returning changeBuyerBidTable.map(_.ticketID) += changeBuyerBidSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByTicketID(ticketID: String): Future[ChangeBuyerBidSerialized] = db.run(changeBuyerBidTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(changeBuyerBidTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(changeBuyerBidTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(changeBuyerBidTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(changeBuyerBidTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(changeBuyerBidTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(changeBuyerBidTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusByTicketID(ticketID: String, status: Option[Boolean]): Future[Int] = db.run(changeBuyerBidTable.filter(_.ticketID === ticketID).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByTicketID(ticketID: String) = db.run(changeBuyerBidTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class ChangeBuyerBidTable(tag: Tag) extends Table[ChangeBuyerBidSerialized](tag, "ChangeBuyerBid") {

    def * = (from, to, bid, time, pegHash, gas, status.?, txHash.?, ticketID, mode, code.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (ChangeBuyerBidSerialized.tupled, ChangeBuyerBidSerialized.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def bid = column[Long]("bid")

    def time = column[Int]("time")

    def pegHash = column[String]("pegHash")

    def gas = column[Int]("gas")

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

    def create(changeBuyerBid: ChangeBuyerBid): Future[String] = add(serialize(ChangeBuyerBid(from = changeBuyerBid.from, to = changeBuyerBid.to, bid = changeBuyerBid.bid, time = changeBuyerBid.time, pegHash = changeBuyerBid.pegHash, gas = changeBuyerBid.gas, status = changeBuyerBid.status, txHash = changeBuyerBid.txHash, ticketID = changeBuyerBid.ticketID, mode = changeBuyerBid.mode, code = changeBuyerBid.code)))

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def resetTransactionStatus(ticketID: String): Future[Int] = updateStatusByTicketID(ticketID, status = null)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[ChangeBuyerBid] = findByTicketID(ticketID).map(_.deserialize())

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val changeBuyerBid = Service.getTransaction(ticketID)

      def negotiation(changeBuyerBid: ChangeBuyerBid): Future[Option[Negotiation]] = blockchainNegotiations.Service.getNegotiation(buyerAddress = changeBuyerBid.from, sellerAddress = changeBuyerBid.to, pegHash = changeBuyerBid.pegHash)

      def getNegotiationResponse(negotiation: Option[Negotiation], changeBuyerBid: ChangeBuyerBid): Future[NegotiationResponse.Response] = negotiation match {
        case Some(negotiation) => getNegotiation.Service.get(negotiation.id)
        case None =>
          val negotiationIDResponse = getNegotiationID.Service.get(buyerAddress = changeBuyerBid.from, sellerAddress = changeBuyerBid.to, pegHash = changeBuyerBid.pegHash)

          def getBlockchainNegotiation(negotiationID: String): Future[NegotiationResponse.Response] = getNegotiation.Service.get(negotiationID)

          for {
            negotiationIDResponse <- negotiationIDResponse
            negotiationResponse <- getBlockchainNegotiation(negotiationIDResponse.negotiationID)
          } yield negotiationResponse
      }

      def createOrUpdate(negotiation: Option[Negotiation], negotiationResponse: NegotiationResponse.Response) = {
        if (negotiation.isDefined) {
          blockchainNegotiations.Service.update(id = negotiationResponse.value.negotiationID, buyerAddress = negotiationResponse.value.buyerAddress, sellerAddress = negotiationResponse.value.sellerAddress, assetPegHash = negotiationResponse.value.pegHash, bid = new MicroLong(negotiationResponse.value.bid), time = negotiationResponse.value.time, buyerSignature = negotiationResponse.value.buyerSignature, sellerSignature = negotiationResponse.value.sellerSignature, buyerBlockHeight = negotiationResponse.value.buyerBlockHeight, sellerBlockHeight = negotiationResponse.value.sellerBlockHeight, buyerContractHash = negotiationResponse.value.buyerContractHash, sellerContractHash = negotiationResponse.value.sellerContractHash, dirtyBit = false)
        } else blockchainNegotiations.Service.create(id = negotiationResponse.value.negotiationID, buyerAddress = negotiationResponse.value.buyerAddress, sellerAddress = negotiationResponse.value.sellerAddress, assetPegHash = negotiationResponse.value.pegHash, bid = new MicroLong(negotiationResponse.value.bid), time = negotiationResponse.value.time, buyerSignature = negotiationResponse.value.buyerSignature, sellerSignature = negotiationResponse.value.sellerSignature, buyerBlockHeight = negotiationResponse.value.buyerBlockHeight, sellerBlockHeight = negotiationResponse.value.sellerBlockHeight, buyerContractHash = negotiationResponse.value.buyerContractHash, sellerContractHash = negotiationResponse.value.sellerContractHash, dirtyBit = false)
      }

      def markDirty(changeBuyerBid: ChangeBuyerBid): Future[Unit] = {
        val markBuyerAccountDirty = blockchainAccounts.Service.markDirty(changeBuyerBid.from)
        val markBuyerFeedbackDirty = blockchainTransactionFeedbacks.Service.markDirty(changeBuyerBid.from)
        for {
          _ <- markBuyerAccountDirty
          _ <- markBuyerFeedbackDirty
        } yield ()
      }

      def getID(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

      def getTrader(accountID: String): Future[Trader] = masterTraders.Service.tryGetByAccountID(accountID)

      def getAssetID(pegHash: String): Future[String] = masterAssets.Service.tryGetIDByPegHash(pegHash)

      def getMasterNegotiation(buyerTraderID: String, sellerTraderID: String, assetID: String): Future[masterNegotiation] = masterNegotiations.Service.tryGetByBuyerSellerTraderIDAndAssetID(buyerTraderID = buyerTraderID, sellerTraderID = sellerTraderID, assetID = assetID)

      //TODO If in future BC provides quantity and assetDescription in Negotiation, modify this to update them as well.
      def updateMasterNegotiation(negotiation: masterNegotiation, negotiationID: String, price: MicroLong, time: Int): Future[Int] = if (negotiation.status == constants.Status.Negotiation.REQUEST_SENT) {
        masterNegotiations.Service.update(negotiation.copy(negotiationID = Option(negotiationID), price = price, time = Option(time), status = constants.Status.Negotiation.STARTED))
      } else {
        masterNegotiations.Service.update(negotiation.copy(price = price, time = Option(time)))
      }

      def sendNotifications(buyer: Trader, seller: Trader, negotiation: masterNegotiation): Future[Unit] = {
        def getOrganization(organizationID: String): Future[Organization] = masterOrganizations.Service.tryGet(organizationID)

        if (negotiation.status == constants.Status.Negotiation.REQUEST_SENT) {
          for {
            _ <- utilitiesNotification.send(buyer.accountID, constants.Notification.NEGOTIATION_ACCEPTED, negotiation.id, negotiation.assetDescription)
            _ <- utilitiesNotification.send(seller.accountID, constants.Notification.NEGOTIATION_ACCEPTED, negotiation.id, negotiation.assetDescription)
            buyerOrganization <- getOrganization(buyer.organizationID)
            sellerOrganization <- getOrganization(seller.organizationID)
            _ <- utilitiesNotification.send(buyerOrganization.accountID, constants.Notification.ORGANIZATION_NOTIFY_NEGOTIATION_STARTED, negotiation.id, negotiation.assetDescription, seller.accountID, buyer.accountID, sellerOrganization.name)
            _ <- utilitiesNotification.send(sellerOrganization.accountID, constants.Notification.ORGANIZATION_NOTIFY_NEGOTIATION_STARTED, negotiation.id, negotiation.assetDescription, seller.accountID, buyer.accountID, buyerOrganization.name)
          } yield ()
        } else {
          for {
            _ <- utilitiesNotification.send(buyer.accountID, constants.Notification.NEGOTIATION_UPDATED, blockResponse.txhash, negotiation.id, negotiation.assetDescription)
            _ <- utilitiesNotification.send(seller.accountID, constants.Notification.NEGOTIATION_UPDATED, blockResponse.txhash, negotiation.id, negotiation.assetDescription)
            _ <- masterTransactionTradeActivities.Service.create(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.NEGOTIATION_STARTED, negotiation.buyerTraderID, negotiation.sellerTraderID, negotiation.assetDescription)
          } yield ()
        }
      }

      (for {
        _ <- markTransactionSuccessful
        changeBuyerBid <- changeBuyerBid
        negotiation <- negotiation(changeBuyerBid)
        negotiationResponse <- getNegotiationResponse(negotiation, changeBuyerBid)
        _ <- createOrUpdate(negotiation, negotiationResponse)
        _ <- markDirty(changeBuyerBid)
        buyerAccountID <- getID(changeBuyerBid.from)
        sellerAccountID <- getID(changeBuyerBid.to)
        buyer <- getTrader(buyerAccountID)
        seller <- getTrader(sellerAccountID)
        assetID <- getAssetID(negotiationResponse.value.pegHash)
        masterNegotiation <- getMasterNegotiation(buyerTraderID = buyer.id, sellerTraderID = seller.id, assetID = assetID)
        _ <- updateMasterNegotiation(negotiation = masterNegotiation, negotiationID = negotiationResponse.value.negotiationID, price = new MicroLong(negotiationResponse.value.bid), time = negotiationResponse.value.time.toInt)
        _ <- sendNotifications(buyer = buyer, seller = seller, negotiation = masterNegotiation)
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
      val changeBuyerBid = Service.getTransaction(ticketID)

      def markDirty(changeBuyerBid: ChangeBuyerBid): Future[Int] = blockchainTransactionFeedbacks.Service.markDirty(changeBuyerBid.from)

      def getAccountID(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

      (for {
        _ <- markTransactionFailed
        changeBuyerBid <- changeBuyerBid
        _ <- markDirty(changeBuyerBid)
        fromAccountID <- getAccountID(changeBuyerBid.from)
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