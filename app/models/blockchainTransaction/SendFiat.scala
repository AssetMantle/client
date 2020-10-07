package models.blockchainTransaction

import java.sql.Timestamp

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.Trait.Logged
import models.blockchain.Fiat
import models.master.{Negotiation => masterNegotiation, Order => masterOrder}
import models.{blockchain, master, masterTransaction}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.GetOrder
import queries.responses.OrderResponse
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse
import utilities.MicroNumber

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SendFiat(from: String, to: String, amount: MicroNumber, pegHash: String, gas: MicroNumber, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends BaseTransaction[SendFiat] with Logged {
  def mutateTicketID(newTicketID: String): SendFiat = SendFiat(from = from, to = to, amount = amount, pegHash = pegHash, gas = gas, status = status, txHash = txHash, ticketID = newTicketID, mode = mode, code = code)
}


@Singleton
class SendFiats @Inject()(
                           actorSystem: ActorSystem,
                           transaction: utilities.Transaction,
                           protected val databaseConfigProvider: DatabaseConfigProvider,
                           blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks,
                           getOrder: GetOrder,
                           blockchainFiats: blockchain.Fiats,
                           blockchainOrders: blockchain.Orders,
                           blockchainNegotiations: blockchain.Negotiations,
                           utilitiesNotification: utilities.Notification,
                           masterAssets: master.Assets,
                           blockchainAccounts: blockchain.Accounts,
                           masterNegotiations: master.Negotiations,
                           masterOrders: master.Orders,
                           masterTransactionSendFiatRequests: masterTransaction.SendFiatRequests,
                           masterTransactionTradeActivities: masterTransaction.TradeActivities,
                         )(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {


  def serialize(sendFiat: SendFiat): SendFiatSerialized = SendFiatSerialized(from = sendFiat.from, to = sendFiat.to, amount = sendFiat.amount.toMicroString, pegHash = sendFiat.pegHash, gas = sendFiat.gas.toMicroString, status = sendFiat.status, txHash = sendFiat.txHash, ticketID = sendFiat.ticketID, mode = sendFiat.mode, code = sendFiat.code, createdBy = sendFiat.createdBy, createdOn = sendFiat.createdOn, createdOnTimeZone = sendFiat.createdOnTimeZone, updatedBy = sendFiat.updatedBy, updatedOn = sendFiat.updatedOn, updatedOnTimeZone = sendFiat.updatedOnTimeZone)

  case class SendFiatSerialized(from: String, to: String, amount: String, pegHash: String, gas: String, status: Option[Boolean], txHash: Option[String], ticketID: String, mode: String, code: Option[String], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: SendFiat = SendFiat(from = from, to = to, amount = new MicroNumber(BigInt(amount)), pegHash = pegHash, gas = new MicroNumber(BigInt(gas)), status = status, txHash = txHash, ticketID = ticketID, mode = mode, code = code, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SEND_FIAT

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val sendFiatTable = TableQuery[SendFiatTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(sendFiatSerialized: SendFiatSerialized): Future[String] = db.run((sendFiatTable returning sendFiatTable.map(_.ticketID) += sendFiatSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByTicketID(ticketID: String): Future[SendFiatSerialized] = db.run(sendFiatTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(sendFiatTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(sendFiatTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findTransactionHashesByBuyerSellerPegHashAndStatus(buyerAddress: String, sellerAddress: String, pegHash: String, status: Option[Boolean]): Future[Seq[Option[String]]] = db.run(sendFiatTable.filter(_.from === buyerAddress).filter(_.to === sellerAddress).filter(_.pegHash === pegHash).filter(_.status.? === status).map(_.txHash.?).result)

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(sendFiatTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusByTicketID(ticketID: String, status: Option[Boolean]): Future[Int] = db.run(sendFiatTable.filter(_.ticketID === ticketID).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(sendFiatTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def getTransactionByFromToAndPegHash(from: String, to: String, pegHash: String) = db.run(sendFiatTable.filter(x => x.from === from && x.to === to && x.pegHash === pegHash).result.headOption)

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(sendFiatTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByTicketID(ticketID: String) = db.run(sendFiatTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(sendFiatTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class SendFiatTable(tag: Tag) extends Table[SendFiatSerialized](tag, "SendFiat") {

    def * = (from, to, amount, pegHash, gas, status.?, txHash.?, ticketID, mode, code.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (SendFiatSerialized.tupled, SendFiatSerialized.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def amount = column[String]("amount")

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

    def create(sendFiat: SendFiat): Future[String] = add(serialize(sendFiat))

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def resetTransactionStatus(ticketID: String): Future[Int] = updateStatusByTicketID(ticketID, status = null)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[SendFiat] = findByTicketID(ticketID).map(_.deserialize)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def getFiatProofs(buyerAddress: String, sellerAddress: String, pegHash: String): Future[Seq[String]] = findTransactionHashesByBuyerSellerPegHashAndStatus(buyerAddress = buyerAddress, sellerAddress = sellerAddress, pegHash = pegHash, status = Option(true)).map(_.map(_.getOrElse(throw new BaseException(constants.Response.TRANSACTION_HASH_NOT_FOUND))).sorted)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

    def getTransactionStatus(from: String, to: String, pegHash: String) = getTransactionByFromToAndPegHash(from, to, pegHash).map(x => if (x.isDefined) x.get.status else Option(false))
  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val sendFiat = Service.getTransaction(ticketID)
      val markBlockchainSuccess = masterTransactionSendFiatRequests.Service.markBlockchainSuccess(ticketID)

      def bcFiatsInOrder(negotiationID: String): Future[Seq[Fiat]] = blockchainFiats.Service.getFiatPegWallet(negotiationID)

      def orderResponse(negotiationID: String): Future[OrderResponse.Response] = getOrder.Service.get(negotiationID)

      def getNegotiationID(sendFiat: SendFiat): Future[String] = blockchainNegotiations.Service.tryGetID(buyerAddress = sendFiat.from, sellerAddress = sendFiat.to, pegHash = sendFiat.pegHash)

      def getMasterNegotiation(negotiationID: String): Future[masterNegotiation] = masterNegotiations.Service.tryGetByBCNegotiationID(negotiationID)

      def updateBCFiat(bcFiatsInOrder: Seq[Fiat], negotiationID: String, orderResponse: OrderResponse.Response): Future[Unit] = orderResponse.value.fiat_peg_wallet match {
        case Some(fiatPegWallet) => {
          val updateFiats = Future.traverse(bcFiatsInOrder.map(_.pegHash).intersect(fiatPegWallet.map(_.pegHash)).flatMap(pegHash => fiatPegWallet.find(_.pegHash == pegHash)))(fiatPeg => {
            blockchainFiats.Service.update(Fiat(pegHash = fiatPeg.pegHash, ownerAddress = negotiationID, transactionID = fiatPeg.transactionID, transactionAmount = fiatPeg.transactionAmount, redeemedAmount = fiatPeg.redeemedAmount, dirtyBit = false))
          })
          val insertFiats = Future.traverse(fiatPegWallet.map(_.pegHash).diff(bcFiatsInOrder.map(_.pegHash)).flatMap(pegHash => fiatPegWallet.find(_.pegHash == pegHash)))(fiatPeg => {
            blockchainFiats.Service.create(pegHash = fiatPeg.pegHash, ownerAddress = negotiationID, transactionID = fiatPeg.transactionID, transactionAmount = fiatPeg.transactionAmount, redeemedAmount = fiatPeg.redeemedAmount, dirtyBit = false)
          })
          for {
            _ <- updateFiats
            _ <- insertFiats
          } yield Unit
        }
        case None => throw new BaseException(constants.Response.FIAT_PEG_WALLET_NOT_FOUND)
      }

      def checkOrderExists(negotiationID: String): Future[Boolean] = blockchainOrders.Service.checkOrderExists(negotiationID)

      def createOrder(orderExists: Boolean, negotiationID: String, negotiation: masterNegotiation, amountSent: MicroNumber): Future[Unit] = {
        def status(fiatsInOrder: MicroNumber, assetSent: Boolean): String = {
          if (fiatsInOrder >= negotiation.price && assetSent) constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING
          else if (fiatsInOrder >= negotiation.price && !assetSent) constants.Status.Order.FIAT_SENT_ASSET_PENDING
          else if (fiatsInOrder < negotiation.price && assetSent) constants.Status.Order.ASSET_SENT_FIAT_PENDING
          else constants.Status.Order.ASSET_AND_FIAT_PENDING
        }

        if (!orderExists) {
          val bcOrderCreate = blockchainOrders.Service.create(id = negotiationID, awbProofHash = None, fiatProofHash = None)

          def masterOrderCreate: Future[String] = masterOrders.Service.create(masterOrder(id = negotiation.id, orderID = negotiationID, status = status(amountSent, false)))

          for {
            _ <- bcOrderCreate
            _ <- masterOrderCreate
          } yield ()

        } else {
          val assetSent = masterAssets.Service.tryGetStatus(negotiation.assetID)
          val fiatsInOrder = masterTransactionSendFiatRequests.Service.getFiatsInOrder(negotiation.id)

          def masterOrderUpdate(fiatsInOrder: MicroNumber, assetSent: String): Future[Int] = masterOrders.Service.update(masterOrder(id = negotiation.id, orderID = negotiationID, status = status(fiatsInOrder, assetSent == constants.Status.Asset.IN_ORDER)))

          for {
            assetSent <- assetSent
            fiatsInOrder <- fiatsInOrder
            _ <- masterOrderUpdate(fiatsInOrder, assetSent)
          } yield ()
        }
      }

      def markDirty(sendFiat: SendFiat): Future[Unit] = {
        val markFiatsDirty = blockchainFiats.Service.markDirty(sendFiat.from)
        val markBuyerTransactionFeedbackDirty = blockchainTransactionFeedbacks.Service.markDirty(sendFiat.from)
        val markBuyerAccountDirty = blockchainAccounts.Service.markDirty(sendFiat.from)
        for {
          _ <- markFiatsDirty
          _ <- markBuyerTransactionFeedbackDirty
          _ <- markBuyerAccountDirty
        } yield ()
      }

      def getAccountID(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

      (for {
        _ <- markTransactionSuccessful
        sendFiat <- sendFiat
        _ <- markBlockchainSuccess
        negotiationID <- getNegotiationID(sendFiat)
        bcFiatsInOrder <- bcFiatsInOrder(negotiationID)
        orderResponse <- orderResponse(negotiationID)
        masterNegotiation <- getMasterNegotiation(negotiationID)
        _ <- updateBCFiat(bcFiatsInOrder = bcFiatsInOrder, negotiationID = negotiationID, orderResponse = orderResponse)
        orderExists <- checkOrderExists(negotiationID)
        _ <- createOrder(orderExists = orderExists, negotiationID = negotiationID, negotiation = masterNegotiation, sendFiat.amount)
        _ <- markDirty(sendFiat)
        fromAccountID <- getAccountID(sendFiat.from)
        toAccountID <- getAccountID(sendFiat.to)
        _ <- utilitiesNotification.send(toAccountID, constants.Notification.SUCCESS, blockResponse.txhash)
        _ <- utilitiesNotification.send(fromAccountID, constants.Notification.SUCCESS, blockResponse.txhash)
        _ <- masterTransactionTradeActivities.Service.create(negotiationID = masterNegotiation.id, tradeActivity = constants.TradeActivity.SEND_FIAT_TO_ORDER_SUCCESSFUL)
      } yield {
        actors.Service.cometActor ! actors.Message.makeCometMessage(username = fromAccountID, messageType = constants.Comet.NEGOTIATION, messageContent = actors.Message.Negotiation(masterNegotiation.id))
      }).recover {
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
      val sendFiat = Service.getTransaction(ticketID)
      val markBlockchainFailure = masterTransactionSendFiatRequests.Service.markBlockchainFailure(ticketID)

      def markDirty(fromAddress: String): Future[Int] = blockchainTransactionFeedbacks.Service.markDirty(fromAddress)

      def getAccountID(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

      (for {
        _ <- markTransactionFailed
        sendFiat <- sendFiat
        _ <- markDirty(sendFiat.from)
        fromAccountID <- getAccountID(sendFiat.from)
        _ <- markBlockchainFailure
        _ <- utilitiesNotification.send(fromAccountID, constants.Notification.FAILURE, message)
      } yield {}).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  val scheduledTask = new Runnable {
    override def run(): Unit = {
      Await.result(transaction.ticketUpdater(Service.getTicketIDsOnStatus, Service.getTransactionHash, Service.getMode, Utility.onSuccess, Utility.onFailure), Duration.Inf)
    }
  }

  if (kafkaEnabled || transactionMode != constants.Transactions.BLOCK_MODE) {
    actorSystem.scheduler.scheduleWithFixedDelay(initialDelay = schedulerInitialDelay, delay = schedulerInterval)(scheduledTask)(schedulerExecutionContext)
  }
}