package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class IssueAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, blockchainAclAccounts: blockchain.ACLAccounts, masterZones: master.Zones, masterAccounts: master.Accounts, withTraderLoginAction: WithTraderLoginAction, withZoneLoginAction: WithZoneLoginAction, blockchainAssets: blockchain.Assets, transactionsIssueAsset: transactions.IssueAsset, blockchainTransactionIssueAssets: blockchainTransaction.IssueAssets, masterTransactionIssueAssetRequests: masterTransaction.IssueAssetRequests)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

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
            if (issueAssetRequestData.unmoderated) {
              val fromAddress = masterAccounts.Service.getAddress(username)
              val ticketID: String = if (kafkaEnabled) transactionsIssueAsset.Service.kafkaPost(transactionsIssueAsset.Request(from = username, to = fromAddress, password = issueAssetRequestData.password, documentHash = issueAssetRequestData.documentHash, assetType = issueAssetRequestData.assetType, assetPrice = issueAssetRequestData.assetPrice, quantityUnit = issueAssetRequestData.quantityUnit, assetQuantity = issueAssetRequestData.assetQuantity, gas = issueAssetRequestData.gas, unmoderated = issueAssetRequestData.unmoderated)).ticketID else Random.nextString(32)
              blockchainTransactionIssueAssets.Service.create(from = username, to = fromAddress, documentHash = issueAssetRequestData.documentHash, assetType = issueAssetRequestData.assetType, assetPrice = issueAssetRequestData.assetPrice, quantityUnit = issueAssetRequestData.quantityUnit, assetQuantity = issueAssetRequestData.assetQuantity, unmoderated = issueAssetRequestData.unmoderated, gas = issueAssetRequestData.gas, null, null, ticketID = ticketID, null)
              if (!kafkaEnabled) {
                Future {
                  try {
                    blockchainTransactionIssueAssets.Utility.onSuccess(ticketID, transactionsIssueAsset.Service.post(transactionsIssueAsset.Request(from = username, to = fromAddress, password = issueAssetRequestData.password, documentHash = issueAssetRequestData.documentHash, assetType = issueAssetRequestData.assetType, assetPrice = issueAssetRequestData.assetPrice, quantityUnit = issueAssetRequestData.quantityUnit, assetQuantity = issueAssetRequestData.assetQuantity, gas = issueAssetRequestData.gas, unmoderated = issueAssetRequestData.unmoderated)))
                  } catch {
                    case baseException: BaseException => logger.error(baseException.failure.message, baseException)
                    case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
                      blockchainTransactionIssueAssets.Utility.onFailure(ticketID, blockChainException.failure.message)
                  }
                }
              }
              Ok(views.html.index(successes = Seq(constants.Response.ASSET_ISSUED)))
            } else {
              masterTransactionIssueAssetRequests.Service.create(accountID = username, documentHash = issueAssetRequestData.documentHash, assetPrice = issueAssetRequestData.assetPrice, assetType = issueAssetRequestData.assetType, quantityUnit = issueAssetRequestData.quantityUnit, assetQuantity = issueAssetRequestData.assetQuantity)
              Ok(views.html.index(successes = Seq(constants.Response.ISSUE_ASSET_REQUEST_SENT)))
            }
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
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
        case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
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
            masterTransactionIssueAssetRequests.Service.reject(id = rejectIssueAssetRequestData.requestID, comment = rejectIssueAssetRequestData.comment)
            Ok(views.html.index(successes = Seq(constants.Response.ISSUE_ASSET_REQUEST_REJECTED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
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
            val toAddress = masterAccounts.Service.getAddress(issueAssetData.accountID)
            if (masterTransactionIssueAssetRequests.Service.getStatus(issueAssetData.requestID).isEmpty) {
              val ticketID: String = if (kafkaEnabled) transactionsIssueAsset.Service.kafkaPost(transactionsIssueAsset.Request(from = username, to = toAddress, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas, unmoderated = issueAssetData.unmoderated)).ticketID else Random.nextString(32)
              blockchainTransactionIssueAssets.Service.create(from = username, to = toAddress, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, unmoderated = issueAssetData.unmoderated, gas = issueAssetData.gas, null, null, ticketID = ticketID, null)
              masterTransactionIssueAssetRequests.Service.accept(issueAssetData.requestID, ticketID, issueAssetData.gas)
              if (!kafkaEnabled) {
                Future {
                  try {
                    blockchainTransactionIssueAssets.Utility.onSuccess(ticketID, transactionsIssueAsset.Service.post(transactionsIssueAsset.Request(from = username, to = toAddress, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas, unmoderated = issueAssetData.unmoderated)))
                  } catch {
                    case baseException: BaseException => logger.error(baseException.failure.message, baseException)
                    case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
                      blockchainTransactionIssueAssets.Utility.onFailure(ticketID, blockChainException.failure.message)
                  }
                }
              }
              Ok(views.html.index(successes = Seq(constants.Response.ASSET_ISSUED)))
            } else {
              Ok(views.html.index(failures = Seq(constants.Response.REQUEST_ALREADY_APPROVED_OR_REJECTED)))
            }
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => masterTransactionIssueAssetRequests.Service.updateComment(issueAssetData.requestID, blockChainException.message)
              Ok(views.html.index(failures = Seq(blockChainException.failure)))
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
            transactionsIssueAsset.Service.kafkaPost(transactionsIssueAsset.Request(from = issueAssetData.from, to = issueAssetData.to, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas, unmoderated = issueAssetData.unmoderated))
          } else {
            transactionsIssueAsset.Service.post(transactionsIssueAsset.Request(from = issueAssetData.from, to = issueAssetData.to, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas, unmoderated = issueAssetData.unmoderated))
          }
          Ok(views.html.index(successes = Seq(constants.Response.ASSET_ISSUED)))

        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }
}
