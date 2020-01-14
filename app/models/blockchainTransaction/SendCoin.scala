package models.blockchainTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.master.Account
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.GetAccount
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SendCoin(from: String, to: String, amount: Int, gas: Int, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None) extends BaseTransaction[SendCoin] {
  def mutateTicketID(newTicketID: String): SendCoin = SendCoin(from = from, to = to, amount = amount, gas = gas, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}


@Singleton
class SendCoins @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, transactionSendCoin: transactions.SendCoin, utilitiesNotification: utilities.Notification, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts, implicit val getAccount: GetAccount)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  private implicit val logger: Logger = Logger(this.getClass)
  val db = databaseConfig.db

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SEND_COIN
  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val sendCoinTable = TableQuery[SendCoinTable]
  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(sendCoin: SendCoin): Future[String] = db.run((sendCoinTable returning sendCoinTable.map(_.ticketID) += sendCoin).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(sendCoin: SendCoin): Future[Int] = db.run(sendCoinTable.insertOrUpdate(sendCoin).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String): Future[SendCoin] = db.run(sendCoinTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(sendCoinTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def deleteByTicketID(ticketID: String) = db.run(sendCoinTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class SendCoinTable(tag: Tag) extends Table[SendCoin](tag, "SendCoin") {

    def * = (from, to, amount, gas, status.?, txHash.?, ticketID, mode, code.?) <> (SendCoin.tupled, SendCoin.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def amount = column[Int]("amount")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def mode = column[String]("mode")

    def code = column[String]("code")
  }

  object Service {

    def create(sendCoin: SendCoin): Future[String] = add(SendCoin(from = sendCoin.from, to = sendCoin.to, amount = sendCoin.amount, gas = sendCoin.gas, status = sendCoin.status, txHash = sendCoin.txHash, ticketID = sendCoin.ticketID, mode = sendCoin.mode, code = sendCoin.code))

    def createAsync(sendCoin: SendCoin) = add(SendCoin(from = sendCoin.from, to = sendCoin.to, amount = sendCoin.amount, gas = sendCoin.gas, status = sendCoin.status, txHash = sendCoin.txHash, ticketID = sendCoin.ticketID, mode = sendCoin.mode, code = sendCoin.code))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def markTransactionFailedAsync(ticketID: String, code: String) = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionSuccessfulAsync(ticketID: String, txHash: String) = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[SendCoin] = findByTicketID(ticketID)

    def getTransactionAsync(ticketID: String) = findByTicketID(ticketID)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

    def updateTransactionHashAsync(ticketID: String, txHash: String) = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))
  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val sendCoin = Service.getTransaction(ticketID)

      def markDirty(sendCoin: SendCoin): Future[Unit] = {
        val markDirtyTo = blockchainAccounts.Service.markDirty(sendCoin.to)
        val markDirtyFrom = blockchainAccounts.Service.markDirty(sendCoin.from)
        for {
          _ <- markDirtyTo
          _ <- markDirtyFrom
        } yield {}
      }

      def toAccount(toAddress: String): Future[Account] = masterAccounts.Service.getAccountByAddress(toAddress)

      def updateUserType(toAccount: Account): Future[Any] = {
        if (toAccount.userType == constants.User.UNKNOWN) {
          masterAccounts.Service.updateUserType(toAccount.id, constants.User.USER)
        } else Future ()
      }

      def fromAccountID(fromAddress: String): Future[String] = masterAccounts.Service.getId(fromAddress)

      (for {
        _ <- markTransactionSuccessful
        sendCoin <- sendCoin
        _ <- markDirty(sendCoin)
        toAccount <- toAccount(sendCoin.to)
        _ <- updateUserType(toAccount)
        fromAccountID <- fromAccountID(sendCoin.from)
      } yield {
        utilitiesNotification.send(toAccount.id, constants.Notification.SUCCESS, blockResponse.txhash)
        utilitiesNotification.send(fromAccountID, constants.Notification.SUCCESS, blockResponse.txhash)
      }).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = {
      val markTransactionFailed = Service.markTransactionFailed(ticketID, message)
      val sendCoin = Service.getTransaction(ticketID)

      def getIDs(sendCoin: SendCoin): Future[(String,String)] = {
        val toAccountID = masterAccounts.Service.getId(sendCoin.to)
        val fromAccountID = masterAccounts.Service.getId(sendCoin.from)
        for {
          toAccountID <- toAccountID
          fromAccountID <- fromAccountID
        } yield (toAccountID, fromAccountID)
      }

      (for {
        _ <- markTransactionFailed
        sendCoin <- sendCoin
        (toAccountID, fromAccountID) <- getIDs(sendCoin)
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