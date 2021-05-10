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
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse
import utilities.MicroNumber

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class RedeemAsset(from: String, to: String, pegHash: String, gas: MicroNumber, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends BaseTransaction[RedeemAsset] with Logged {
  def mutateTicketID(newTicketID: String): RedeemAsset = RedeemAsset(from = from, to = to, pegHash = pegHash, gas = gas, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}

@Singleton
class RedeemAssets @Inject()(
                              actorSystem: ActorSystem,
                              transaction: utilities.Transaction,
                              protected val databaseConfigProvider: DatabaseConfigProvider,
                              blockchainAssets: blockchain.Assets,
                              blockchainAccounts: blockchain.Accounts,
                              utilitiesNotification: utilities.Notification,
                              masterAccounts: master.Accounts,
                              masterAssets: master.Assets,
                              masterNegotiations: master.Negotiations,
                            )(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_REDEEM_ASSET

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val redeemAssetTable = TableQuery[RedeemAssetTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  def serialize(redeemAsset: RedeemAsset): RedeemAssetSerialized = RedeemAssetSerialized(from = redeemAsset.from, to = redeemAsset.to, pegHash = redeemAsset.pegHash, gas = redeemAsset.gas.toMicroString, status = redeemAsset.status, txHash = redeemAsset.txHash, ticketID = redeemAsset.ticketID, mode = redeemAsset.mode, code = redeemAsset.code, createdBy = redeemAsset.createdBy, createdOn = redeemAsset.createdOn, createdOnTimeZone = redeemAsset.createdOnTimeZone, updatedBy = redeemAsset.updatedBy, updatedOn = redeemAsset.updatedOn, updatedOnTimeZone = redeemAsset.updatedOnTimeZone)

  case class RedeemAssetSerialized(from: String, to: String, pegHash: String, gas: String, status: Option[Boolean], txHash: Option[String], ticketID: String, mode: String, code: Option[String], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: RedeemAsset = RedeemAsset(from = from, to = to, pegHash = pegHash, gas = new MicroNumber(BigInt(gas)), status = status, txHash = txHash, ticketID = ticketID, mode = mode, code = code, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  private def add(redeemAsset: RedeemAsset): Future[String] = db.run((redeemAssetTable returning redeemAssetTable.map(_.ticketID) += serialize(redeemAsset)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByTicketID(ticketID: String): Future[RedeemAssetSerialized] = db.run(redeemAssetTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(redeemAssetTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(redeemAssetTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(redeemAssetTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusByTicketID(ticketID: String, status: Option[Boolean]): Future[Int] = db.run(redeemAssetTable.filter(_.ticketID === ticketID).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(redeemAssetTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def getTransactionByBuyerSellerAddressesAndPegHash(from: String, to: String, pegHash: String) = db.run(redeemAssetTable.filter(x => x.from === from && x.to === to && x.pegHash === pegHash).sortBy(x => x.updatedOn.ifNull(x.createdOn).desc).result.headOption)

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(redeemAssetTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByTicketID(ticketID: String) = db.run(redeemAssetTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(redeemAssetTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class RedeemAssetTable(tag: Tag) extends Table[RedeemAssetSerialized](tag, "RedeemAsset") {

    def * = (from, to, pegHash, gas, status.?, txHash.?, ticketID, mode, code.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (RedeemAssetSerialized.tupled, RedeemAssetSerialized.unapply)

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

    def create(redeemAsset: RedeemAsset): Future[String] = add(RedeemAsset(from = redeemAsset.from, to = redeemAsset.to, pegHash = redeemAsset.pegHash, gas = redeemAsset.gas, status = redeemAsset.status, txHash = redeemAsset.txHash, ticketID = redeemAsset.ticketID, mode = redeemAsset.mode, code = redeemAsset.code))

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def resetTransactionStatus(ticketID: String): Future[Int] = updateStatusByTicketID(ticketID, status = null)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[RedeemAsset] = findByTicketID(ticketID).map(_.deserialize)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

    def getTransactionStatus(buyerAddress: String, zoneAddress: String, pegHash: String) = getTransactionByBuyerSellerAddressesAndPegHash(buyerAddress, zoneAddress, pegHash).map(x => if (x.isDefined) x.get.status else Option(false))

  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val redeemAsset = Service.getTransaction(ticketID)

      def markRedeemed(pegHash: String): Future[Int] = masterAssets.Service.markRedeemedByPegHash(pegHash)

      def markDirty(redeemAsset: RedeemAsset): Future[Unit] = {
        val markAssetDirty = blockchainAssets.Service.markDirty(redeemAsset.pegHash)
        val markFromAccountDirty = blockchainAccounts.Service.markDirty(redeemAsset.from)
        for {
          _ <- markAssetDirty
          _ <- markFromAccountDirty
        } yield ()
      }

      def getAccountID(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)
      def assetID(pegHash: String): Future[String] = masterAssets.Service.tryGetIDByPegHash(pegHash)
      def negotiations(assetID:String): Future[Seq[models.master.Negotiation]] = masterNegotiations.Service.getAllByAssetID(assetID)

      (for {
        _ <- markTransactionSuccessful
        redeemAsset <- redeemAsset
        _ <- markRedeemed(redeemAsset.pegHash)
        _ <- markDirty(redeemAsset)
        fromAccountID <- getAccountID(redeemAsset.from)
        toAccountID <- getAccountID(redeemAsset.to)
        assetID<-assetID(redeemAsset.pegHash)
        negotiations<-negotiations(assetID)
        _ <- utilitiesNotification.send(fromAccountID, constants.Notification.REDEEM_ASSET_SUCCESSFUL, blockResponse.txhash)
        _ <- utilitiesNotification.send(toAccountID, constants.Notification.REDEEM_ASSET_SUCCESSFUL, blockResponse.txhash)
      } yield {
        negotiations.find(_.status == constants.Status.Negotiation.COMPLETED).map(negotiation=> actors.Service.cometActor ! actors.Message.makeCometMessage(username = fromAccountID, messageType = constants.Comet.NEGOTIATION, messageContent = actors.Message.Negotiation(negotiation.id))).getOrElse()
      }).recover {
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
      val redeemAsset = Service.getTransaction(ticketID)

      def getID(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

      (for {
        _ <- markTransactionFailed
        redeemAsset <- redeemAsset
        fromAccountID <- getID(redeemAsset.from)
        _ <- utilitiesNotification.send(fromAccountID, constants.Notification.BLOCKCHAIN_TRANSACTION_SEND_ASSET_TO_ORDER_FAILED, message)
      } yield ()).recover {
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