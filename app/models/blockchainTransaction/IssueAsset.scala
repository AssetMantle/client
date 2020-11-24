package models.blockchainTransaction

import java.sql.Timestamp

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.Trait.Logged
import models.master.{Negotiation, Organization, Trader, Asset => masterAsset}
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.GetAccount
import queries.responses.AccountResponse
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse
import utilities.MicroNumber

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class IssueAsset(from: String, to: String, documentHash: String, assetType: String, assetPrice: MicroNumber, quantityUnit: String, assetQuantity: MicroNumber, moderated: Boolean, gas: MicroNumber, takerAddress: Option[String] = None, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends BaseTransaction[IssueAsset] with Logged {
  def mutateTicketID(newTicketID: String): IssueAsset = IssueAsset(from = from, to = to, documentHash = documentHash, assetType = assetType, assetPrice = assetPrice, quantityUnit = quantityUnit, assetQuantity = assetQuantity, moderated = moderated, gas = gas, takerAddress = takerAddress, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}

@Singleton
class IssueAssets @Inject()(
                             actorSystem: ActorSystem,
                             transaction: utilities.Transaction,
                             protected val databaseConfigProvider: DatabaseConfigProvider,
                             getAccount: GetAccount,
                             blockchainAssets: blockchain.Assets,
                             utilitiesNotification: utilities.Notification,
                             masterAssets: master.Assets,
                             blockchainAccounts: blockchain.Accounts,
                             masterNegotiations: master.Negotiations,
                             masterTraders: master.Traders,
                             masterOrganizations: master.Organizations
                           )(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  case class IssueAssetSerialized(from: String, to: String, documentHash: String, assetType: String, assetPrice: String, quantityUnit: String, assetQuantity: String, moderated: Boolean, gas: String, takerAddress: Option[String] = None, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: IssueAsset = IssueAsset(from = from, to = to, documentHash = documentHash, assetType = assetType, assetPrice = new MicroNumber(BigInt(assetPrice)), quantityUnit = quantityUnit, assetQuantity = new MicroNumber(BigInt(assetQuantity)), moderated = moderated, gas = new MicroNumber(BigInt(gas)), status = status, txHash = txHash, ticketID = ticketID, mode = mode, code = code, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(issueAsset: IssueAsset): IssueAssetSerialized = IssueAssetSerialized(from = issueAsset.from, to = issueAsset.to, documentHash = issueAsset.documentHash, assetType = issueAsset.assetType, assetPrice = issueAsset.assetPrice.toMicroString, quantityUnit = issueAsset.quantityUnit, assetQuantity = issueAsset.assetQuantity.toMicroString, moderated = issueAsset.moderated, gas = issueAsset.gas.toMicroString, takerAddress = issueAsset.takerAddress, status = issueAsset.status, txHash = issueAsset.txHash, ticketID = issueAsset.ticketID, mode = issueAsset.mode, code = issueAsset.code, createdBy = issueAsset.createdBy, createdOn = issueAsset.createdOn, createdOnTimeZone = issueAsset.createdOnTimeZone, updatedBy = issueAsset.updatedBy, updatedOn = issueAsset.updatedOn, updatedOnTimeZone = issueAsset.updatedOnTimeZone)

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_ISSUE_ASSET

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val issueAssetTable = TableQuery[IssueAssetTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(issueAssetSerialized: IssueAssetSerialized): Future[String] = db.run((issueAssetTable returning issueAssetTable.map(_.ticketID) += issueAssetSerialized).asTry).map {
    case Success(result) =>
      result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByTicketID(ticketID: String): Future[IssueAssetSerialized] = db.run(issueAssetTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(issueAssetTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(issueAssetTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(issueAssetTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusByTicketID(ticketID: String, status: Option[Boolean]): Future[Int] = db.run(issueAssetTable.filter(_.ticketID === ticketID).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(issueAssetTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(issueAssetTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(issueAssetTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByTicketID(ticketID: String) = db.run(issueAssetTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class IssueAssetTable(tag: Tag) extends Table[IssueAssetSerialized](tag, "IssueAsset") {

    def * = (from, to, documentHash, assetType, assetPrice, quantityUnit, assetQuantity, moderated, gas, takerAddress.?, status.?, txHash.?, ticketID, mode, code.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (IssueAssetSerialized.tupled, IssueAssetSerialized.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def documentHash = column[String]("documentHash")

    def assetType = column[String]("assetType")

    def assetPrice = column[String]("assetPrice")

    def quantityUnit = column[String]("quantityUnit")

    def assetQuantity = column[String]("assetQuantity")

    def moderated = column[Boolean]("moderated")

    def gas = column[String]("gas")

    def takerAddress = column[String]("takerAddress")

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

    def create(issueAsset: IssueAsset): Future[String] = add(serialize(IssueAsset(from = issueAsset.from, to = issueAsset.to, documentHash = issueAsset.documentHash, assetType = issueAsset.assetType, assetPrice = issueAsset.assetPrice, quantityUnit = issueAsset.quantityUnit, assetQuantity = issueAsset.assetQuantity, status = issueAsset.status, txHash = issueAsset.txHash, ticketID = issueAsset.ticketID, mode = issueAsset.mode, code = issueAsset.code, moderated = issueAsset.moderated, gas = issueAsset.gas, takerAddress = issueAsset.takerAddress)))

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def resetTransactionStatus(ticketID: String): Future[Int] = updateStatusByTicketID(ticketID, status = null)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[IssueAsset] = findByTicketID(ticketID).map(_.deserialize)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val issueAsset = Service.getTransaction(ticketID)

      def accountResponse(toAddress: String): Future[AccountResponse.Response] = getAccount.Service.get(toAddress)

      def getAccountIDByAddress(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

      def getTrader(accountID: String): Future[Trader] = masterTraders.Service.tryGetByAccountID(accountID)

      def getMasterAsset(traderID: String, documentHash: String): Future[masterAsset] = masterAssets.Service.getAllAssets(traderID).map(assets => assets.find(_.documentHash == documentHash).getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)))

      def create(bcAssets: Option[Seq[AccountResponse.Asset]], ownerAddress: String, masterAsset: masterAsset): Future[String] = {
        val asset = bcAssets match {
          case Some(bcAssets) => bcAssets.find(_.documentHash == masterAsset.documentHash).getOrElse(throw new BaseException(constants.Response.ASSET_NOT_FOUND))
          case None => throw new BaseException(constants.Response.ASSET_PEG_WALLET_NOT_FOUND)
        }
        blockchainAssets.Service.create(pegHash = asset.pegHash, documentHash = asset.documentHash, assetType = asset.assetType, assetPrice = asset.assetPrice, assetQuantity = asset.assetQuantity, quantityUnit = asset.quantityUnit, locked = asset.locked, moderated = asset.moderated, takerAddress = if (asset.takerAddress == "") null else Option(asset.takerAddress), ownerAddress = ownerAddress, dirtyBit = false)
      }

      def markAssetIssued(assetID: String, pegHash: String): Future[Int] = masterAssets.Service.markIssuedByID(id = assetID, pegHash = pegHash)

      def getNegotiations(assetID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllByAssetID(assetID)

      def updateNegotiationStatus(sellerAccountID: String, negotiations: Seq[Negotiation], masterAsset: masterAsset) = {

        def getIDByTraderID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

        Future.traverse(negotiations)(negotiation => {
          if (negotiation.status == constants.Status.Negotiation.ISSUE_ASSET_PENDING) {
            val markStatusRequestSent = masterNegotiations.Service.markStatusRequestSent(negotiation.id)
            for {
              _ <- markStatusRequestSent
              buyerAccountID <- getIDByTraderID(negotiation.buyerTraderID)
              _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.NEGOTIATION_REQUEST_SENT, masterAsset.description, masterAsset.assetType, masterAsset.quantity.toString, masterAsset.quantityUnit, masterAsset.price.toString)
              _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.NEGOTIATION_REQUEST_SENT, masterAsset.description, masterAsset.assetType, masterAsset.quantity.toString, masterAsset.quantityUnit, masterAsset.price.toString)
            } yield ()
          } else Future()
        })
      }

      def markAccountDirty(address: String): Future[Int] = blockchainAccounts.Service.markDirty(address)

      def getOrganization(organizationID: String): Future[Organization] = masterOrganizations.Service.tryGet(organizationID)

      (for {
        _ <- markTransactionSuccessful
        issueAsset <- issueAsset
        accountResponse <- accountResponse(issueAsset.to)
        ownerAccountID <- getAccountIDByAddress(issueAsset.to)
        seller <- getTrader(ownerAccountID)
        masterAsset <- getMasterAsset(traderID = seller.id, documentHash = issueAsset.documentHash)
        pegHash <- create(bcAssets = accountResponse.value.asset_peg_wallet, ownerAddress = issueAsset.to, masterAsset = masterAsset)
        _ <- markAssetIssued(assetID = masterAsset.id, pegHash = pegHash)
        negotiations <- getNegotiations(masterAsset.id)
        _ <- updateNegotiationStatus(sellerAccountID = ownerAccountID, negotiations = negotiations, masterAsset = masterAsset)
        _ <- markAccountDirty(issueAsset.from)
        fromAccountID <- getAccountIDByAddress(issueAsset.from)
        traderOrganization <- getOrganization(seller.organizationID)
        _ <- utilitiesNotification.send(ownerAccountID, constants.Notification.ASSET_ISSUED, blockResponse.txhash, masterAsset.description, masterAsset.assetType, masterAsset.quantity.toString, masterAsset.quantityUnit, masterAsset.price.toString)
        _ <- utilitiesNotification.send(traderOrganization.accountID, constants.Notification.ORGANIZATION_NOTIFY_ASSET_ISSUED, blockResponse.txhash, seller.accountID, masterAsset.description, masterAsset.assetType, masterAsset.quantity.toString, masterAsset.quantityUnit, masterAsset.price.toString)
        _ <- if (fromAccountID != ownerAccountID) utilitiesNotification.send(fromAccountID, constants.Notification.ZONE_NOTIFY_ASSET_ISSUED, blockResponse.txhash, traderOrganization.name, seller.accountID, masterAsset.description, masterAsset.assetType, masterAsset.quantity.toString, masterAsset.quantityUnit, masterAsset.price.toString) else Future(None)
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

    def onFailure(ticketID: String, message: String): Future[Unit] = {
      val markTransactionFailed = Service.markTransactionFailed(ticketID, message)
      val issueAsset = Service.getTransaction(ticketID)

      def getAccountIDByAddress(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

      def getTrader(accountID: String): Future[Trader] = masterTraders.Service.tryGetByAccountID(accountID)

      def getMasterAsset(traderID: String, documentHash: String): Future[masterAsset] = masterAssets.Service.getAllAssets(traderID).map(assets => assets.find(_.documentHash == documentHash).getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)))

      def markIssueAssetFailed(assetID: String): Future[Int] = masterAssets.Service.markIssueAssetFailed(assetID)

      def getNegotiations(assetID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllByAssetID(assetID)

      def updateNegotiationStatus(negotiations: Seq[Negotiation], masterAsset: masterAsset) = {

        def getIDByTraderID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

        Future.traverse(negotiations)(negotiation => {
          val markStatusIssueAssetRequestFailed = masterNegotiations.Service.markStatusIssueAssetRequestFailed(negotiation.id)
          for {
            _ <- markStatusIssueAssetRequestFailed
            sellerAccountID <- getIDByTraderID(negotiation.sellerTraderID)
            _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.NEGOTIATION_REQUEST_SENT_FAILED, masterAsset.description, masterAsset.assetType)
          } yield ()
        })
      }

      def getOrganization(organizationID: String): Future[Organization] = masterOrganizations.Service.tryGet(organizationID)

      (for {
        _ <- markTransactionFailed
        issueAsset <- issueAsset
        toAccountID <- getAccountIDByAddress(issueAsset.to)
        seller <- getTrader(toAccountID)
        masterAsset <- getMasterAsset(traderID = seller.id, documentHash = issueAsset.documentHash)
        negotiations <- getNegotiations(masterAsset.id)
        _ <- updateNegotiationStatus(negotiations = negotiations, masterAsset = masterAsset)
        _ <- markIssueAssetFailed(masterAsset.id)
        fromAccountID <- getAccountIDByAddress(issueAsset.from)
        traderOrganization <- getOrganization(seller.organizationID)
        _ <- utilitiesNotification.send(toAccountID, constants.Notification.ISSUE_ASSET_REQUEST_FAILED, message, masterAsset.description, masterAsset.assetType, masterAsset.quantity.toString, masterAsset.quantityUnit, masterAsset.price.toString)
        _ <- utilitiesNotification.send(traderOrganization.accountID, constants.Notification.ORGANIZATION_NOTIFY_ISSUE_ASSET_REQUEST_FAILED, message, seller.accountID, masterAsset.description, masterAsset.assetType, masterAsset.quantity.toString, masterAsset.quantityUnit, masterAsset.price.toString)
        _ <- if (fromAccountID != toAccountID) utilitiesNotification.send(fromAccountID, constants.Notification.ZONE_NOTIFY_ISSUE_ASSET_REQUEST_FAILED, message, masterAsset.description, masterAsset.assetType, masterAsset.quantity.toString, masterAsset.quantityUnit, masterAsset.price.toString) else Future(None)
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  val scheduledTask = new Runnable {
    override def run(): Unit = {
      Await.result(transaction.ticketUpdater(Service.getTicketIDsOnStatus, Service.getTransactionHash, Service.getMode, Utility.onSuccess, Utility.onFailure), Duration.Inf)
    }
  }

  if (kafkaEnabled || transactionMode != constants.Transactions.BLOCK_MODE) {
    actorSystem.scheduler.scheduleWithFixedDelay(initialDelay = schedulerInitialDelay, delay = schedulerInterval)(scheduledTask)(schedulerExecutionContext)
  }
}