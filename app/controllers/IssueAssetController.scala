package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext
import scala.util.Random

class IssueAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, blockchainAclAccounts: blockchain.ACLAccounts, masterZones: master.Zones, masterAccounts: master.Accounts, withTraderLoginAction: WithTraderLoginAction, blockchainAccounts: blockchain.Accounts, withZoneLoginAction: WithZoneLoginAction, blockchainAssets: blockchain.Assets, transactionsIssueAsset: transactions.IssueAsset, blockchainTransactionIssueAssets: blockchainTransaction.IssueAssets, masterTransactionIssueAssetRequests: masterTransaction.IssueAssetRequests)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private implicit val logger: Logger = Logger(this.getClass)

  def issueAssetRequestForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.issueAssetRequest(views.companion.master.IssueAssetRequest.form))
  }

  def issueAssetRequest: Action[AnyContent] = withTraderLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.IssueAssetRequest.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.issueAssetRequest(formWithErrors))
        },
        issueAssetRequestData => {
          try {
            masterTransactionIssueAssetRequests.Service.addIssueAssetRequest(accountID = username, documentHash = issueAssetRequestData.documentHash, assetPrice = issueAssetRequestData.assetPrice, assetType = issueAssetRequestData.assetType, quantityUnit = issueAssetRequestData.quantityUnit, assetQuantity = issueAssetRequestData.assetQuantity)
            Ok(views.html.index(success = constants.Success.ISSUE_ASSET_REQUEST))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          }
        }
      )
  }

  def viewPendingIssueAssetRequests: Action[AnyContent] = withZoneLoginAction.authenticated { username =>
    implicit request =>
      try {
        Ok(views.html.component.master.viewPendingIssueAssetRequests(masterTransactionIssueAssetRequests.Service.getPendingIssueAssetRequests(masterAccounts.Service.getIDsForAddresses(blockchainAclAccounts.Service.getAddressesUnderZone(masterZones.Service.getZoneId(username))))))
      }
      catch {
        case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
      }
  }

  def rejectIssueAssetRequestForm(requestID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.rejectIssueAssetRequest(views.companion.master.RejectIssueAssetRequest.form, requestID))
  }

  def rejectIssueAssetRequest: Action[AnyContent] = withZoneLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.RejectIssueAssetRequest.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.rejectIssueAssetRequest(formWithErrors, formWithErrors.data(constants.Form.REQUEST_ID)))
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

  def issueAssetForm(requestID: String, accountID: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.issueAsset(views.companion.master.IssueAsset.form, requestID, accountID, documentHash, assetType, assetPrice, quantityUnit, assetQuantity))
  }

  def issueAsset: Action[AnyContent] = withZoneLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.IssueAsset.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.issueAsset(formWithErrors, formWithErrors.data(constants.Form.REQUEST_ID), formWithErrors.data(constants.Form.ACCOUNT_ID), formWithErrors.data(constants.Form.DOCUMENT_HASH), formWithErrors.data(constants.Form.ASSET_TYPE), formWithErrors.data(constants.Form.ASSET_PRICE).toInt, formWithErrors.data(constants.Form.QUANTITY_UNIT), formWithErrors.data(constants.Form.ASSET_QUANTITY).toInt))
        },
        issueAssetData => {
          try {
            if (masterTransactionIssueAssetRequests.Service.getStatus(issueAssetData.requestID).isEmpty) {
              if (kafkaEnabled) {
                val toAddress = masterAccounts.Service.getAddress(username)
                val response = transactionsIssueAsset.Service.kafkaPost(transactionsIssueAsset.Request(from = username, to = toAddress, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas))
                blockchainTransactionIssueAssets.Service.addIssueAssetKafka(from = username, to = toAddress, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas, null, null, ticketID = response.ticketID, null)
                Ok(views.html.index(success = response.ticketID))
              } else {
                val toAddress = masterAccounts.Service.getAddress(issueAssetData.accountID)
                val response = transactionsIssueAsset.Service.post(transactionsIssueAsset.Request(from = username, to = toAddress, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas))
                val zoneAddress = masterAccounts.Service.getAddress(username)
                blockchainTransactionIssueAssets.Service.addIssueAsset(from = username, to = toAddress, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
                masterTransactionIssueAssetRequests.Service.updateStatusAndGas(issueAssetData.requestID, true, issueAssetData.gas)
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
              masterTransactionIssueAssetRequests.Service.updateComment(issueAssetData.requestID, blockChainException.message)
              Ok(views.html.index(failure = blockChainException.message))
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
