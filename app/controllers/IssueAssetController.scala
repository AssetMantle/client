package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction, WithoutLoginAction, WithoutLoginActionAsync}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.Asset
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import play.api.{Configuration, Logger}
import utilities.MicroInt

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IssueAssetController @Inject()(
                                      messagesControllerComponents: MessagesControllerComponents,
                                      masterTraders: master.Traders,
                                      transaction: utilities.Transaction,
                                      masterZones: master.Zones,
                                      blockchainAccounts: blockchain.Accounts,
                                      masterAssets: master.Assets,
                                      withZoneLoginAction: WithZoneLoginAction,
                                      transactionsIssueAsset: transactions.IssueAsset,
                                      blockchainTransactionIssueAssets: blockchainTransaction.IssueAssets,
                                      withUsernameToken: WithUsernameToken,
                                      withoutLoginAction: WithoutLoginAction,
                                      withoutLoginActionAsync: WithoutLoginActionAsync
                                    )(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ISSUE_ASSET

  def viewPendingIssueAssetRequests: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def traderIDsUnderZone(zoneID: String): Future[Seq[String]] = masterTraders.Service.getTraderIDsByZoneID(zoneID)

      def pendingIssueAssetRequests(traderIDs: Seq[String]): Future[Seq[Asset]] = masterAssets.Service.getPendingIssueAssetRequests(traderIDs)

      (for {
        zoneID <- zoneID
        traderIDs <- traderIDsUnderZone(zoneID)
        pendingIssueAssetRequests <- pendingIssueAssetRequests(traderIDs)
      } yield Ok(views.html.component.master.viewPendingIssueAssetRequests(pendingIssueAssetRequests))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def issueAssetForm(assetID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val asset = masterAssets.Service.tryGet(assetID)
      (for {
        asset <- asset
      } yield Ok(views.html.component.master.issueAssetOld(views.companion.master.IssueAssetOld.form.fill(views.companion.master.IssueAssetOld.Data(id = assetID, tarderID = asset.ownerID, documentHash = asset.documentHash, assetType = asset.assetType, assetPricePerUnit = asset.price / asset.quantity, quantityUnit = asset.quantityUnit, assetQuantity = asset.quantity, takerAddress = None, gas = constants.FormField.GAS.minimumValue, password = ""))))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def issueAsset: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.IssueAssetOld.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.issueAssetOld(formWithErrors)))
        },
        issueAssetData => {
          val traderAccountID = masterTraders.Service.tryGetAccountId(issueAssetData.tarderID)

          def toAddress(toAccountID: String): Future[String] = blockchainAccounts.Service.tryGetAddress(toAccountID)

          val verifyRequestedStatus = masterAssets.Service.verifyAssetPendingRequestStatus(issueAssetData.id)

          def getResult(toAddress: String, verifyRequestedStatus: Boolean): Future[Result] = {
            if (verifyRequestedStatus) {
              val ticketID = transaction.process[blockchainTransaction.IssueAsset, transactionsIssueAsset.Request](
                entity = blockchainTransaction.IssueAsset(from = loginState.address, to = toAddress, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPricePerUnit * issueAssetData.assetQuantity, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, moderated = true, gas = issueAssetData.gas, takerAddress = issueAssetData.takerAddress, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionIssueAssets.Service.create,
                request = transactionsIssueAsset.Request(transactionsIssueAsset.BaseReq(from = loginState.address, gas = issueAssetData.gas.toString), to = toAddress, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice =new MicroInt(issueAssetData.assetPricePerUnit * issueAssetData.assetQuantity).value.toString, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity.toString, moderated = true, takerAddress = issueAssetData.takerAddress.getOrElse(""), mode = transactionMode),
                action = transactionsIssueAsset.Service.post,
                onSuccess = blockchainTransactionIssueAssets.Utility.onSuccess,
                onFailure = blockchainTransactionIssueAssets.Utility.onFailure,
                updateTransactionHash = blockchainTransactionIssueAssets.Service.updateTransactionHash
              )

              for {
                ticketID <- ticketID
                result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ASSET_ISSUED)))
              } yield result
            } else {
              Future(PreconditionFailed(views.html.index(failures = Seq(constants.Response.ALL_ASSET_FILES_NOT_VERIFIED))))
            }
          }

          (for {
            traderAccountID <- traderAccountID
            toAddress <- toAddress(traderAccountID)
            verifyRequestedStatus <- verifyRequestedStatus
            result <- getResult(toAddress, verifyRequestedStatus)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def blockchainIssueAssetForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.issueAsset())
  }

  def blockchainIssueAsset: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.IssueAsset.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.issueAsset(formWithErrors)))
      },
      issueAssetData => {
        val post = transactionsIssueAsset.Service.post(transactionsIssueAsset.Request(transactionsIssueAsset.BaseReq(from = issueAssetData.from, gas = issueAssetData.gas.toString), to = issueAssetData.to, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = (issueAssetData.assetPricePerUnit * issueAssetData.assetQuantity).toString, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity.toString, moderated = issueAssetData.moderated, takerAddress = issueAssetData.takerAddress, mode = issueAssetData.mode))
        (for {
          _ <- post
        } yield Ok(views.html.index(successes = Seq(constants.Response.ASSET_ISSUED)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}