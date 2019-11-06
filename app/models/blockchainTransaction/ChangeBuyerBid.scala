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

case class ChangeBuyerBid(from: String, to: String, bid: Int, time: Int, pegHash: String, gas: Int, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None) extends BaseTransaction[ChangeBuyerBid] {
  def mutateTicketID(newTicketID: String): ChangeBuyerBid = ChangeBuyerBid(from = from, to = to, bid = bid, time = time, pegHash = pegHash, gas = gas, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}


@Singleton
class ChangeBuyerBids @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks, getNegotiation: GetNegotiation, getNegotiationID: GetNegotiationID, blockchainNegotiations: blockchain.Negotiations, transactionChangeBuyerBid: transactions.ChangeBuyerBid, utilitiesNotification: utilities.Notification, masterAccounts: master.Accounts, masterTransactionNegotiationRequests: masterTransaction.NegotiationRequests, blockchainAccounts: blockchain.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

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
      Thread.sleep(sleepTime)

      def negotiationID(changeBuyerBid: ChangeBuyerBid) = blockchainNegotiations.Service.getNegotiationID(buyerAddress = changeBuyerBid.from, sellerAddress = changeBuyerBid.to, pegHash = changeBuyerBid.pegHash)

      def negotiationResponse(negotiationID: String, changeBuyerBid: ChangeBuyerBid) = if (negotiationID == "") getNegotiation.Service.get(getNegotiationID.Service.get(buyerAddress = changeBuyerBid.from, sellerAddress = changeBuyerBid.to, pegHash = changeBuyerBid.pegHash).negotiationID) else getNegotiation.Service.get(negotiationID)

      def insertOrUpdate(negotiationResponse: NegotiationResponse.Response) = blockchainNegotiations.Service.insertOrUpdate(id = negotiationResponse.value.negotiationID, buyerAddress = negotiationResponse.value.buyerAddress, sellerAddress = negotiationResponse.value.sellerAddress, assetPegHash = negotiationResponse.value.pegHash, bid = negotiationResponse.value.bid, time = negotiationResponse.value.time, buyerSignature = negotiationResponse.value.buyerSignature, sellerSignature = negotiationResponse.value.sellerSignature, buyerBlockHeight = negotiationResponse.value.buyerBlockHeight, sellerBlockHeight = negotiationResponse.value.sellerBlockHeight, buyerContractHash = negotiationResponse.value.buyerContractHash, sellerContractHash = negotiationResponse.value.sellerContractHash, dirtyBit = true)

      def markDirty(changeBuyerBid: ChangeBuyerBid) = {
        val markDirtyFromAddressBlockchainAccounts = blockchainAccounts.Service.markDirty(changeBuyerBid.from)
        val markDirtyFromAddressInBlockchainTransactionFeedbacks = blockchainTransactionFeedbacks.Service.markDirty(changeBuyerBid.from)
        val markDirtyToAddressInBlockchainTransactionFeedbacks = blockchainTransactionFeedbacks.Service.markDirty(changeBuyerBid.to)
        for {
          _ <- markDirtyFromAddressBlockchainAccounts
          _ <- markDirtyFromAddressInBlockchainTransactionFeedbacks
          _ <- markDirtyToAddressInBlockchainTransactionFeedbacks
        } yield {}
      }

      (for {
        _ <- markTransactionSuccessful
        changeBuyerBid <- changeBuyerBid
        negotiationID <- negotiationID(changeBuyerBid)
        negotiationResponse <- negotiationResponse(negotiationID, changeBuyerBid)
        _ <- insertOrUpdate(negotiationResponse)
        _ <- markDirty(changeBuyerBid)
        (toAddressID, fromAddressID) <- getIDs(changeBuyerBid)
      } yield {
        utilitiesNotification.send(toAddressID, constants.Notification.SUCCESS, blockResponse.txhash)
        utilitiesNotification.send(fromAddressID, constants.Notification.SUCCESS, blockResponse.txhash)
      }).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
        case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
      }
    }

    def getIDs(changeBuyerBid: ChangeBuyerBid) = {
      val toAddressID = masterAccounts.Service.getId(changeBuyerBid.to)
      val fromAddressID = masterAccounts.Service.getId(changeBuyerBid.from)
      for {
        toAddressID <- toAddressID
        fromAddressID <- fromAddressID
      } yield (toAddressID, fromAddressID)
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = {

      val markTransactionFailed = Service.markTransactionFailed(ticketID, message)
      val changeBuyerBid = Service.getTransaction(ticketID)

      def markDirty(changeBuyerBid: ChangeBuyerBid) = Future.sequence(List(blockchainTransactionFeedbacks.Service.markDirty(changeBuyerBid.from), blockchainTransactionFeedbacks.Service.markDirty(changeBuyerBid.to)))

      def getAddresses(changeBuyerBid: ChangeBuyerBid) = Future.sequence(List(masterAccounts.Service.getId(changeBuyerBid.to), masterAccounts.Service.getId(changeBuyerBid.from)))

      (for {
        _ <- markTransactionFailed
        changeBuyerBid <- changeBuyerBid
        _ <- markDirty(changeBuyerBid)
        (toAddressID, fromAddressID) <- getIDs(changeBuyerBid)
      } yield {
        utilitiesNotification.send(toAddressID, constants.Notification.FAILURE, message)
        utilitiesNotification.send(fromAddressID, constants.Notification.FAILURE, message)
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