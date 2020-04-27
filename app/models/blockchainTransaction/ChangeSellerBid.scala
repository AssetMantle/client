package models.blockchainTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.master.{Negotiation => masterNegotiation, Organization, Trader}
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

import models.blockchain.Negotiation

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ChangeSellerBid(from: String, to: String, bid: Int, time: Int, pegHash: String, gas: Int, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None) extends BaseTransaction[ChangeSellerBid] {
  def mutateTicketID(newTicketID: String): ChangeSellerBid = ChangeSellerBid(from = from, to = to, bid = bid, time = time, pegHash = pegHash, gas = gas, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}

@Singleton
class ChangeSellerBids @Inject()(
                                  actorSystem: ActorSystem,
                                  transaction: utilities.Transaction,
                                  protected val databaseConfigProvider: DatabaseConfigProvider,
                                  blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks,
                                  getNegotiation: GetNegotiation,
                                  getNegotiationID: GetNegotiationID,
                                  masterNegotiations: master.Negotiations,
                                  blockchainNegotiations: blockchain.Negotiations,
                                  utilitiesNotification: utilities.Notification,
                                  masterAccounts: master.Accounts,
                                  masterAssets: master.Assets,
                                  masterTraders: master.Traders,
                                  masterOrganizations: master.Organizations,
                                  blockchainAccounts: blockchain.Accounts
                                )(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_CHANGE_SELLER_BID

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val changeSellerBidTable = TableQuery[ChangeSellerBidTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

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

  private def updateStatusByTicketID(ticketID: String, status: Option[Boolean]): Future[Int] = db.run(changeSellerBidTable.filter(_.ticketID === ticketID).map(_.status.?).update(status).asTry).map {
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

    def resetTransactionStatus(ticketID: String): Future[Int] = updateStatusByTicketID(ticketID, status = null)

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

      def negotiation(changeSellerBid: ChangeSellerBid): Future[Option[Negotiation]] = blockchainNegotiations.Service.getNegotiation(buyerAddress = changeSellerBid.from, sellerAddress = changeSellerBid.to, pegHash = changeSellerBid.pegHash)

      def negotiationResponse(negotiation: Option[Negotiation], changeSellerBid: ChangeSellerBid): Future[NegotiationResponse.Response] = negotiation match {
        case Some(negotiation) => getNegotiation.Service.get(negotiation.id)
        case None =>
          val negotiationIDResponse = getNegotiationID.Service.get(buyerAddress = changeSellerBid.from, sellerAddress = changeSellerBid.to, pegHash = changeSellerBid.pegHash)

          def getBlockchainNegotiation(negotiationID: String): Future[NegotiationResponse.Response] = getNegotiation.Service.get(negotiationID)

          for {
            negotiationIDResponse <- negotiationIDResponse
            negotiationResponse <- getBlockchainNegotiation(negotiationIDResponse.negotiationID)
          } yield negotiationResponse
      }

      def insertOrUpdate(negotiation: Option[Negotiation], negotiationResponse: NegotiationResponse.Response) = {
        if (negotiation.isDefined) {
          blockchainNegotiations.Service.update(id = negotiationResponse.value.negotiationID, buyerAddress = negotiationResponse.value.buyerAddress, sellerAddress = negotiationResponse.value.sellerAddress, assetPegHash = negotiationResponse.value.pegHash, bid = negotiationResponse.value.bid, time = negotiationResponse.value.time, buyerSignature = negotiationResponse.value.buyerSignature, sellerSignature = negotiationResponse.value.sellerSignature, buyerBlockHeight = negotiationResponse.value.buyerBlockHeight, sellerBlockHeight = negotiationResponse.value.sellerBlockHeight, buyerContractHash = negotiationResponse.value.buyerContractHash, sellerContractHash = negotiationResponse.value.sellerContractHash, dirtyBit = false)
        } else blockchainNegotiations.Service.create(id = negotiationResponse.value.negotiationID, buyerAddress = negotiationResponse.value.buyerAddress, sellerAddress = negotiationResponse.value.sellerAddress, assetPegHash = negotiationResponse.value.pegHash, bid = negotiationResponse.value.bid, time = negotiationResponse.value.time, buyerSignature = negotiationResponse.value.buyerSignature, sellerSignature = negotiationResponse.value.sellerSignature, buyerBlockHeight = negotiationResponse.value.buyerBlockHeight, sellerBlockHeight = negotiationResponse.value.sellerBlockHeight, buyerContractHash = negotiationResponse.value.buyerContractHash, sellerContractHash = negotiationResponse.value.sellerContractHash, dirtyBit = false)
      }

      def markDirty(changeSellerBid: ChangeSellerBid): Future[Unit] = {
        val markSellerAccountDirty = blockchainAccounts.Service.markDirty(changeSellerBid.from)
        val markSellerFeedbackDirty = blockchainTransactionFeedbacks.Service.markDirty(changeSellerBid.from)
        for {
          _ <- markSellerAccountDirty
          _ <- markSellerFeedbackDirty
        } yield ()
      }

      def getID(address: String): Future[String] = masterAccounts.Service.getId(address)

      def getTrader(accountID: String): Future[Trader] = masterTraders.Service.tryGetByAccountID(accountID)

      def getAssetID(pegHash: String): Future[String] = masterAssets.Service.tryGetIDByPegHash(pegHash)

      def getMasterNegotiation(buyerTraderID: String, sellerTraderID: String, assetID: String): Future[masterNegotiation] = masterNegotiations.Service.tryGetByBuyerSellerTraderIDAndAssetID(buyerTraderID = buyerTraderID, sellerTraderID = sellerTraderID, assetID = assetID)

      //TODO If in future BC provides quantity and assetDescription in Negotiation, modify this to update them as well.
      def updateMasterNegotiation(negotiation: masterNegotiation, negotiationID: String, price: Int, time: Int): Future[Int] = if (negotiation.status == constants.Status.Negotiation.REQUEST_SENT) {
        masterNegotiations.Service.update(negotiation.copy(negotiationID = Option(negotiationID), assetAndBuyerAccepted = negotiation.assetAndBuyerAccepted.copy(price = price), time = Option(time), status = constants.Status.Negotiation.STARTED))
      } else {
        masterNegotiations.Service.update(negotiation.copy(assetAndBuyerAccepted = negotiation.assetAndBuyerAccepted.copy(price = price), time = Option(time)))
      }

      def sendNotifications(buyer: Trader, seller: Trader, negotiation: masterNegotiation): Future[Unit] = {
        def getOrganization(organizationID: String): Future[Organization] = masterOrganizations.Service.tryGet(organizationID)

        if (negotiation.status == constants.Status.Negotiation.REQUEST_SENT) {
          for {
            _ <- utilitiesNotification.send(buyer.accountID, constants.Notification.NEGOTIATION_ACCEPTED, negotiation.id, negotiation.assetAndBuyerAccepted.assetDescription)
            _ <- utilitiesNotification.send(seller.accountID, constants.Notification.NEGOTIATION_ACCEPTED, negotiation.id, negotiation.assetAndBuyerAccepted.assetDescription)
            buyerOrganization <- getOrganization(buyer.organizationID)
            sellerOrganization <- getOrganization(seller.organizationID)
            _ <- utilitiesNotification.send(buyerOrganization.accountID, constants.Notification.ORGANIZATION_NOTIFY_NEGOTIATION_STARTED, negotiation.id, negotiation.assetAndBuyerAccepted.assetDescription, seller.name, buyer.name, sellerOrganization.name)
            _ <- utilitiesNotification.send(sellerOrganization.accountID, constants.Notification.ORGANIZATION_NOTIFY_NEGOTIATION_STARTED, negotiation.id, negotiation.assetAndBuyerAccepted.assetDescription, seller.name, buyer.name, buyerOrganization.name)
          } yield Unit
        } else {
          for {
            _ <- utilitiesNotification.send(buyer.accountID, constants.Notification.NEGOTIATION_UPDATED, blockResponse.txhash, negotiation.id, negotiation.assetAndBuyerAccepted.assetDescription)
            _ <- utilitiesNotification.send(seller.accountID, constants.Notification.NEGOTIATION_UPDATED, blockResponse.txhash, negotiation.id, negotiation.assetAndBuyerAccepted.assetDescription)
          } yield Unit
        }
      }

      (for {
        _ <- markTransactionSuccessful
        changeSellerBid <- changeSellerBid
        negotiation <- negotiation(changeSellerBid)
        negotiationResponse <- negotiationResponse(negotiation, changeSellerBid)
        _ <- insertOrUpdate(negotiationResponse)
        _ <- markDirty(changeSellerBid)
        buyerAccountID <- getID(changeSellerBid.to)
        sellerAccountID <- getID(changeSellerBid.from)
        buyer <- getTrader(buyerAccountID)
        seller <- getTrader(sellerAccountID)
        assetID <- getAssetID(negotiationResponse.value.pegHash)
        masterNegotiation <- getMasterNegotiation(buyerTraderID = buyer.id, sellerTraderID = seller.id, assetID = assetID)
        _ <- updateMasterNegotiation(negotiation = masterNegotiation, negotiationID = negotiationResponse.value.negotiationID, price = negotiationResponse.value.bid.toInt, time = negotiationResponse.value.time.toInt)
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
      val changeSellerBid = Service.getTransaction(ticketID)

      def markDirty(changeSellerBid: ChangeSellerBid): Future[Int] = blockchainTransactionFeedbacks.Service.markDirty(changeSellerBid.from)

      def getAccountID(address: String): Future[String] = masterAccounts.Service.getId(address)

      (for {
        _ <- markTransactionFailed
        changeSellerBid <- changeSellerBid
        _ <- markDirty(changeSellerBid)
        fromAccountID <- getAccountID(changeSellerBid.from)
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