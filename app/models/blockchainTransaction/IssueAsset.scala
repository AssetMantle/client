package models.blockchainTransaction

import java.net.ConnectException
import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.master.{Asset, Negotiation}
import models.masterTransaction.IssueAssetRequest
import models.{blockchain, master, masterTransaction}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.GetAccount
import queries.responses.AccountResponse
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class IssueAsset(from: String, to: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, moderated: Boolean, gas: Int, takerAddress: Option[String] = None, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None) extends BaseTransaction[IssueAsset] {
  def mutateTicketID(newTicketID: String): IssueAsset = IssueAsset(from = from, to = to, documentHash = documentHash, assetType = assetType, assetPrice = assetPrice, quantityUnit = quantityUnit, assetQuantity = assetQuantity, moderated = moderated, gas = gas, takerAddress = takerAddress, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}

@Singleton
class IssueAssets @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, getAccount: GetAccount, blockchainAssets: blockchain.Assets, transactionIssueAsset: transactions.IssueAsset, utilitiesNotification: utilities.Notification, masterAccounts: master.Accounts, masterAssets: master.Assets, blockchainAccounts: blockchain.Accounts, masterNegotiations: master.Negotiations, masterTraders: master.Traders)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_ISSUE_ASSET

  private implicit val logger: Logger = Logger(this.getClass)
  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db
  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val issueAssetTable = TableQuery[IssueAssetTable]
  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")
  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(issueAsset: IssueAsset): Future[String] = db.run((issueAssetTable returning issueAssetTable.map(_.ticketID) += issueAsset).asTry).map {
    case Success(result) =>
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

    def * = (from, to, documentHash, assetType, assetPrice, quantityUnit, assetQuantity, moderated, gas, takerAddress.?, status.?, txHash.?, ticketID, mode, code.?) <> (IssueAsset.tupled, IssueAsset.unapply)

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

    def create(issueAsset: IssueAsset): Future[String] = add(IssueAsset(from = issueAsset.from, to = issueAsset.to, documentHash = issueAsset.documentHash, assetType = issueAsset.assetType, assetPrice = issueAsset.assetPrice, quantityUnit = issueAsset.quantityUnit, assetQuantity = issueAsset.assetQuantity, status = issueAsset.status, txHash = issueAsset.txHash, ticketID = issueAsset.ticketID, mode = issueAsset.mode, code = issueAsset.code, moderated = issueAsset.moderated, gas = issueAsset.gas, takerAddress = issueAsset.takerAddress))

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[IssueAsset] = findByTicketID(ticketID)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val issueAsset = Service.getTransaction(ticketID)

      def responseAccount(toAddress: String): Future[AccountResponse.Response] = getAccount.Service.get(toAddress)

      val asset = masterAssets.Service.tryGetByTicketID(ticketID)

      def negotiations(assetID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllByAssetID(assetID)

      def insertOrUpdate(responseAccount: queries.responses.AccountResponse.Response, asset: Asset, issueAsset: IssueAsset, negotiations: Seq[Negotiation]) = {
        responseAccount.value.assetPegWallet match {
          case Some(bcAssets) => Future.sequence {
            bcAssets.map { bcAsset =>
              val upsert = blockchainAssets.Service.insertOrUpdate(pegHash = bcAsset.pegHash, documentHash = bcAsset.documentHash, assetType = bcAsset.assetType, assetPrice = bcAsset.assetPrice, assetQuantity = bcAsset.assetQuantity, quantityUnit = bcAsset.quantityUnit, locked = bcAsset.locked, moderated = bcAsset.moderated, takerAddress = if (bcAsset.takerAddress == "") null else Option(bcAsset.takerAddress), ownerAddress = issueAsset.to, dirtyBit = true)

              def markAssetIssued: Future[Int] = {
                if (asset.documentHash == bcAsset.documentHash) {
                  masterAssets.Service.markIssuedByID(id = asset.id, pegHash = bcAsset.pegHash)
                } else {
                  Future(0)
                }
              }

              for {
                _ <- upsert
                _ <- markAssetIssued
              } yield Future()
            }
          }
          case None => Future()
        }
      }

      def getIDByTraderID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

      def updateNegotiationStatus(negotiations: Seq[Negotiation], asset: Asset): Future[Int] = {
        negotiations.map { negotiation =>
          if (negotiation.status == constants.Status.Negotiation.ISSUE_ASSET_PENDING || negotiation.status == constants.Status.Negotiation.REQUEST_SENDING_WAITING_FOR_ISSUE_ASSET) {
            val markStatusRequestSent = masterNegotiations.Service.markStatusRequestSent(negotiation.id)

            for {
              _ <- markStatusRequestSent
              sellerAccountID <- getIDByTraderID(negotiation.sellerTraderID)
              buyerAccountID <- getIDByTraderID(negotiation.buyerTraderID)
              _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.NEGOTIATION_REQUEST_SENT, asset.description, asset.assetType, asset.quantity.toString(), asset.quantityUnit, asset.price.toString())
              _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.NEGOTIATION_REQUEST_SENT, asset.description, asset.assetType, asset.quantity.toString(), asset.quantityUnit, asset.price.toString())
            } yield Unit
          }
        }
        Future(0)
      }

      def markAccountDirty(from: String): Future[Int] = blockchainAccounts.Service.markDirty(from)

      def getIDByAddress(address: String): Future[String] = masterAccounts.Service.getId(address)

      (for {
        _ <- markTransactionSuccessful
        issueAsset <- issueAsset
        responseAccount <- responseAccount(issueAsset.to)
        asset <- asset
        negotiations <- negotiations(asset.id)
        _ <- updateNegotiationStatus(negotiations, asset)
        _ <- insertOrUpdate(responseAccount = responseAccount, asset = asset, issueAsset = issueAsset, negotiations = negotiations)
        _ <- markAccountDirty(issueAsset.from)
        fromAccountID <- getIDByAddress(issueAsset.from)
        toAccountID <- getIDByAddress(issueAsset.to)
        _ <- utilitiesNotification.send(toAccountID, constants.Notification.ASSET_ISSUED, blockResponse.txhash, asset.description, asset.assetType, asset.quantity.toString(), asset.quantityUnit, asset.price.toString())
        _ <- if (fromAccountID != toAccountID) utilitiesNotification.send(fromAccountID, constants.Notification.ASSET_ISSUED, blockResponse.txhash, asset.description, asset.assetType, asset.quantity.toString(), asset.quantityUnit, asset.price.toString()) else Future(None)
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
        case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = {
      val markTransactionFailed = Service.markTransactionFailed(ticketID, message)
      val issueAsset = Service.getTransaction(ticketID)
      val asset = masterAssets.Service.tryGetByTicketID(ticketID)

      def markIssueAssetRejected(assetID: String): Future[Int] = masterAssets.Service.markIssueAssetRejected(assetID)

      def getIDByAddress(address: String): Future[String] = masterAccounts.Service.getId(address)

      def negotiations(assetID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllByAssetID(assetID)

      def getIDByTraderID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

      def updateNegotiationStatus(negotiations: Seq[Negotiation], asset: Asset): Future[Int] = {
        negotiations.map { negotiation =>
          if (negotiation.status == constants.Status.Negotiation.ISSUE_ASSET_PENDING || negotiation.status == constants.Status.Negotiation.REQUEST_SENDING_WAITING_FOR_ISSUE_ASSET) {
            val markStatusIssueAssetRequestFailed = masterNegotiations.Service.markStatusIssueAssetRequestFailed(negotiation.id)

            for {
              _ <- markStatusIssueAssetRequestFailed
              sellerAccountID <- getIDByTraderID(negotiation.sellerTraderID)
              buyerAccountID <- getIDByTraderID(negotiation.buyerTraderID)
              _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.NEGOTIATION_REQUEST_SENT_FAILED, asset.description, asset.assetType)
              _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.NEGOTIATION_REQUEST_SENT_FAILED, asset.description, asset.assetType)
            } yield Unit
          }
        }
        Future(0)
      }

      (for {
        _ <- markTransactionFailed
        issueAsset <- issueAsset
        asset <- asset
        negotiations <- negotiations(asset.id)
        _ <- updateNegotiationStatus(negotiations = negotiations, asset = asset)
        _ <- markIssueAssetRejected(asset.id)
        fromAccountID <- getIDByAddress(issueAsset.from)
        toAccountID <- getIDByAddress(issueAsset.to)
        _ <- utilitiesNotification.send(toAccountID, constants.Notification.ISSUE_ASSET_REQUEST_FAILED, message, asset.description, asset.assetType, asset.quantity.toString(), asset.quantityUnit, asset.price.toString())
        _ <- if (fromAccountID != toAccountID) utilitiesNotification.send(fromAccountID, constants.Notification.ISSUE_ASSET_REQUEST_FAILED, message, asset.description, asset.assetType, asset.quantity.toString(), asset.quantityUnit, asset.price.toString()) else Future(None)
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