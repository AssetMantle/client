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
import queries.GetAccount
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class IssueAsset(from: String, to: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, moderated: Boolean, gas: Int, takerAddress: Option[String] = None, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None) extends BaseTransaction[IssueAsset] {
  def mutateTicketID(newTicketID: String): IssueAsset = IssueAsset(from = from, to = to, documentHash = documentHash, assetType = assetType, assetPrice = assetPrice, quantityUnit = quantityUnit, assetQuantity = assetQuantity, moderated = moderated, gas = gas, takerAddress = takerAddress, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}


@Singleton
class IssueAssets @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, getAccount: GetAccount, blockchainAssets: blockchain.Assets, transactionIssueAsset: transactions.IssueAsset,utilitiesNotification: utilities.Notification, masterAccounts: master.Accounts, masterTransactionIssueAssetRequests: masterTransaction.IssueAssetRequests, blockchainAccounts: blockchain.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_ISSUE_ASSET

  private implicit val logger: Logger = Logger(this.getClass)

  private val schedulerExecutionContext:ExecutionContext= actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val issueAssetTable = TableQuery[IssueAssetTable]
  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")
  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(issueAsset: IssueAsset): Future[String] = db.run((issueAssetTable returning issueAssetTable.map(_.ticketID) += issueAsset).asTry).map {
    case Success(result) =>
      println(Thread.currentThread().getName)
      result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(issueAsset: IssueAsset): Future[Int] = db.run(issueAssetTable.insertOrUpdate(issueAsset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String): Future[IssueAsset] = db.run(issueAssetTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(issueAssetTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(issueAssetTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(issueAssetTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(issueAssetTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(issueAssetTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(issueAssetTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByTicketID(ticketID: String) = db.run(issueAssetTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class IssueAssetTable(tag: Tag) extends Table[IssueAsset](tag, "IssueAsset") {

    def * = (from, to, documentHash, assetType, assetPrice, quantityUnit, assetQuantity, moderated, gas,takerAddress.?, status.?, txHash.?, ticketID, mode, code.?) <> (IssueAsset.tupled, IssueAsset.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def documentHash = column[String]("documentHash")

    def assetType = column[String]("assetType")

    def assetPrice = column[Int]("assetPrice")

    def quantityUnit = column[String]("quantityUnit")

    def assetQuantity = column[Int]("assetQuantity")

    def moderated = column[Boolean]("moderated")

    def gas = column[Int]("gas")

    def takerAddress = column[String]("takerAddress")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def mode = column[String]("mode")

    def code = column[String]("code")
  }

  object Service {

    def create(issueAsset: IssueAsset): String = Await.result(add(IssueAsset(from = issueAsset.from, to = issueAsset.to, documentHash = issueAsset.documentHash, assetType = issueAsset.assetType, assetPrice = issueAsset.assetPrice, quantityUnit = issueAsset.quantityUnit, assetQuantity = issueAsset.assetQuantity, status = issueAsset.status, txHash = issueAsset.txHash, ticketID = issueAsset.ticketID, mode = issueAsset.mode, code = issueAsset.code, moderated = issueAsset.moderated, gas = issueAsset.gas, takerAddress = issueAsset.takerAddress)), Duration.Inf)

    def markTransactionSuccessful(ticketID: String, txHash: String): Int = Await.result(updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true)), Duration.Inf)

    def markTransactionFailed(ticketID: String, code: String): Int = Await.result(updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus, Duration.Inf)

    def getTransaction(ticketID: String): IssueAsset = Await.result(findByTicketID(ticketID), Duration.Inf)

    def getTransactionHash(ticketID: String): Option[String] = Await.result(findTransactionHashByTicketID(ticketID), Duration.Inf)

    def getMode(ticketID: String): String = Await.result(findModeByTicketID(ticketID), Duration.Inf)

    def updateTransactionHash(ticketID: String, txHash: String): Int = Await.result(updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash)), Duration.Inf)

  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = Future {
      try {
        Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
        val issueAsset = Service.getTransaction(ticketID)
        Thread.sleep(sleepTime)
        val responseAccount = getAccount.Service.get(issueAsset.to)
        val assetRequest = masterTransactionIssueAssetRequests.Service.getIssueAssetByTicketID(ticketID)
        responseAccount.value.assetPegWallet.foreach(assets => assets.foreach(asset => {
          blockchainAssets.Service.insertOrUpdate(pegHash = asset.pegHash, documentHash = asset.documentHash, assetType = asset.assetType, assetPrice = asset.assetPrice, assetQuantity = asset.assetQuantity, quantityUnit = asset.quantityUnit, locked = asset.locked, moderated = asset.moderated, takerAddress = if (asset.takerAddress == "") null else Option(asset.takerAddress), ownerAddress = issueAsset.to, dirtyBit = true)
          if(assetRequest.documentHash == asset.documentHash){
            masterTransactionIssueAssetRequests.Service.updatePegHashAndStatusByTicketID(ticketID, Option(asset.pegHash), constants.Status.Asset.LISTED_FOR_TRADE)
          }
        }))
        blockchainAccounts.Service.markDirty(issueAsset.from)
        utilitiesNotification.send(masterAccounts.Service.getId(issueAsset.to), constants.Notification.SUCCESS, blockResponse.txhash)
        utilitiesNotification.send(masterAccounts.Service.getId(issueAsset.from), constants.Notification.SUCCESS, blockResponse.txhash)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
        case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.markTransactionFailed(ticketID, message)
        val issueAsset = Service.getTransaction(ticketID)
        masterTransactionIssueAssetRequests.Service.updatePegHashAndStatusByTicketID(ticketID, None, constants.Status.Asset.FAILED)
        utilitiesNotification.send(masterAccounts.Service.getId(issueAsset.to), constants.Notification.FAILURE, message)
        utilitiesNotification.send(masterAccounts.Service.getId(issueAsset.from), constants.Notification.FAILURE, message)
      } catch {
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