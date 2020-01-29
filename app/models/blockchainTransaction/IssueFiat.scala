package models.blockchainTransaction

import java.net.ConnectException

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.AccountResponse
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class IssueFiat(from: String, to: String, transactionID: String, transactionAmount: Int, gas: Int, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None) extends BaseTransaction[IssueFiat] {
  def mutateTicketID(newTicketID: String): IssueFiat = IssueFiat(from = from, to = to, transactionID = transactionID, transactionAmount = transactionAmount, gas = gas, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}


@Singleton
class IssueFiats @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, transactionIssueFiat: transactions.IssueFiat, utilitiesNotification: utilities.Notification, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts, blockchainFiats: blockchain.Fiats, getAccount: queries.GetAccount)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_ISSUE_FIAT

  private implicit val logger: Logger = Logger(this.getClass)
  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db
  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val issueFiatTable = TableQuery[IssueFiatTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")
  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(issueFiat: IssueFiat): Future[String] = db.run((issueFiatTable returning issueFiatTable.map(_.ticketID) += issueFiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(issueFiat: IssueFiat): Future[Int] = db.run(issueFiatTable.insertOrUpdate(issueFiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String): Future[IssueFiat] = db.run(issueFiatTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(issueFiatTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(issueFiatTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(issueFiatTable..filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update(txHash, status).asTry)

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(issueFiatTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update(txHash, status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(issueFiatTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(issueFiatTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(issueFiatTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByTicketID(ticketID: String) = db.run(issueFiatTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class IssueFiatTable(tag: Tag) extends Table[IssueFiat](tag, "IssueFiat") {

    def * = (from, to, transactionID, transactionAmount, gas, status.?, txHash.?, ticketID, mode, code.?) <> (IssueFiat.tupled, IssueFiat.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def transactionID = column[String]("transactionID")

    def transactionAmount = column[Int]("transactionAmount")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def mode = column[String]("mode")

    def code = column[String]("code")
  }

  object Service {

    def create(issueFiat: IssueFiat): Future[String] = add(IssueFiat(from = issueFiat.from, to = issueFiat.to, transactionID = issueFiat.transactionID, transactionAmount = issueFiat.transactionAmount, gas = issueFiat.gas, status = issueFiat.status, txHash = issueFiat.txHash, ticketID = issueFiat.ticketID, mode = issueFiat.mode, code = issueFiat.code))

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[IssueFiat] = findByTicketID(ticketID)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val issueFiat = Service.getTransaction(ticketID)

      def account(issueFiat: IssueFiat): Future[AccountResponse.Response] = getAccount.Service.get(issueFiat.to)

      def insertOrUpdate(account: queries.responses.AccountResponse.Response, issueFiat: IssueFiat) = {
        account.value.fiatPegWallet match {
          case Some(fiats) => Future.sequence(fiats.map(fiatPeg => blockchainFiats.Service.insertOrUpdate(fiatPeg.pegHash, issueFiat.to, fiatPeg.transactionID, fiatPeg.transactionAmount, fiatPeg.redeemedAmount, dirtyBit = true)))
          case None => Future {}
        }
      }

      def markDirty(issueFiat: IssueFiat): Future[Int] = blockchainAccounts.Service.markDirty(issueFiat.from)

      def getIDs(issueFiat: IssueFiat): Future[(String,String)] = {
        val toAccountID = masterAccounts.Service.getId(issueFiat.to)
        val fromAccountID = masterAccounts.Service.getId(issueFiat.from)
        for {
          toAccountID <- toAccountID
          fromAccountID <- fromAccountID
        } yield (toAccountID, fromAccountID)
      }

      (for {
        _ <- markTransactionSuccessful
        issueFiat <- issueFiat
        account <- account(issueFiat)
        _ <- insertOrUpdate(account, issueFiat)
        _ <- markDirty(issueFiat)
        (toAccountID, fromAccountID) <- getIDs(issueFiat)
      } yield {
        utilitiesNotification.send(toAccountID, constants.Notification.SUCCESS, blockResponse.txhash)
        utilitiesNotification.send(fromAccountID, constants.Notification.SUCCESS, blockResponse.txhash)
      }).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
        case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = {

      val markTransactionFailed = Service.markTransactionFailed(ticketID, message)
      val issueFiat = Service.getTransaction(ticketID)

      def getIDs(issueFiat: IssueFiat): Future[(String,String)] = {
        val toAccountID = masterAccounts.Service.getId(issueFiat.to)
        val fromAccountID = masterAccounts.Service.getId(issueFiat.from)
        for {
          toAccountID <- toAccountID
          fromAccountID <- fromAccountID
        } yield (toAccountID, fromAccountID)
      }

      (for {
        _ <- markTransactionFailed
        issueFiat <- issueFiat
        (toAccountID, fromAccountID) <- getIDs(issueFiat)
      } yield {
        utilitiesNotification.send(toAccountID, constants.Notification.FAILURE, message)
        utilitiesNotification.send(fromAccountID, constants.Notification.FAILURE, message)
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