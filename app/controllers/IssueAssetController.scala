package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}

import scala.concurrent.ExecutionContext
import scala.util.Random

class IssueAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, blockchainAclAccounts: blockchain.ACLAccounts, masterZones: master.Zones, masterAccounts: master.Accounts, withTraderLoginAction: WithTraderLoginAction, blockchainAccounts: blockchain.Accounts, withZoneLoginAction: WithZoneLoginAction, blockchainAssets: blockchain.Assets, transactionsIssueAsset: transactions.IssueAsset, blockchainTransactionIssueAssets: blockchainTransaction.IssueAssets, masterTransactionIssueAssetRequests: masterTransaction.IssueAssetRequests)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def issueAssetRequestForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.issueAssetRequest(views.companion.master.IssueAssetRequest.form))
  }

  def issueAssetRequest: Action[AnyContent] = withTraderLoginAction { implicit request =>
    views.companion.master.IssueAssetRequest.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.issueAssetRequest(formWithErrors))
      },
      issueAssetRequestData => {
        try {
          masterTransactionIssueAssetRequests.Service.addIssueAssetRequest(accountID = request.session.get(constants.Security.USERNAME).get, documentHash = issueAssetRequestData.documentHash, assetPrice = issueAssetRequestData.assetPrice, assetType = issueAssetRequestData.assetType, quantityUnit = issueAssetRequestData.quantityUnit, assetQuantity = issueAssetRequestData.assetQuantity)
          Ok(views.html.index(success = constants.Success.ISSUE_ASSET_REQUEST))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
        }
      }
    )
  }

  def viewPendingIssueAssetRequests: Action[AnyContent] = withZoneLoginAction { implicit request =>
    try {
      Ok(views.html.component.master.viewPendingIssueAssetRequests(masterTransactionIssueAssetRequests.Service.getPendingIssueAssetRequests(masterAccounts.Service.getIDsForAddresses(blockchainAclAccounts.Service.getAddressesUnderZone(masterZones.Service.getZoneId(request.session.get(constants.Security.USERNAME).get))))))
    }
    catch {
      case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
    }
  }

  def rejectIssueAssetRequestForm(requestID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.rejectIssueAssetRequest(views.companion.master.RejectIssueAssetRequest.form, requestID))
  }

  def rejectIssueAssetRequest: Action[AnyContent] = withZoneLoginAction { implicit request =>
    views.companion.master.RejectIssueAssetRequest.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.rejectIssueAssetRequest(formWithErrors, formWithErrors.data(constants.Forms.REQUEST_ID)))
      },
      rejectIssueAssetRequestData => {
        try {
          masterTransactionIssueAssetRequests.Service.updateStatus(rejectIssueAssetRequestData.requestID, false)
          Ok(views.html.index(success = Messages(constants.Success.ISSUE_FIAT_REQUEST_REJECTED)))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
        }
      }
    )
  }

  def issueAssetForm(requestID: String, accountID: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int): Action[AnyContent] = Action {
    implicit request =>
      Ok(views.html.component.master.issueAsset(views.companion.master.IssueAsset.form, requestID, accountID, documentHash, assetType, assetPrice, quantityUnit, assetQuantity))
  }

  def issueAsset: Action[AnyContent] = withZoneLoginAction {
    implicit request =>
      views.companion.master.IssueAsset.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.issueAsset(formWithErrors, formWithErrors.data(constants.Forms.REQUEST_ID), formWithErrors.data(constants.Forms.ACCOUNT_ID), formWithErrors.data(constants.Forms.DOCUMENT_HASH), formWithErrors.data(constants.Forms.ASSET_TYPE), formWithErrors.data(constants.Forms.ASSET_PRICE).toInt, formWithErrors.data(constants.Forms.QUANTITY_UNIT), formWithErrors.data(constants.Forms.ASSET_QUANTITY).toInt))
        },
        issueAssetData => {
          try {
            if (masterTransactionIssueAssetRequests.Service.getStatus(issueAssetData.requestID).isEmpty) {
              if (kafkaEnabled) {
                val toAddress = masterAccounts.Service.getAddress(request.session.get(constants.Security.USERNAME).get)
                val response = transactionsIssueAsset.Service.kafkaPost(transactionsIssueAsset.Request(from = request.session.get(constants.Security.USERNAME).get, to = toAddress, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas))
                blockchainTransactionIssueAssets.Service.addIssueAssetKafka(from = request.session.get(constants.Security.USERNAME).get, to = toAddress, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas, null, null, ticketID = response.ticketID, null)
                Ok(views.html.index(success = response.ticketID))
              } else {
                val toAddress = masterAccounts.Service.getAddress(issueAssetData.accountID)
                val zoneAddress = masterAccounts.Service.getAddress(request.session.get(constants.Security.USERNAME).get)
                val response = transactionsIssueAsset.Service.post(transactionsIssueAsset.Request(from = request.session.get(constants.Security.USERNAME).get, to = toAddress, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas))
                blockchainTransactionIssueAssets.Service.addIssueAsset(from = request.session.get(constants.Security.USERNAME).get, to = toAddress, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
                masterTransactionIssueAssetRequests.Service.updateStatusAndGas(issueAssetData.requestID, true, issueAssetData.gas)
                blockchainAccounts.Service.updateSequence(toAddress, blockchainAccounts.Service.getSequence(toAddress) + 1)
                blockchainAccounts.Service.updateSequence(zoneAddress, blockchainAccounts.Service.getSequence(zoneAddress) + 1)
                for (tag <- response.Tags) {
                  if (tag.Key == constants.Response.KEY_ASSET) {
                    blockchainAssets.Service.addAsset(pegHash = tag.Value, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, ownerAddress = toAddress)
                  }
                }
                Ok(views.html.index(success = response.TxHash))
              }
            } else {
              Ok(views.html.index(failure = Messages(constants.Error.REQUEST_ALREADY_APPROVED_OR_REJECTED)))
            }
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
            case blockChainException: BlockChainException =>
              masterTransactionIssueAssetRequests.Service.updateStatusAndComment(issueAssetData.requestID, false, blockChainException.message)
              Ok(views.html.index(failure = blockChainException.message))
          }
        }
      )
  }

  def blockchainIssueAssetForm: Action[AnyContent] = Action {
    implicit request =>
      Ok(views.html.component.blockchain.issueAsset(views.companion.blockchain.IssueAsset.form))
  }

  def blockchainIssueAsset: Action[AnyContent] = Action {
    implicit request =>
      views.companion.blockchain.IssueAsset.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.blockchain.issueAsset(formWithErrors))
        },
        issueAssetData => {
          try {
            if (kafkaEnabled) {
              val response = transactionsIssueAsset.Service.kafkaPost(transactionsIssueAsset.Request(from = issueAssetData.from, to = issueAssetData.to, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas))
              blockchainTransactionIssueAssets.Service.addIssueAssetKafka(from = issueAssetData.from, to = issueAssetData.to, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas, null, null, ticketID = response.ticketID, null)
              Ok(views.html.index(success = response.ticketID))
            } else {
              val response = transactionsIssueAsset.Service.post(transactionsIssueAsset.Request(from = issueAssetData.from, to = issueAssetData.to, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas))
              blockchainTransactionIssueAssets.Service.addIssueAsset(from = issueAssetData.from, to = issueAssetData.to, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
              Ok(views.html.index(success = response.TxHash))
            }
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
            case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
          }
        }
      )
  }
}
