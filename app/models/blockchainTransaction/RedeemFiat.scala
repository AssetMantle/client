package models.blockchainTransaction

import java.sql.Timestamp

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.Trait.Logged
import models.{blockchain, master, masterTransaction}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse
import utilities.MicroLong

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class RedeemFiat(from: String, to: String, redeemAmount: MicroLong, gas: MicroLong, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends BaseTransaction[RedeemFiat] with Logged {
  def mutateTicketID(newTicketID: String): RedeemFiat = RedeemFiat(from = from, to = to, redeemAmount = redeemAmount, gas = gas, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}

@Singleton
class RedeemFiats @Inject()(actorSystem: ActorSystem,
                            transaction: utilities.Transaction,
                            protected val databaseConfigProvider: DatabaseConfigProvider,
                            blockchainFiats: blockchain.Fiats,
                            blockchainAccounts: blockchain.Accounts,
                            masterTransactionRedeemFiats: masterTransaction.RedeemFiatRequests,
                            utilitiesNotification: utilities.Notification)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {
  
  def serialize(redeemFiat: RedeemFiat): RedeemFiatSerialized = RedeemFiatSerialized(from = redeemFiat.from, to = redeemFiat.to, redeemAmount = redeemFiat.redeemAmount.value, gas = redeemFiat.gas.toMicroLong, status = redeemFiat.status, txHash = redeemFiat.txHash, ticketID = redeemFiat.ticketID, mode = redeemFiat.mode, code = redeemFiat.code, createdBy = redeemFiat.createdBy, createdOn = redeemFiat.createdOn, createdOnTimeZone = redeemFiat.createdOnTimeZone, updatedBy = redeemFiat.updatedBy, updatedOn = redeemFiat.updatedOn, updatedOnTimeZone = redeemFiat.updatedOnTimeZone)

  case class RedeemFiatSerialized(from: String, to: String, redeemAmount: Long, gas: Long, status: Option[Boolean], txHash: Option[String], ticketID: String, mode: String, code: Option[String], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: RedeemFiat = RedeemFiat(from = from, to = to, redeemAmount = new MicroLong(redeemAmount), gas = new MicroLong(gas), status = status, txHash = txHash, ticketID = ticketID, mode = mode, code = code, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }
  
  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_REDEEM_FIAT

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val redeemFiatTable = TableQuery[RedeemFiatTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(redeemFiatSerialized: RedeemFiatSerialized): Future[String] = db.run((redeemFiatTable returning redeemFiatTable.map(_.ticketID) += redeemFiatSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByTicketID(ticketID: String): Future[RedeemFiatSerialized] = db.run(redeemFiatTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(redeemFiatTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(redeemFiatTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(redeemFiatTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update(txHash, status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(redeemFiatTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusByTicketID(ticketID: String, status: Option[Boolean]): Future[Int] = db.run(redeemFiatTable.filter(_.ticketID === ticketID).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(redeemFiatTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def deleteByTicketID(ticketID: String) = db.run(redeemFiatTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(redeemFiatTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class RedeemFiatTable(tag: Tag) extends Table[RedeemFiatSerialized](tag, "RedeemFiat") {

    def * = (from, to, redeemAmount, gas, status.?, txHash.?, ticketID, mode, code.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (RedeemFiatSerialized.tupled, RedeemFiatSerialized.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def redeemAmount = column[Long]("redeemAmount")

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

    def create(redeemFiat: RedeemFiat): Future[String] = add(serialize(redeemFiat))

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def resetTransactionStatus(ticketID: String): Future[Int] = updateStatusByTicketID(ticketID, status = null)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[RedeemFiat] = findByTicketID(ticketID).map(_.deserialize)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val redeemFiat = Service.getTransaction(ticketID)
      val markBlockchainSuccess = masterTransactionRedeemFiats.Service.markBlockchainSuccess(ticketID)

      def markDirty(redeemFiat: RedeemFiat): Future[Unit] = {
        val markFiatsDirty = blockchainFiats.Service.markDirty(redeemFiat.from)
        val markBlockchainAccountDirty = blockchainAccounts.Service.markDirty(redeemFiat.from)
        for {
          _ <- markFiatsDirty
          _ <- markBlockchainAccountDirty
        } yield {}
      }

      def getAccountID(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

      (for {
        _ <- markTransactionSuccessful
        redeemFiat <- redeemFiat
        _ <- markBlockchainSuccess
        _ <- markDirty(redeemFiat)
        fromAccountID <- getAccountID(redeemFiat.from)
        toAccountID <- getAccountID(redeemFiat.to)
        _ <- utilitiesNotification.send(toAccountID, constants.Notification.SUCCESS, blockResponse.txhash)
        _ <- utilitiesNotification.send(fromAccountID, constants.Notification.SUCCESS, blockResponse.txhash)
      } yield ()).recover {
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

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      val markTransactionFailed = Service.markTransactionFailed(ticketID, message)
      val redeemFiat = Service.getTransaction(ticketID)

      def getAccountID(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

      val markBlockchainFailure = masterTransactionRedeemFiats.Service.markBlockchainFailure(ticketID)
      (for {
        _ <- markTransactionFailed
        redeemFiat <- redeemFiat
        fromAccountID <- getAccountID(redeemFiat.from)
        _ <- markBlockchainFailure
        _ <- utilitiesNotification.send(fromAccountID, constants.Notification.FAILURE, message)
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