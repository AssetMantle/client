package utilities

import akka.actor.{ActorSystem, Cancellable}
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}
import models.{blockchainTransaction, master}

import scala.concurrent.duration._
import models.master.{Asset, Zone}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ZoneAutomation @Inject()(
                                masterTraders: master.Traders,
                                masterAssets: master.Assets,
                                masterZones: master.Zones,
                                masterAccounts: master.Accounts,
                                utilitiesTransaction: utilities.Transaction,
                                blockchainTransactionIssueAssets: blockchainTransaction.IssueAssets,
                                transactionsIssueAsset: transactions.IssueAsset,
                                utilitiesNotification: utilities.Notification,
                                actorSystem: ActorSystem,
                              )
                              (implicit
                               executionContext: ExecutionContext,
                               configuration: Configuration,
                              ) {
  private implicit val module: String = constants.Module.UTILITIES_ZONE_AUTOMATION

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private val zonePassword = configuration.get[String]("zone.password")

  private val zoneGas = configuration.get[Int]("zone.gas")

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")

  private def processIssueAssetRequests(zoneID: String, zoneAccountID: String, zoneWalletAddress: String): Unit = {

    val traderIDs = masterTraders.Service.getTraderIDsByZoneID(zoneID)

    def pendingIssueAssetRequests(traderIDs: Seq[String]): Future[Seq[Asset]] = masterAssets.Service.getPendingIssueAssetRequests(traderIDs)

    def issueAssets(pendingIssueAssetRequests: Seq[Asset]): Unit = {
      pendingIssueAssetRequests.foreach { issueAssetRequest =>
        def getAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

        def getAddress(accountID: String): Future[String] = masterAccounts.Service.getAddress(accountID)

        def getTakerAddress(takerID: Option[String]): Future[String] = {
          takerID match {
            case Some(takerID) =>
              for {
                takerAccountID <- getAccountID(takerID)
                takerAddress <- getAddress(takerAccountID)
              } yield takerAddress
            case None => Future("")
          }
        }

        def sendTransaction(toAddress: String, takerAddress: String): Future[String] = utilitiesTransaction.process[blockchainTransaction.IssueAsset, transactionsIssueAsset.Request](
          entity = blockchainTransaction.IssueAsset(from = zoneWalletAddress, to = toAddress, documentHash = issueAssetRequest.documentHash, assetType = issueAssetRequest.assetType, assetPrice = issueAssetRequest.price, quantityUnit = issueAssetRequest.quantityUnit, assetQuantity = issueAssetRequest.quantity, moderated = true, gas = zoneGas, takerAddress = Option(takerAddress), ticketID = "", mode = transactionMode),
          blockchainTransactionCreate = blockchainTransactionIssueAssets.Service.create,
          request = transactionsIssueAsset.Request(transactionsIssueAsset.BaseReq(from = zoneWalletAddress, gas = zoneGas.toString), to = toAddress, password = zonePassword, documentHash = issueAssetRequest.documentHash, assetType = issueAssetRequest.assetType, assetPrice = issueAssetRequest.price.toString, quantityUnit = issueAssetRequest.quantityUnit, assetQuantity = issueAssetRequest.quantity.toString, moderated = true, takerAddress = takerAddress, mode = transactionMode),
          action = transactionsIssueAsset.Service.post,
          onSuccess = blockchainTransactionIssueAssets.Utility.onSuccess,
          onFailure = blockchainTransactionIssueAssets.Utility.onFailure,
          updateTransactionHash = blockchainTransactionIssueAssets.Service.updateTransactionHash
        )

        def markAssetStatusAwaitingBlockchainResponse: Future[Int] = masterAssets.Service.markStatusAwaitingBlockchainResponse(issueAssetRequest.id)

        for {
          traderAccountID <- getAccountID(issueAssetRequest.ownerID)
          toAddress <- getAddress(traderAccountID)
          takerAddress <- getTakerAddress(issueAssetRequest.takerID)
          ticketID <- sendTransaction(toAddress = toAddress, takerAddress = takerAddress)
          _ <- markAssetStatusAwaitingBlockchainResponse
          _ <- utilitiesNotification.send(zoneAccountID, constants.Notification.ASSET_ISSUED, ticketID)
          _ <- utilitiesNotification.send(traderAccountID, constants.Notification.ASSET_ISSUED, ticketID)
        } yield ()
      }
    }

    (for {
      traderIDs <- traderIDs
      pendingIssueAssetRequests <- pendingIssueAssetRequests(traderIDs)
    } yield issueAssets(pendingIssueAssetRequests)
      ).recover {
      case baseException: BaseException => logger.info(baseException.failure.message)
    }

  }

  private val issueAssetRunnable = new Runnable {
    def run(): Unit = {
      val getAllVerifiedZones = masterZones.Service.getAllVerified

      def getAddress(accountID: String): Future[String] = masterAccounts.Service.getAddress(accountID)

      def startIssuingAssets(zones: Seq[Zone]): Unit = {
        zones.foreach(zone =>
          for {
            zoneWalletAddress <- getAddress(zone.accountID)
          } yield processIssueAssetRequests(zoneID = zone.id, zoneAccountID = zone.accountID, zoneWalletAddress = zoneWalletAddress)
        )
      }

      (for {
        verifiedZones <- getAllVerifiedZones
      } yield startIssuingAssets(verifiedZones)
        ).recover {
        case baseException: BaseException => logger.info(baseException.failure.message)
      }
    }
  }

  def start(): Cancellable = {
    actorSystem.scheduler.scheduleWithFixedDelay(initialDelay = schedulerInitialDelay, delay = schedulerInterval)(issueAssetRunnable)(schedulerExecutionContext)
  }

}
