package models.blockchainTransaction

import java.net.ConnectException

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.{GetNegotiation, GetNegotiationID}
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse
import utilities.PushNotification

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ConfirmBuyerBid(from: String, to: String, bid: Int, time: Int, pegHash: String, buyerContractHash: String, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, mode: String, code: Option[String])

@Singleton
class ConfirmBuyerBids @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks, getNegotiationID: GetNegotiationID, blockchainNegotiations: blockchain.Negotiations, getNegotiation: GetNegotiation, transactionConfirmBuyerBid: transactions.ConfirmBuyerBid, pushNotification: PushNotification, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_CONFIRM_BUYER_BID

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val confirmBuyerBidTable = TableQuery[ConfirmBuyerBidTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private def add(confirmBuyerBid: ConfirmBuyerBid): Future[String] = db.run((confirmBuyerBidTable returning confirmBuyerBidTable.map(_.ticketID) += confirmBuyerBid).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(confirmBuyerBid: ConfirmBuyerBid): Future[Int] = db.run(confirmBuyerBidTable.insertOrUpdate(confirmBuyerBid).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String): Future[ConfirmBuyerBid] = db.run(confirmBuyerBidTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndResponseCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(confirmBuyerBidTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(confirmBuyerBidTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(confirmBuyerBidTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByTicketID(ticketID: String)(implicit executionContext: ExecutionContext) = db.run(confirmBuyerBidTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class ConfirmBuyerBidTable(tag: Tag) extends Table[ConfirmBuyerBid](tag, "ConfirmBuyerBid") {

    def * = (from, to, bid, time, pegHash, buyerContractHash, gas, status.?, txHash.?, ticketID, mode, code.?) <> (ConfirmBuyerBid.tupled, ConfirmBuyerBid.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def bid = column[Int]("bid")

    def time = column[Int]("time")

    def pegHash = column[String]("pegHash")

    def buyerContractHash = column[String]("buyerContractHash")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def mode = column[String]("mode")

    def code = column[String]("code")
  }


  object Service {

    def create(confirmBuyerBid: ConfirmBuyerBid): String = Await.result(add(ConfirmBuyerBid(from = confirmBuyerBid.from, to = confirmBuyerBid.to, bid = confirmBuyerBid.bid, time = confirmBuyerBid.time, pegHash = confirmBuyerBid.pegHash, buyerContractHash = confirmBuyerBid.buyerContractHash, gas = confirmBuyerBid.gas, status = confirmBuyerBid.status, txHash = confirmBuyerBid.txHash, ticketID = confirmBuyerBid.ticketID, mode = confirmBuyerBid.mode, code = confirmBuyerBid.code)), Duration.Inf)

    def markTransactionSuccessful(ticketID: String, txHash: String): Int = Await.result(updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true)), Duration.Inf)

    def markTransactionFailed(ticketID: String, code: String): Int = Await.result(updateStatusAndResponseCodeOnTicketID(ticketID, status = Option(false), code), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus, Duration.Inf)

    def getTransaction(ticketID: String): ConfirmBuyerBid = Await.result(findByTicketID(ticketID), Duration.Inf)

    def getTransactionHash(ticketID: String): Option[String] = Await.result(findByTicketID(ticketID), Duration.Inf).txHash

  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = Future {
      try {
        Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
        val confirmBuyerBid = Service.getTransaction(ticketID)
        val fromAddress = masterAccounts.Service.getAddress(confirmBuyerBid.from)
        val toID = masterAccounts.Service.getId(confirmBuyerBid.to)
        Thread.sleep(sleepTime)
        val negotiationID = blockchainNegotiations.Service.getNegotiationID(buyerAddress = fromAddress, sellerAddress = confirmBuyerBid.to, pegHash = confirmBuyerBid.pegHash)
        val negotiationResponse = if (negotiationID == "") getNegotiation.Service.get(getNegotiationID.Service.get(buyerAccount = confirmBuyerBid.from, sellerAccount = toID, pegHash = confirmBuyerBid.pegHash).negotiationID) else getNegotiation.Service.get(negotiationID)
        blockchainNegotiations.Service.insertOrUpdate(id = negotiationResponse.value.negotiationID, buyerAddress = negotiationResponse.value.buyerAddress, sellerAddress = negotiationResponse.value.sellerAddress, assetPegHash = negotiationResponse.value.pegHash, bid = negotiationResponse.value.bid, time = negotiationResponse.value.time, buyerSignature = negotiationResponse.value.buyerSignature, sellerSignature = negotiationResponse.value.sellerSignature, buyerBlockHeight = negotiationResponse.value.buyerBlockHeight, sellerBlockHeight = negotiationResponse.value.sellerBlockHeight, buyerContractHash = negotiationResponse.value.BuyerContractHash, sellerContractHash = negotiationResponse.value.SellerContractHash, dirtyBit = true)
        blockchainAccounts.Service.markDirty(fromAddress)
        blockchainTransactionFeedbacks.Service.markDirty(fromAddress)
        blockchainTransactionFeedbacks.Service.markDirty(confirmBuyerBid.to)
        pushNotification.sendNotification(toID, constants.Notification.SUCCESS, blockResponse.txhash)
        pushNotification.sendNotification(confirmBuyerBid.from, constants.Notification.SUCCESS, blockResponse.txhash)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
        case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.markTransactionFailed(ticketID, message)
        val confirmBuyerBid = Service.getTransaction(ticketID)
        blockchainTransactionFeedbacks.Service.markDirty(masterAccounts.Service.getAddress(confirmBuyerBid.from))
        blockchainTransactionFeedbacks.Service.markDirty(confirmBuyerBid.to)
        pushNotification.sendNotification(masterAccounts.Service.getId(confirmBuyerBid.to), constants.Notification.FAILURE, message)
        pushNotification.sendNotification(confirmBuyerBid.from, constants.Notification.FAILURE, message)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }


  if (kafkaEnabled) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      transaction.ticketUpdater(Service.getTicketIDsOnStatus, Service.getTransactionHash, Utility.onSuccess, Utility.onFailure)
    }
  }
}