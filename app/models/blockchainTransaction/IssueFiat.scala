package models.blockchainTransaction

import java.sql.Timestamp

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.Trait.Logged
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.AccountResponse
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse
import utilities.MicroNumber

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class IssueFiat(from: String, to: String, transactionID: String, transactionAmount: MicroNumber, gas: MicroNumber, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends BaseTransaction[IssueFiat] with Logged {
  def mutateTicketID(newTicketID: String): IssueFiat = IssueFiat(from = from, to = to, transactionID = transactionID, transactionAmount = transactionAmount, gas = gas, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}

@Singleton
class IssueFiats @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, utilitiesNotification: utilities.Notification, masterFiats: master.Fiats, masterTraders: master.Traders, blockchainAccounts: blockchain.Accounts, blockchainFiats: blockchain.Fiats, getAccount: queries.GetAccount)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  def serialize(issueFiat: IssueFiat): IssueFiatSerialized = IssueFiatSerialized(from = issueFiat.from, to = issueFiat.to, transactionID = issueFiat.transactionID, transactionAmount = issueFiat.transactionAmount.toMicroString, gas = issueFiat.gas.toMicroString, status = issueFiat.status, txHash = issueFiat.txHash, ticketID = issueFiat.ticketID, mode = issueFiat.mode, code = issueFiat.code, createdBy = issueFiat.createdBy, createdOn = issueFiat.createdOn, createdOnTimeZone = issueFiat.createdOnTimeZone, updatedBy = issueFiat.updatedBy, updatedOn = issueFiat.updatedOn, updatedOnTimeZone = issueFiat.updatedOnTimeZone)

  case class IssueFiatSerialized(from: String, to: String, transactionID: String, transactionAmount: String, gas: String, status: Option[Boolean], txHash: Option[String], ticketID: String, mode: String, code: Option[String], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: IssueFiat = IssueFiat(from = from, to = to, transactionID = transactionID, transactionAmount = new MicroNumber(BigInt(transactionAmount)), gas = new MicroNumber(BigInt(gas)), status = status, txHash = txHash, ticketID = ticketID, mode = mode, code = code, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_ISSUE_FIAT

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val issueFiatTable = TableQuery[IssueFiatTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(issueFiatSerialized: IssueFiatSerialized): Future[String] = db.run((issueFiatTable returning issueFiatTable.map(_.ticketID) += issueFiatSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByTicketID(ticketID: String): Future[IssueFiatSerialized] = db.run(issueFiatTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(issueFiatTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(issueFiatTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(issueFiatTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update(txHash, status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(issueFiatTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(issueFiatTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusByTicketID(ticketID: String, status: Option[Boolean]): Future[Int] = db.run(issueFiatTable.filter(_.ticketID === ticketID).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(issueFiatTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByTicketID(ticketID: String) = db.run(issueFiatTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class IssueFiatTable(tag: Tag) extends Table[IssueFiatSerialized](tag, "IssueFiat") {

    def * = (from, to, transactionID, transactionAmount, gas, status.?, txHash.?, ticketID, mode, code.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (IssueFiatSerialized.tupled, IssueFiatSerialized.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def transactionID = column[String]("transactionID")

    def transactionAmount = column[String]("transactionAmount")

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

    def create(issueFiat: IssueFiat): Future[String] = add(serialize(issueFiat))

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def resetTransactionStatus(ticketID: String): Future[Int] = updateStatusByTicketID(ticketID, status = null)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[IssueFiat] = findByTicketID(ticketID).map(_.deserialize)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val issueFiat = Service.getTransaction(ticketID)

      def getAccountResponse(issueFiat: IssueFiat): Future[AccountResponse.Response] = getAccount.Service.get(issueFiat.to)

      def create(account: queries.responses.AccountResponse.Response, issueFiat: IssueFiat) = {
        val fiat = account.value.fiat_peg_wallet match {
          case Some(fiatPegWallet) => fiatPegWallet.find(_.transactionID == issueFiat.transactionID).getOrElse(throw new BaseException(constants.Response.FIAT_PEG_NOT_FOUND))
          case None => throw new BaseException(constants.Response.FIAT_PEG_WALLET_NOT_FOUND)
        }
        blockchainFiats.Service.create(pegHash = fiat.pegHash, ownerAddress = issueFiat.to, transactionID = fiat.transactionID, transactionAmount = fiat.transactionAmount, redeemedAmount = fiat.redeemedAmount, dirtyBit = false)
      }

      def markDirty(issueFiat: IssueFiat): Future[Int] = blockchainAccounts.Service.markDirty(issueFiat.from)

      def getAccountID(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

      def traderID(accountID: String): Future[String] = masterTraders.Service.tryGetID(accountID)

      def markSuccess(traderID: String, transactionID: String): Future[Int] = masterFiats.Service.markSuccess(traderID, transactionID)

      (for {
        _ <- markTransactionSuccessful
        issueFiat <- issueFiat
        accountResponse <- getAccountResponse(issueFiat)
        toAccountID <- getAccountID(issueFiat.to)
        traderID <- traderID(toAccountID)
        _ <- create(accountResponse, issueFiat)
        _ <- markSuccess(traderID, issueFiat.transactionID)
        _ <- markDirty(issueFiat)
        fromAccountID <- getAccountID(issueFiat.from)
        _ <- utilitiesNotification.send(toAccountID, constants.Notification.SUCCESS, blockResponse.txhash)
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
      val issueFiat = Service.getTransaction(ticketID)

      def getAccountID(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

      def traderID(accountID: String): Future[String] = masterTraders.Service.tryGetID(accountID)

      def markFailure(traderID: String, transactionID: String): Future[Int] = masterFiats.Service.markFailure(traderID, transactionID)

      (for {
        _ <- markTransactionFailed
        issueFiat <- issueFiat
        fromAccountID <- getAccountID(issueFiat.from)
        toAccountID <- getAccountID(issueFiat.to)
        traderID <- traderID(toAccountID)
        _ <- markFailure(traderID, issueFiat.transactionID)
        _ <- utilitiesNotification.send(toAccountID, constants.Notification.FAILURE, message)
        _ <- utilitiesNotification.send(fromAccountID, constants.Notification.FAILURE, message)
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  if (kafkaEnabled || transactionMode != constants.Transactions.BLOCK_MODE) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      Await.result(transaction.ticketUpdater(Service.getTicketIDsOnStatus, Service.getTransactionHash, Service.getMode, Utility.onSuccess, Utility.onFailure), Duration.Inf)
    }(schedulerExecutionContext)
  }
}