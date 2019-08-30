package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class IssueAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, blockchainAclAccounts: blockchain.ACLAccounts, masterZones: master.Zones, masterAccounts: master.Accounts, withTraderLoginAction: WithTraderLoginAction, withZoneLoginAction: WithZoneLoginAction, blockchainAssets: blockchain.Assets, transactionsIssueAsset: transactions.IssueAsset, blockchainTransactionIssueAssets: blockchainTransaction.IssueAssets, masterTransactionIssueAssetRequests: masterTransaction.IssueAssetRequests, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  def issueAssetRequestForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.issueAssetRequest(views.companion.master.IssueAssetRequest.form))
  }

  def issueAssetRequest: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.IssueAssetRequest.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.issueAssetRequest(formWithErrors))
        },
        issueAssetRequestData => {
          try {
            if (!issueAssetRequestData.moderated) {
              transaction.process[blockchainTransaction.IssueAsset, transactionsIssueAsset.Request](
                entity = blockchainTransaction.IssueAsset(from = loginState.address, to = loginState.address, documentHash = issueAssetRequestData.documentHash, assetType = issueAssetRequestData.assetType, assetPrice = issueAssetRequestData.assetPrice, quantityUnit = issueAssetRequestData.quantityUnit, assetQuantity = issueAssetRequestData.assetQuantity, moderated = issueAssetRequestData.moderated,gas=issueAssetRequestData.gas,takerAddress = if (issueAssetRequestData.takerAddress == "") null else Option(issueAssetRequestData.takerAddress), status = null, txHash = null, ticketID = "", mode = transactionMode, code = null),
                blockchainTransactionCreate = blockchainTransactionIssueAssets.Service.create,
                request = transactionsIssueAsset.Request(transactionsIssueAsset.BaseRequest(from = loginState.address), to = loginState.address, password = issueAssetRequestData.password, documentHash = issueAssetRequestData.documentHash, assetType = issueAssetRequestData.assetType, assetPrice = issueAssetRequestData.assetPrice.toString, quantityUnit = issueAssetRequestData.quantityUnit, assetQuantity = issueAssetRequestData.assetQuantity.toString, moderated = issueAssetRequestData.moderated, gas=issueAssetRequestData.gas.toString,takerAddress = issueAssetRequestData.takerAddress, mode = transactionMode),
                action = transactionsIssueAsset.Service.post,
                onSuccess = blockchainTransactionIssueAssets.Utility.onSuccess,
                onFailure = blockchainTransactionIssueAssets.Utility.onFailure,
                updateTransactionHash = blockchainTransactionIssueAssets.Service.updateTransactionHash
              )
              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ASSET_ISSUED)))
            } else {
              masterTransactionIssueAssetRequests.Service.create(accountID = loginState.username, documentHash = issueAssetRequestData.documentHash, assetPrice = issueAssetRequestData.assetPrice, assetType = issueAssetRequestData.assetType, quantityUnit = issueAssetRequestData.quantityUnit, assetQuantity = issueAssetRequestData.assetQuantity, takerAddress = if (issueAssetRequestData.takerAddress == "") null else Option(issueAssetRequestData.takerAddress))
              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ISSUE_ASSET_REQUEST_SENT)))
            }
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
          }
        }
      )
  }

  def viewPendingIssueAssetRequests: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.viewPendingIssueAssetRequests(masterTransactionIssueAssetRequests.Service.getPendingIssueAssetRequests(masterAccounts.Service.getIDsForAddresses(blockchainAclAccounts.Service.getAddressesUnderZone(masterZones.Service.getZoneId(loginState.username))))))
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def rejectIssueAssetRequestForm(requestID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.rejectIssueAssetRequest(views.companion.master.RejectIssueAssetRequest.form, requestID))
  }

  def rejectIssueAssetRequest: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RejectIssueAssetRequest.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.rejectIssueAssetRequest(formWithErrors, formWithErrors.data(constants.Form.REQUEST_ID)))
        },
        rejectIssueAssetRequestData => {
          try {
            masterTransactionIssueAssetRequests.Service.reject(id = rejectIssueAssetRequestData.requestID, comment = rejectIssueAssetRequestData.comment)
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ISSUE_ASSET_REQUEST_REJECTED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def issueAssetForm(requestID: String, accountID: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, takerAddress: Option[String]): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.issueAsset(views.companion.master.IssueAsset.form, requestID, accountID, documentHash, assetType, assetPrice, quantityUnit, assetQuantity, takerAddress))
  }

  def issueAsset: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.IssueAsset.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.issueAsset(formWithErrors, formWithErrors.data(constants.Form.REQUEST_ID), formWithErrors.data(constants.Form.ACCOUNT_ID), formWithErrors.data(constants.Form.DOCUMENT_HASH), formWithErrors.data(constants.Form.ASSET_TYPE), formWithErrors.data(constants.Form.ASSET_PRICE).toInt, formWithErrors.data(constants.Form.QUANTITY_UNIT), formWithErrors.data(constants.Form.ASSET_QUANTITY).toInt, Option(formWithErrors.data(constants.Form.TAKER_ADDRESS))))
        },
        issueAssetData => {
          try {
            val toAddress = masterAccounts.Service.getAddress(issueAssetData.accountID)
            if (masterTransactionIssueAssetRequests.Service.getStatus(issueAssetData.requestID).isEmpty) {
              val ticketID = transaction.process[blockchainTransaction.IssueAsset, transactionsIssueAsset.Request](
                entity = blockchainTransaction.IssueAsset(from = loginState.address, to = toAddress, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, moderated = true, gas=issueAssetData.gas,takerAddress = if (issueAssetData.takerAddress == "") null else Option(issueAssetData.takerAddress), status = null, txHash = null, ticketID = "", mode = transactionMode, code = null),
                blockchainTransactionCreate = blockchainTransactionIssueAssets.Service.create,
                request = transactionsIssueAsset.Request(transactionsIssueAsset.BaseRequest(from = loginState.address), to = toAddress, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice.toString, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity.toString, moderated = true,gas=issueAssetData.gas.toString, takerAddress = issueAssetData.takerAddress, mode = transactionMode),
                action = transactionsIssueAsset.Service.post,
                onSuccess = blockchainTransactionIssueAssets.Utility.onSuccess,
                onFailure = blockchainTransactionIssueAssets.Utility.onFailure,
                updateTransactionHash = blockchainTransactionIssueAssets.Service.updateTransactionHash
              )
              masterTransactionIssueAssetRequests.Service.accept(issueAssetData.requestID, ticketID)
              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ASSET_ISSUED)))
            } else {
              Unauthorized(views.html.index(failures = Seq(constants.Response.REQUEST_ALREADY_APPROVED_OR_REJECTED)))
            }
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
          }
        }
      )
  }

  def blockchainIssueAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.issueAsset(views.companion.blockchain.IssueAsset.form))
  }

  def blockchainIssueAsset: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.IssueAsset.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.issueAsset(formWithErrors))
      },
      issueAssetData => {
        try {
          transactionsIssueAsset.Service.post(transactionsIssueAsset.Request(transactionsIssueAsset.BaseRequest(from = issueAssetData.from), to = issueAssetData.to, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice.toString, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity.toString, moderated = issueAssetData.moderated, takerAddress = issueAssetData.takerAddress, mode = issueAssetData.mode))
          Ok(views.html.index(successes = Seq(constants.Response.ASSET_ISSUED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }
}
