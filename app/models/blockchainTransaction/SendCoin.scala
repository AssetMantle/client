package models.blockchainTransaction

import java.sql.Timestamp

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.Trait.Logged
import models.master.Account
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.GetAccount
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse
import utilities.MicroLong

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SendCoin(from: String, to: String, amount: MicroLong, gas: MicroLong, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends BaseTransaction[SendCoin] with Logged {
  def mutateTicketID(newTicketID: String): SendCoin = SendCoin(from = from, to = to, amount = amount, gas = gas, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}


@Singleton
class SendCoins @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, transactionSendCoin: transactions.SendCoin, utilitiesNotification: utilities.Notification, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts, implicit val getAccount: GetAccount)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  private implicit val logger: Logger = Logger(this.getClass)
  val db = databaseConfig.db

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SEND_COIN

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val sendCoinTable = TableQuery[SendCoinTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").second

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  case class SendCoinSerialized(from: String, to: String, amount: Long, gas: Long, status: Option[Boolean], txHash: Option[String], ticketID: String, mode: String, code: Option[String], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: SendCoin = SendCoin(from = from, to = to, amount = new MicroLong(amount), gas = new MicroLong(gas), status = status, txHash = txHash, ticketID = ticketID, mode = mode, code = code, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedOn = updatedOn, updatedBy = updatedBy, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(sendCoin: SendCoin): SendCoinSerialized = SendCoinSerialized(from = sendCoin.from, to = sendCoin.to, amount = sendCoin.amount.toMicroLong, gas = sendCoin.gas.toMicroLong, status = sendCoin.status, txHash = sendCoin.txHash, ticketID = sendCoin.ticketID, mode = sendCoin.mode, code = sendCoin.code, createdBy = sendCoin.createdBy, createdOn = sendCoin.createdOn, createdOnTimeZone = sendCoin.createdOnTimeZone, updatedBy = sendCoin.updatedBy, updatedOn = sendCoin.updatedOn, updatedOnTimeZone = sendCoin.updatedOnTimeZone)

  private def add(sendCoin: SendCoin): Future[String] = db.run((sendCoinTable returning sendCoinTable.map(_.ticketID) += serialize(sendCoin)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusByTicketID(ticketID: String, status: Option[Boolean]): Future[Int] = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findByTicketID(ticketID: String): Future[SendCoinSerialized] = db.run(sendCoinTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(sendCoinTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def deleteByTicketID(ticketID: String) = db.run(sendCoinTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class SendCoinTable(tag: Tag) extends Table[SendCoinSerialized](tag, "SendCoin") {

    def * = (from, to, amount, gas, status.?, txHash.?, ticketID, mode, code.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (SendCoinSerialized.tupled, SendCoinSerialized.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def amount = column[Long]("amount")

    def gas = column[Long]("gas")

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

    def create(sendCoin: SendCoin): Future[String] = add(SendCoin(from = sendCoin.from, to = sendCoin.to, amount = sendCoin.amount, gas = sendCoin.gas, status = sendCoin.status, txHash = sendCoin.txHash, ticketID = sendCoin.ticketID, mode = sendCoin.mode, code = sendCoin.code))

    def createAsync(sendCoin: SendCoin): Future[String] = add(SendCoin(from = sendCoin.from, to = sendCoin.to, amount = sendCoin.amount, gas = sendCoin.gas, status = sendCoin.status, txHash = sendCoin.txHash, ticketID = sendCoin.ticketID, mode = sendCoin.mode, code = sendCoin.code))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def resetTransactionStatus(ticketID: String): Future[Int] = updateStatusByTicketID(ticketID, status = null)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[SendCoin] = findByTicketID(ticketID).map(_.deserialize)

    def getTransactionAsync(ticketID: String): Future[SendCoin] = findByTicketID(ticketID).map(_.deserialize)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

    def updateTransactionHashAsync(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))
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

      def getAccountID(address: String) = blockchainAccounts.Service.tryGetUsername(address)

      def toAccount(accountID: String): Future[Account] = masterAccounts.Service.tryGet(accountID)

      def unknownUserTypeUpdate(toAccount: Account): Future[Int] = {
        if (toAccount.userType == constants.User.UNKNOWN) {
          masterAccounts.Service.markUserTypeUser(toAccount.id)
        } else Future(0)
      }

      (for {
        _ <- markTransactionSuccessful
        sendCoin <- sendCoin
        _ <- markDirty(sendCoin)
        accountID <- getAccountID(sendCoin.to)
        toAccount <- toAccount(accountID)
        _ <- unknownUserTypeUpdate(toAccount)
        fromAccountID <- getAccountID(sendCoin.from)
        _ <- utilitiesNotification.send(toAccount.id, constants.Notification.SUCCESS, blockResponse.txhash)
        _ <- utilitiesNotification.send(fromAccountID, constants.Notification.SUCCESS, blockResponse.txhash)
      } yield {}).recover {
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
      val sendCoin = Service.getTransaction(ticketID)

      def getAccountID(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

      (for {
        _ <- markTransactionFailed
        sendCoin <- sendCoin
        fromAccountID <- getAccountID(sendCoin.from)
        _ <- utilitiesNotification.send(fromAccountID, constants.Notification.FAILURE, message)
      } yield {}).recover {
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