package models.blockchainTransaction

import java.sql.Timestamp

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.Trait.Logged
import models.master.{Asset, Negotiation => masterNegotiation, Order => masterOrder}
import models.{blockchain, master, masterTransaction}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.GetOrder
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse
import utilities.MicroInt

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SendAsset(from: String, to: String, pegHash: String, gas: Int, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends BaseTransaction[SendAsset] with Logged {
  def mutateTicketID(newTicketID: String): SendAsset = SendAsset(from = from, to = to, pegHash = pegHash, gas = gas, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}

@Singleton
class SendAssets @Inject()(
                            actorSystem: ActorSystem,
                            blockchainAccounts: blockchain.Accounts,
                            blockchainAssets: blockchain.Assets,
                            blockchainOrders: blockchain.Orders,
                            blockchainNegotiations: blockchain.Negotiations,
                            blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks,
                            getOrder: GetOrder,
                            masterAccounts: master.Accounts,
                            masterAssets: master.Assets,
                            masterNegotiations: master.Negotiations,
                            masterOrders: master.Orders,
                            masterTransactionSendFiatRequests: masterTransaction.SendFiatRequests,
                            protected val databaseConfigProvider: DatabaseConfigProvider,
                            transaction: utilities.Transaction,
                            utilitiesNotification: utilities.Notification,
                          )(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SEND_ASSET

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val sendAssetTable = TableQuery[SendAssetTable]

  private implicit val assetWrites: OWrites[blockchain.Asset] = Json.writes[blockchain.Asset]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(sendAsset: SendAsset): Future[String] = db.run((sendAssetTable returning sendAssetTable.map(_.ticketID) += sendAsset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByTicketID(ticketID: String): Future[SendAsset] = db.run(sendAssetTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(sendAssetTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(sendAssetTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(sendAssetTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(sendAssetTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(sendAssetTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusByTicketID(ticketID: String, status: Option[Boolean]): Future[Int] = db.run(sendAssetTable.filter(_.ticketID === ticketID).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByTicketID(ticketID: String) = db.run(sendAssetTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(sendAssetTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class SendAssetTable(tag: Tag) extends Table[SendAsset](tag, "SendAsset") {

    def * = (from, to, pegHash, gas, status.?, txHash.?, ticketID, mode, code.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (SendAsset.tupled, SendAsset.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

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

    def create(sendAsset: SendAsset): Future[String] = add(SendAsset(from = sendAsset.from, to = sendAsset.to, pegHash = sendAsset.pegHash, gas = sendAsset.gas, status = sendAsset.status, txHash = sendAsset.txHash, ticketID = sendAsset.ticketID, mode = sendAsset.mode, code = sendAsset.code))

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def resetTransactionStatus(ticketID: String): Future[Int] = updateStatusByTicketID(ticketID, status = null)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[SendAsset] = findByTicketID(ticketID)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val sendAsset = Service.getTransaction(ticketID)

      def getNegotiationID(sendAsset: SendAsset): Future[String] = blockchainNegotiations.Service.tryGetID(buyerAddress = sendAsset.to, sellerAddress = sendAsset.from, pegHash = sendAsset.pegHash)

      def getMasterNegotiation(negotiationID: String): Future[masterNegotiation] = masterNegotiations.Service.tryGetByBCNegotiationID(negotiationID)

      def getMasterAsset(assetID: String): Future[Asset] = masterAssets.Service.tryGet(assetID)

      def markBCAssetSentToOrder(pegHash: String, negotiationID: String): Future[Int] = blockchainAssets.Service.markAssetSentToOrder(pegHash = pegHash, address = negotiationID)

      def markMasterAssetSendToOrder(pegHash: String, ownerID: String): Future[Int] = masterAssets.Service.markAssetSendToOrderByPegHash(pegHash = pegHash, ownerID = ownerID)

      def checkOrderExists(negotiationID: String): Future[Boolean] = blockchainOrders.Service.checkOrderExists(negotiationID)

      def createOrder(orderExists: Boolean, negotiationID: String, negotiation: masterNegotiation): Future[Unit] = if (!orderExists) {
        val bcOrderCreate = blockchainOrders.Service.create(id = negotiationID, awbProofHash = None, fiatProofHash = None)

        def masterOrderCreate(asset: Asset): Future[String] = masterOrders.Service.create(masterOrder(id = negotiation.id, orderID = negotiationID, status = if (asset.moderated) constants.Status.Order.ASSET_SENT_FIAT_PENDING else constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING))

        for {
          _ <- bcOrderCreate
          asset <- getMasterAsset(negotiation.assetID)
          _ <- masterOrderCreate(asset)
        } yield ()

      } else {
        val fiatsInOrder = masterTransactionSendFiatRequests.Service.getFiatsInOrder(negotiation.id)

        def status(fiatsInOrder: MicroInt): String = {
          if (fiatsInOrder.double >= negotiation.price.toDouble) constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING
          else constants.Status.Order.ASSET_SENT_FIAT_PENDING
        }

        def masterOrderUpdate(status: String): Future[Int] = masterOrders.Service.update(masterOrder(id = negotiation.id, orderID = negotiationID, status = status))

        for {
          fiatsInOrder <- fiatsInOrder
          _ <- masterOrderUpdate(status(fiatsInOrder))
        } yield ()
      }

      def markDirty(sendAsset: SendAsset): Future[Unit] = {
        val markDirtyBlockchainAccounts = blockchainAccounts.Service.markDirty(sendAsset.from)
        val markDirtyBlockchainTransactionFeedbacks = blockchainTransactionFeedbacks.Service.markDirty(sendAsset.from)
        for {
          _ <- markDirtyBlockchainAccounts
          _ <- markDirtyBlockchainTransactionFeedbacks
        } yield ()
      }

      def getAccountID(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

      (for {
        _ <- markTransactionSuccessful
        sendAsset <- sendAsset
        negotiationID <- getNegotiationID(sendAsset)
        masterNegotiation <- getMasterNegotiation(negotiationID)
        _ <- markBCAssetSentToOrder(pegHash = sendAsset.pegHash, negotiationID = negotiationID)
        _ <- markMasterAssetSendToOrder(pegHash = sendAsset.pegHash, ownerID = masterNegotiation.id)
        orderExists <- checkOrderExists(negotiationID)
        _ <- createOrder(orderExists = orderExists, negotiationID = negotiationID, negotiation = masterNegotiation)
        _ <- markDirty(sendAsset)
        fromAccountID <- getAccountID(sendAsset.from)
        toAccountID <- getAccountID(sendAsset.to)
        _ <- utilitiesNotification.send(fromAccountID, constants.Notification.SEND_ASSET_TO_ORDER_SUCCESSFUL, blockResponse.txhash)
        _ <- utilitiesNotification.send(toAccountID, constants.Notification.SEND_ASSET_TO_ORDER_SUCCESSFUL, blockResponse.txhash)
      } yield ()
        ).recover {
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
      val sendAsset = Service.getTransaction(ticketID)

      def address(sendAsset: SendAsset): Future[String] = blockchainAccounts.Service.tryGetAddress(sendAsset.from)

      def markDirty(address: String): Future[Int] = blockchainTransactionFeedbacks.Service.markDirty(address)

      def getAccountID(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

      (for {
        _ <- markTransactionFailed
        sendAsset <- sendAsset
        address <- address(sendAsset)
        _ <- markDirty(address)
        fromAccountID <- getAccountID(sendAsset.from)
        _ <- utilitiesNotification.send(fromAccountID, constants.Notification.SEND_ASSET_TO_ORDER_FAILED, message)
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