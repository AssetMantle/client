package models.blockchainTransaction

import java.sql.Timestamp

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.Trait.Logged
import models.master.Negotiation
import models.{blockchain, master, masterTransaction}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse
import utilities.MicroNumber

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ReleaseAsset(from: String, to: String, pegHash: String, gas: MicroNumber, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends BaseTransaction[ReleaseAsset] with Logged {
  def mutateTicketID(newTicketID: String): ReleaseAsset = ReleaseAsset(from = from, to = to, pegHash = pegHash, gas = gas, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}

@Singleton
class ReleaseAssets @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, transactionReleaseAsset: transactions.ReleaseAsset, blockchainAssets: blockchain.Assets, blockchainAccounts: blockchain.Accounts, utilitiesNotification: utilities.Notification, masterAccounts: master.Accounts, masterAssets: master.Assets, masterNegotiations: master.Negotiations, masterTransactionTradeActivities: masterTransaction.TradeActivities)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_RELEASE_ASSET

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val releaseAssetTable = TableQuery[ReleaseAssetTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  def serialize(releaseAsset: ReleaseAsset): ReleaseAssetSerialized = ReleaseAssetSerialized(from = releaseAsset.from, to = releaseAsset.to, pegHash = releaseAsset.pegHash, gas = releaseAsset.gas.toMicroString, status = releaseAsset.status, txHash = releaseAsset.txHash, ticketID = releaseAsset.ticketID, mode = releaseAsset.mode, code = releaseAsset.code, createdBy = releaseAsset.createdBy, createdOn = releaseAsset.createdOn, createdOnTimeZone = releaseAsset.createdOnTimeZone, updatedBy = releaseAsset.updatedBy, updatedOn = releaseAsset.updatedOn, updatedOnTimeZone = releaseAsset.updatedOnTimeZone)

  case class ReleaseAssetSerialized(from: String, to: String, pegHash: String, gas: String, status: Option[Boolean], txHash: Option[String], ticketID: String, mode: String, code: Option[String], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: ReleaseAsset = ReleaseAsset(from = from, to = to, pegHash = pegHash, gas = new MicroNumber(BigInt(gas)), status = status, txHash = txHash, ticketID = ticketID, mode = mode, code = code, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  private def add(releaseAsset: ReleaseAsset): Future[String] = db.run((releaseAssetTable returning releaseAssetTable.map(_.ticketID) += serialize(releaseAsset)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByTicketID(ticketID: String): Future[ReleaseAssetSerialized] = db.run(releaseAssetTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(releaseAssetTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(releaseAssetTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(releaseAssetTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(releaseAssetTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusByTicketID(ticketID: String, status: Option[Boolean]): Future[Int] = db.run(releaseAssetTable.filter(_.ticketID === ticketID).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(releaseAssetTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def deleteByTicketID(ticketID: String) = db.run(releaseAssetTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(releaseAssetTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class ReleaseAssetTable(tag: Tag) extends Table[ReleaseAssetSerialized](tag, "ReleaseAsset") {

    def * = (from, to, pegHash, gas, status.?, txHash.?, ticketID, mode, code.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (ReleaseAssetSerialized.tupled, ReleaseAssetSerialized.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

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

    def create(releaseAsset: ReleaseAsset): Future[String] = add(ReleaseAsset(from = releaseAsset.from, to = releaseAsset.to, pegHash = releaseAsset.pegHash, gas = releaseAsset.gas, status = releaseAsset.status, txHash = releaseAsset.txHash, ticketID = releaseAsset.ticketID, mode = releaseAsset.mode, code = releaseAsset.code))

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def resetTransactionStatus(ticketID: String): Future[Int] = updateStatusByTicketID(ticketID, status = null)

    def getTransaction(ticketID: String): Future[ReleaseAsset] = findByTicketID(ticketID).map(_.deserialize)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val releaseAsset = Service.getTransaction(ticketID)

      def markDirty(releaseAsset: ReleaseAsset): Future[Unit] = {
        val markDirtyPegHash = blockchainAssets.Service.markDirty(releaseAsset.pegHash)
        val markDirtyFrom = blockchainAccounts.Service.markDirty(releaseAsset.from)
        for {
          _ <- markDirtyPegHash
          _ <- markDirtyFrom
        } yield {}
      }

      def getAssetID(pegHash: String): Future[String] = masterAssets.Service.tryGetIDByPegHash(pegHash)

      def getNegotiationListByAssetID(assetID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllByAssetID(assetID)

      def getAccountID(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

      def createTradeActivity(negotiationList: Seq[Negotiation]): Future[Seq[String]] = Future.sequence {
        negotiationList.map { negotiation =>
          masterTransactionTradeActivities.Service.create(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.ZONE_RELEASED_ASSET)
        }
      }

      (for {
        _ <- markTransactionSuccessful
        releaseAsset <- releaseAsset
        _ <- markDirty(releaseAsset)
        assetID <- getAssetID(releaseAsset.pegHash)
        negotiationList <- getNegotiationListByAssetID(assetID)
        fromAccountID <- getAccountID(releaseAsset.from)
        toAccountID <- getAccountID(releaseAsset.to)
        _ <- utilitiesNotification.send(toAccountID, constants.Notification.ZONE_RELEASED_ASSET, blockResponse.txhash)
        _ <- utilitiesNotification.send(fromAccountID, constants.Notification.ZONE_RELEASED_ASSET, blockResponse.txhash)
        _ <- createTradeActivity(negotiationList)
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
      val releaseAsset = Service.getTransaction(ticketID)

      def getAccountID(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

      (for {
        _ <- markTransactionFailed
        releaseAsset <- releaseAsset
        fromAccountID <- getAccountID(releaseAsset.from)
        _ <- utilitiesNotification.send(fromAccountID, constants.Notification.FAILURE, message)
      } yield {}).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  val scheduledTask = new Runnable {
    override def run(): Unit = {
      try {
       Await.result(transaction.ticketUpdater(Service.getTicketIDsOnStatus, Service.getTransactionHash, Service.getMode, Utility.onSuccess, Utility.onFailure), Duration.Inf)
      } catch {
        case exception: Exception => logger.error(exception.getMessage, exception)
      }
    }
  }

  if (kafkaEnabled || transactionMode != constants.Transactions.BLOCK_MODE) {
    actorSystem.scheduler.scheduleWithFixedDelay(initialDelay = schedulerInitialDelay, delay = schedulerInterval)(scheduledTask)(schedulerExecutionContext)
  }
}