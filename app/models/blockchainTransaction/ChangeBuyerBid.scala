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

case class ChangeBuyerBid(from: String, to: String, bid: Int, time: Int, pegHash: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

@Singleton
class ChangeBuyerBids @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks, getNegotiation: GetNegotiation, getNegotiationID: GetNegotiationID, blockchainNegotiations: blockchain.Negotiations, transactionChangeBuyerBid: transactions.ChangeBuyerBid, actorSystem: ActorSystem, pushNotification: PushNotification,masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext)  {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_CHANGE_BUYER_BID

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val changeBuyerBidTable = TableQuery[ChangeBuyerBidTable]

  private def add(changeBuyerBid: ChangeBuyerBid)(implicit executionContext: ExecutionContext): Future[String] = db.run((changeBuyerBidTable returning changeBuyerBidTable.map(_.ticketID) += changeBuyerBid).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def update(changeBuyerBid: ChangeBuyerBid)(implicit executionContext: ExecutionContext): Future[Int] = db.run(changeBuyerBidTable.insertOrUpdate(changeBuyerBid).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[ChangeBuyerBid] = db.run(changeBuyerBidTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndResponseOnTicketID(ticketID: String, status: Boolean, responseCode: String): Future[Int] = db.run(changeBuyerBidTable.filter(_.ticketID === ticketID).map(x => (x.status, x.responseCode)).update((status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus()(implicit executionContext: ExecutionContext): Future[Seq[String]] = db.run(changeBuyerBidTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashStatusAndResponseOnTicketID(ticketID: String, txHash: String, status: Boolean, responseCode: String): Future[Int] = db.run(changeBuyerBidTable.filter(_.ticketID === ticketID).map(x => (x.txHash, x.status, x.responseCode)).update((txHash, status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByTicketID(ticketID: String)(implicit executionContext: ExecutionContext) = db.run(changeBuyerBidTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class ChangeBuyerBidTable(tag: Tag) extends Table[ChangeBuyerBid](tag, "ChangeBuyerBid") {

    def * = (from, to, bid, time, pegHash, gas, status.?, txHash.?, ticketID, responseCode.?) <> (ChangeBuyerBid.tupled, ChangeBuyerBid.unapply)

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

    def addChangeBuyerBid(from: String, to: String, bid: Int, time: Int, pegHash: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String]) (implicit executionContext: ExecutionContext): String = Await.result(add(ChangeBuyerBid(from = from, to = to, bid = bid, time = time, pegHash = pegHash, gas = gas, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def updateTxHashStatusResponseCode(ticketID: String, txHash: String, status: Boolean, responseCode: String): Int = Await.result(updateTxHashStatusAndResponseOnTicketID(ticketID, txHash, status, responseCode), Duration.Inf)

    def updateStatusAndResponseCode(ticketID: String, status: Boolean, responseCode: String): Int = Await.result(updateStatusAndResponseOnTicketID(ticketID, status, responseCode), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus(), Duration.Inf)

    def getTransaction(ticketID: String)(implicit executionContext: ExecutionContext): ChangeBuyerBid = Await.result(findByTicketID(ticketID), Duration.Inf)

  }

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval =  configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  object Utility {
    def onSuccess(ticketID: String, response: Response): Future[Unit] = Future {
      try {
        Service.updateTxHashStatusResponseCode(ticketID, response.TxHash, status = true, response.Code)
        val changeBuyerBid = Service.getTransaction(ticketID)
        val fromAddress = masterAccounts.Service.getAddress(changeBuyerBid.from)
        val toID = masterAccounts.Service.getId(changeBuyerBid.to)
        Thread.sleep(sleepTime)
        val negotiationID = blockchainNegotiations.Service.getNegotiationID(buyerAddress = fromAddress, sellerAddress = changeBuyerBid.to, pegHash = changeBuyerBid.pegHash)
        val negotiationResponse = if (negotiationID == "") getNegotiation.Service.get(getNegotiationID.Service.get(buyerAccount = changeBuyerBid.from, sellerAccount = toID, pegHash = changeBuyerBid.pegHash).negotiationID) else getNegotiation.Service.get(negotiationID)
        blockchainNegotiations.Service.insertOrUpdateNegotiation(id = negotiationResponse.value.negotiationID, buyerAddress = negotiationResponse.value.buyerAddress, sellerAddress = negotiationResponse.value.sellerAddress, assetPegHash = negotiationResponse.value.pegHash, bid = negotiationResponse.value.bid, time = negotiationResponse.value.time, buyerSignature = negotiationResponse.value.buyerSignature, sellerSignature = negotiationResponse.value.sellerSignature, true)
        blockchainAccounts.Service.updateDirtyBit(fromAddress, dirtyBit = true)
        blockchainTransactionFeedbacks.Service.updateDirtyBit(fromAddress, true)
        blockchainTransactionFeedbacks.Service.updateDirtyBit(changeBuyerBid.to, true)
        pushNotification.sendNotification(toID, constants.Notification.SUCCESS, Seq(response.TxHash))
        pushNotification.sendNotification(changeBuyerBid.from, constants.Notification.SUCCESS, Seq(response.TxHash))
      } catch {
        case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
          throw new BaseException(constants.Error.PSQL_EXCEPTION)
        case connectException: ConnectException => logger.error(constants.Error.CONNECT_EXCEPTION, connectException)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.updateStatusAndResponseCode(ticketID, status = false, message)
        val changeBuyerBid = Service.getTransaction(ticketID)
        pushNotification.sendNotification(masterAccounts.Service.getId(changeBuyerBid.to), constants.Notification.FAILURE, Seq(message))
        pushNotification.sendNotification(changeBuyerBid.from, constants.Notification.FAILURE, Seq(message))
      } catch {
        case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
      }
    }
  }


  if (kafkaEnabled) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      utilities.TicketUpdater.start(Service.getTicketIDsOnStatus, transactionChangeBuyerBid.Service.getTxFromWSResponse, Utility.onSuccess, Utility.onFailure)
    }
  }
}