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
import transactions.responses.TransactionResponse.Response
import utilities.PushNotification

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ChangeSellerBid(from: String, to: String, bid: Int, time: Int, pegHash: String, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

@Singleton
class ChangeSellerBids @Inject()(actorSystem: ActorSystem, protected val databaseConfigProvider: DatabaseConfigProvider, blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks, getNegotiation: GetNegotiation, getNegotiationID: GetNegotiationID, blockchainNegotiations: blockchain.Negotiations, transactionChangeSellerBid: transactions.ChangeSellerBid, pushNotification: PushNotification, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_CHANGE_SELLER_BID

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val changeSellerBidTable = TableQuery[ChangeSellerBidTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private def add(changeSellerBid: ChangeSellerBid)(implicit executionContext: ExecutionContext): Future[String] = db.run((changeSellerBidTable returning changeSellerBidTable.map(_.ticketID) += changeSellerBid).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(changeSellerBid: ChangeSellerBid)(implicit executionContext: ExecutionContext): Future[Int] = db.run(changeSellerBidTable.insertOrUpdate(changeSellerBid).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[ChangeSellerBid] = db.run(changeSellerBidTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndResponseCodeOnTicketID(ticketID: String, status: Option[Boolean], responseCode: String): Future[Int] = db.run(changeSellerBidTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.responseCode)).update((status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(changeSellerBidTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashStatusAndResponseCodeOnTicketID(ticketID: String, txHash: String, status: Option[Boolean], responseCode: String): Future[Int] = db.run(changeSellerBidTable.filter(_.ticketID === ticketID).map(x => (x.txHash, x.status.?, x.responseCode)).update((txHash, status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByTicketID(ticketID: String)(implicit executionContext: ExecutionContext) = db.run(changeSellerBidTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class ChangeSellerBidTable(tag: Tag) extends Table[ChangeSellerBid](tag, "ChangeSellerBid") {

    def * = (from, to, bid, time, pegHash, gas, status.?, txHash.?, ticketID, responseCode.?) <> (ChangeSellerBid.tupled, ChangeSellerBid.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def bid = column[Int]("bid")

    def time = column[Int]("time")

    def pegHash = column[String]("pegHash")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }

  object Service {

    def create(from: String, to: String, bid: Int, time: Int, pegHash: String, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])(implicit executionContext: ExecutionContext): String = Await.result(add(ChangeSellerBid(from = from, to = to, bid = bid, time = time, pegHash = pegHash, gas = gas, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def markTransactionSuccessful(ticketID: String, txHash: String, responseCode: String): Int = Await.result(updateTxHashStatusAndResponseCodeOnTicketID(ticketID, txHash, status = Option(true), responseCode), Duration.Inf)

    def markTransactionFailed(ticketID: String, responseCode: String): Int = Await.result(updateStatusAndResponseCodeOnTicketID(ticketID, status = Option(false), responseCode), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus, Duration.Inf)

    def getTransaction(ticketID: String)(implicit executionContext: ExecutionContext): ChangeSellerBid = Await.result(findByTicketID(ticketID), Duration.Inf)

  }

  object Utility {
    def onSuccess(ticketID: String, response: Response): Future[Unit] = Future {
      try {
        Service.markTransactionSuccessful(ticketID, response.TxHash, response.Code)
        val changeSellerBid = Service.getTransaction(ticketID)
        val fromAddress = masterAccounts.Service.getAddress(changeSellerBid.from)
        val toID = masterAccounts.Service.getId(changeSellerBid.to)
        Thread.sleep(sleepTime)
        val negotiationID = blockchainNegotiations.Service.getNegotiationID(buyerAddress = changeSellerBid.to, sellerAddress = fromAddress, pegHash = changeSellerBid.pegHash)
        val negotiationResponse = if (negotiationID == "") getNegotiation.Service.get(getNegotiationID.Service.get(buyerAccount = toID, sellerAccount = changeSellerBid.from, pegHash = changeSellerBid.pegHash).negotiationID) else getNegotiation.Service.get(negotiationID)
        blockchainNegotiations.Service.insertOrUpdate(id = negotiationResponse.value.negotiationID, buyerAddress = negotiationResponse.value.buyerAddress, sellerAddress = negotiationResponse.value.sellerAddress, assetPegHash = negotiationResponse.value.pegHash, bid = negotiationResponse.value.bid, time = negotiationResponse.value.time, buyerSignature = negotiationResponse.value.buyerSignature, sellerSignature = negotiationResponse.value.sellerSignature, buyerBlockHeight = negotiationResponse.value.buyerBlockHeight, sellerBlockHeight = negotiationResponse.value.sellerBlockHeight, buyerContractHash = negotiationResponse.value.BuyerContractHash, sellerContractHash = negotiationResponse.value.SellerContractHash, dirtyBit = true)
        blockchainAccounts.Service.markDirty(fromAddress)
        blockchainTransactionFeedbacks.Service.markDirty(fromAddress)
        blockchainTransactionFeedbacks.Service.markDirty(changeSellerBid.to)
        pushNotification.sendNotification(toID, constants.Notification.SUCCESS, response.TxHash)
        pushNotification.sendNotification(changeSellerBid.from, constants.Notification.SUCCESS, response.TxHash)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
        case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.markTransactionFailed(ticketID, message)
        val changeSellerBid = Service.getTransaction(ticketID)
        blockchainTransactionFeedbacks.Service.markDirty(masterAccounts.Service.getAddress(changeSellerBid.from))
        blockchainTransactionFeedbacks.Service.markDirty(changeSellerBid.to)
        pushNotification.sendNotification(masterAccounts.Service.getId(changeSellerBid.to), constants.Notification.FAILURE, message)
        pushNotification.sendNotification(changeSellerBid.from, constants.Notification.FAILURE, message)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }


  if (kafkaEnabled) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      utilities.TicketUpdater.start(Service.getTicketIDsOnStatus, transactionChangeSellerBid.Service.getTxFromWSResponse, Utility.onSuccess, Utility.onFailure)
    }
  }
}