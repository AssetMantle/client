package controllers

import controllers.actions.{LoginState, WithLoginAction, WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.{Asset, Trader}
import models.masterTransaction.AssetFile
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AssetController @Inject()(
                                 blockchainAssets: blockchain.Assets,
                                 blockchainTransactionIssueAssets: blockchainTransaction.IssueAssets,
                                 masterAccounts: master.Accounts,
                                 masterTraders: master.Traders,
                                 masterTradeRelations: master.TraderRelations,
                                 masterZones: master.Zones,
                                 masterAssets: master.Assets,
                                 messagesControllerComponents: MessagesControllerComponents,
                                 withTraderLoginAction: WithTraderLoginAction,
                                 withLoginAction: WithLoginAction,
                                 withUsernameToken: WithUsernameToken,
                                 transaction: utilities.Transaction,
                                 transactionsIssueAsset: transactions.IssueAsset,
                                 utilitiesNotification: utilities.Notification,
                                 withZoneLoginAction: WithZoneLoginAction,
                                 masterTransactionAssetFiles: masterTransaction.AssetFiles,
                                 transactionsReleaseAsset: transactions.ReleaseAsset,
                                 blockchainTransactionReleaseAssets: blockchainTransaction.ReleaseAssets,
                                 masterTransactionTradeActivities: masterTransaction.TradeActivities,
                               )
                               (implicit
                                executionContext: ExecutionContext,
                                configuration: Configuration
                               ) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_ASSET

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  def issueForm(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.issueAsset())
  }

  def issue(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.IssueAsset.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.issueAsset(formWithErrors)))
        },
        issueAssetData => {
          (loginState.acl match {
            case Some(acl) => {
              if (acl.issueAsset) {
                val traderID = masterTraders.Service.tryGetID(loginState.username)

                def getResult(traderID: String): Future[Result] = {

                  def getAllTradableAssets(traderID: String): Future[Seq[Asset]] = masterAssets.Service.getAllTradableAssets(traderID)

                  def getCounterPartyList(traderID: String): Future[Seq[String]] = masterTradeRelations.Service.getAllCounterParties(traderID)

                  def getCounterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

                  if (issueAssetData.moderated) {
                    val addModeratedAsset = masterAssets.Service.addModerated(ownerID = traderID, assetType = issueAssetData.assetType, description = issueAssetData.description, quantity = issueAssetData.quantity, quantityUnit = issueAssetData.quantityUnit, price = issueAssetData.price, shippingPeriod = issueAssetData.shippingPeriod, portOfLoading = issueAssetData.portOfLoading, portOfDischarge = issueAssetData.portOfDischarge)

                    for {
                      _ <- addModeratedAsset
                      tradableAssets <- getAllTradableAssets(traderID)
                      counterPartyList <- getCounterPartyList(traderID)
                      counterPartyTraders <- getCounterPartyTraders(counterPartyList)
                      result <- withUsernameToken.PartialContent(views.html.component.master.negotiationRequest(tradableAssets = tradableAssets, counterPartyTraders = counterPartyTraders))
                    } yield result
                  } else {
                    val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = issueAssetData.password.getOrElse(""))

                    def issueAssetAndGetResult(validateUsernamePassword: Boolean): Future[Result] = if (validateUsernamePassword) {
                      val addUnmoderatedAsset = masterAssets.Service.addUnmoderated(ownerID = traderID, assetType = issueAssetData.assetType, description = issueAssetData.description, quantity = issueAssetData.quantity, quantityUnit = issueAssetData.quantityUnit, price = issueAssetData.price, shippingPeriod = issueAssetData.shippingPeriod, portOfLoading = issueAssetData.portOfLoading, portOfDischarge = issueAssetData.portOfDischarge)

                      def sendTransaction(documentHash: String): Future[String] = transaction.process[blockchainTransaction.IssueAsset, transactionsIssueAsset.Request](
                        entity = blockchainTransaction.IssueAsset(from = loginState.address, to = loginState.address, documentHash = documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.price, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.quantity, moderated = false, takerAddress = None, gas = issueAssetData.gas.getOrElse(throw new BaseException(constants.Response.GAS_NOT_GIVEN)), ticketID = "", mode = transactionMode),
                        blockchainTransactionCreate = blockchainTransactionIssueAssets.Service.create,
                        request = transactionsIssueAsset.Request(transactionsIssueAsset.BaseReq(from = loginState.address, gas = issueAssetData.gas.getOrElse(throw new BaseException(constants.Response.GAS_NOT_GIVEN)).toString), to = loginState.address, password = issueAssetData.password.getOrElse(throw new BaseException(constants.Response.PASSWORD_NOT_GIVEN)), documentHash = documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.price.toString, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.quantity.toString, moderated = false, takerAddress = "", mode = transactionMode),
                        action = transactionsIssueAsset.Service.post,
                        onSuccess = blockchainTransactionIssueAssets.Utility.onSuccess,
                        onFailure = blockchainTransactionIssueAssets.Utility.onFailure,
                        updateTransactionHash = blockchainTransactionIssueAssets.Service.updateTransactionHash
                      )

                      for {
                        documentHash <- addUnmoderatedAsset
                        ticketID <- sendTransaction(documentHash)
                        tradableAssets <- getAllTradableAssets(traderID)
                        counterPartyList <- getCounterPartyList(traderID)
                        counterPartyTraders <- getCounterPartyTraders(counterPartyList)
                        result <- withUsernameToken.PartialContent(views.html.component.master.negotiationRequest(tradableAssets = tradableAssets, counterPartyTraders = counterPartyTraders))
                      } yield result
                    }
                    else {
                      Future(BadRequest(views.html.component.master.issueAsset(views.companion.master.IssueAsset.form.fill(issueAssetData).withGlobalError(constants.Response.INCORRECT_PASSWORD.message))))
                    }

                    for {
                      validateUsernamePassword <- validateUsernamePassword
                      result <- issueAssetAndGetResult(validateUsernamePassword)
                    } yield result
                  }
                }

                for {
                  traderID <- traderID
                  result <- getResult(traderID)
                } yield result
              } else {
                throw new BaseException(constants.Response.UNAUTHORIZED)
              }
            }
            case None => {
              throw new BaseException(constants.Response.UNAUTHORIZED)
            }
          }).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def releaseForm(assetID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.releaseAsset(assetID = assetID))
  }

  def release: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ReleaseAsset.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.releaseAsset(formWithErrors, formWithErrors.data(constants.FormField.ASSET_ID.name))))
        },
        releaseData => {
          val zoneID = masterZones.Service.tryGetID(loginState.username)
          val asset = masterAssets.Service.tryGet(releaseData.assetID)
          val billOfLading = masterTransactionAssetFiles.Service.tryGet(id = releaseData.assetID, documentType = constants.File.BILL_OF_LADING)

          def getTrader(traderID: String): Future[Trader] = masterTraders.Service.tryGet(traderID)

          def getAddress(accountID: String): Future[String] = masterAccounts.Service.tryGetAddress(accountID)

          def getLockedStatus(pegHash: Option[String]): Future[Boolean] = if (pegHash.isDefined) blockchainAssets.Service.tryGetLockedStatus(pegHash.get) else throw new BaseException(constants.Response.ASSET_NOT_FOUND)

          def sendTransaction(seller: Trader, zoneID: String, sellerAddress: String, billOfLading: AssetFile, asset: Asset, lockedStatus: Boolean): Future[String] = {
            if (seller.zoneID != zoneID || asset.ownerID != seller.id) throw new BaseException(constants.Response.UNAUTHORIZED)
            if (!lockedStatus) throw new BaseException(constants.Response.ASSET_ALREADY_UNLOCKED)
            if (billOfLading.status.isEmpty) throw new BaseException(constants.Response.BILL_OF_LADING_VERIFICATION_STATUS_PENDING)
            if (billOfLading.status.isDefined && !billOfLading.status.get) throw new BaseException(constants.Response.BILL_OF_LADING_REJECTED)
            transaction.process[blockchainTransaction.ReleaseAsset, transactionsReleaseAsset.Request](
              entity = blockchainTransaction.ReleaseAsset(from = loginState.address, to = sellerAddress, pegHash = asset.pegHash.getOrElse(throw new BaseException(constants.Response.ASSET_NOT_FOUND)), gas = releaseData.gas, ticketID = "", mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionReleaseAssets.Service.create,
              request = transactionsReleaseAsset.Request(transactionsReleaseAsset.BaseReq(from = loginState.address, gas = releaseData.gas.toString), to = sellerAddress, password = releaseData.password, pegHash = asset.pegHash.getOrElse(throw new BaseException(constants.Response.ASSET_NOT_FOUND)), mode = transactionMode),
              action = transactionsReleaseAsset.Service.post,
              onSuccess = blockchainTransactionReleaseAssets.Utility.onSuccess,
              onFailure = blockchainTransactionReleaseAssets.Utility.onFailure,
              updateTransactionHash = blockchainTransactionReleaseAssets.Service.updateTransactionHash
            )
          }

          (for {
            zoneID <- zoneID
            asset <- asset
            seller <- getTrader(asset.ownerID)
            lockedStatus <- getLockedStatus(asset.pegHash)
            billOfLading <- billOfLading
            sellerAddress <- getAddress(seller.accountID)
            ticketID <- sendTransaction(seller = seller, zoneID = zoneID, sellerAddress = sellerAddress, billOfLading = billOfLading, asset = asset, lockedStatus = lockedStatus)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.ZONE_RELEASED_ASSET, ticketID)
            _ <- utilitiesNotification.send(seller.accountID, constants.Notification.ZONE_RELEASED_ASSET, ticketID)
            result <- withUsernameToken.Ok(views.html.trades(successes = Seq(constants.Response.ZONE_RELEASED_ASSET)))
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

}
