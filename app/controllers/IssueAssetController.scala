package controllers

import java.util.Date

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Serializable
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import utilities.PushNotification

import scala.concurrent.ExecutionContext

@Singleton
class IssueAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, pushNotification: PushNotification, transaction: utilities.Transaction, blockchainAclAccounts: blockchain.ACLAccounts, masterZones: master.Zones, masterAccounts: master.Accounts, masterTransactionAssetFiles: masterTransaction.AssetFiles, withTraderLoginAction: WithTraderLoginAction, withZoneLoginAction: WithZoneLoginAction, blockchainAssets: blockchain.Assets, transactionsIssueAsset: transactions.IssueAsset, blockchainTransactionIssueAssets: blockchainTransaction.IssueAssets, masterTransactionIssueAssetRequests: masterTransaction.IssueAssetRequests, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ISSUE_ASSET

  def issueAssetRequestForm(id: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.issueAssetRequest(views.companion.master.ConfirmTransaction.form.fill(views.companion.master.ConfirmTransaction.Data(id, Option(0), Option(""))), masterTransactionIssueAssetRequests.Service.getIssueAssetByID(id)))
  }

  def assetDetail(id: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.assetDetail(masterTransactionIssueAssetRequests.Service.getIssueAssetByID(id), masterTransactionAssetFiles.Service.getAllDocuments(id)))
  }

  def issueAssetRequest: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmTransaction.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.issueAssetRequest(formWithErrors, masterTransactionIssueAssetRequests.Service.getIssueAssetByID(formWithErrors.data(constants.FormField.REQUEST_ID.name))))
        },
        confirmTransactionData => {
          try {
            val asset = masterTransactionIssueAssetRequests.Service.getIssueAssetByID(confirmTransactionData.requestID)
            if (asset.physicalDocumentsHandledVia == constants.Option.COMDEX) {
              masterTransactionIssueAssetRequests.Service.updateStatusAndComment(confirmTransactionData.requestID, constants.Status.Asset.REQUESTED)
              Ok(views.html.index(successes = Seq(constants.Response.ISSUE_ASSET_REQUEST_SENT)))
            } else {
              val ticketID = transaction.process[blockchainTransaction.IssueAsset, transactionsIssueAsset.Request](
                entity = blockchainTransaction.IssueAsset(from = loginState.address, to = loginState.address, documentHash = asset.documentHash, assetType = asset.assetType, assetPrice = asset.assetPrice, quantityUnit = asset.quantityUnit, assetQuantity = asset.assetQuantity, moderated = false, takerAddress = asset.takerAddress, gas = confirmTransactionData.gas.getOrElse(throw new BaseException(constants.Response.GAS_NOT_GIVEN)), ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionIssueAssets.Service.create,
                request = transactionsIssueAsset.Request(transactionsIssueAsset.BaseRequest(from = loginState.address, gas = confirmTransactionData.gas.getOrElse(throw new BaseException(constants.Response.GAS_NOT_GIVEN)).toString), to = loginState.address, password = confirmTransactionData.password.getOrElse(throw new BaseException(constants.Response.PASSWORD_NOT_GIVEN)), documentHash = asset.documentHash, assetType = asset.assetType, assetPrice = asset.assetPrice.toString, quantityUnit = asset.quantityUnit, assetQuantity = asset.assetQuantity.toString, moderated = false, takerAddress = asset.takerAddress.getOrElse(""), mode = transactionMode),
                action = transactionsIssueAsset.Service.post,
                onSuccess = blockchainTransactionIssueAssets.Utility.onSuccess,
                onFailure = blockchainTransactionIssueAssets.Utility.onFailure,
                updateTransactionHash = blockchainTransactionIssueAssets.Service.updateTransactionHash
              )
              masterTransactionIssueAssetRequests.Service.updateTicketID(confirmTransactionData.requestID, ticketID)
              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ASSET_ISSUED)))

            }
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }

      )
  }

  def issueAssetDetailForm(id: Option[String]): Action[AnyContent] = Action { implicit request =>
    try {
      if (id.isDefined) {
        val assetRequest = masterTransactionIssueAssetRequests.Service.getIssueAssetByID(id.get)
        Ok(views.html.component.master.issueAssetDetail(views.companion.master.IssueAssetDetail.form.fill(views.companion.master.IssueAssetDetail.Data(Option(assetRequest.id), assetRequest.documentHash, assetRequest.assetType, assetRequest.quantityUnit, assetRequest.assetQuantity, assetRequest.assetPrice, assetRequest.takerAddress, assetRequest.shipmentDetails.commodityName, assetRequest.shipmentDetails.quality, assetRequest.shipmentDetails.deliveryTerm, assetRequest.shipmentDetails.tradeType, assetRequest.shipmentDetails.portOfLoading, assetRequest.shipmentDetails.portOfDischarge, assetRequest.shipmentDetails.shipmentDate, assetRequest.physicalDocumentsHandledVia, assetRequest.paymentTerms))))
      } else {
        Ok(views.html.component.master.issueAssetDetail(views.companion.master.IssueAssetDetail.form))
      }
    } catch {
      case _: BaseException => InternalServerError(views.html.component.master.issueAssetDetail(views.companion.master.IssueAssetDetail.form))
    }
  }

  def issueAssetDetail: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.IssueAssetDetail.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.issueAssetDetail(formWithErrors))
        },
        issueAssetDetailData => {
          try {
            val id = if (issueAssetDetailData.requestID.isEmpty) utilities.IDGenerator.requestID() else issueAssetDetailData.requestID.get
            masterTransactionIssueAssetRequests.Service.insertOrUpdate(id = id, ticketID = None, pegHash = None, accountID = loginState.username, documentHash = issueAssetDetailData.documentHash, assetType = issueAssetDetailData.assetType, quantityUnit = issueAssetDetailData.quantityUnit, assetQuantity = issueAssetDetailData.assetQuantity, assetPrice = issueAssetDetailData.assetPrice, takerAddress = issueAssetDetailData.takerAddress, shipmentDetails = Serializable.ShipmentDetails(issueAssetDetailData.commodityName, issueAssetDetailData.quality, issueAssetDetailData.deliveryTerm, issueAssetDetailData.tradeType, issueAssetDetailData.portOfLoading, issueAssetDetailData.portOfDischarge, issueAssetDetailData.shipmentDate), physicalDocumentsHandledVia = issueAssetDetailData.physicalDocumentsHandledVia, paymentTerms = issueAssetDetailData.comdexPaymentTerms, status = constants.Status.Asset.INCOMPLETE_DETAILS)
            val obl = utilities.JSON.getInstance[Serializable.OBL](masterTransactionAssetFiles.Service.getOrEmpty(id, constants.File.OBL).context.getOrElse(Json.toJson(Serializable.OBL("", "", "", "", "", "", new Date, "", 0, 0)).toString))
            PartialContent(views.html.component.master.issueAssetOBL(views.companion.master.IssueAssetOBL.form.fill(views.companion.master.IssueAssetOBL.Data(id, obl.billOfLadingID, obl.portOfLoading, obl.shipperName, obl.shipperAddress, obl.notifyPartyName, obl.notifyPartyAddress, obl.dateOfShipping, obl.deliveryTerm, obl.weightOfConsignment, obl.declaredAssetValue)), masterTransactionAssetFiles.Service.getOrNone(id, constants.File.OBL)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }

      )
  }

  def issueAssetOBLForm(id: String): Action[AnyContent] = Action {
    implicit request =>
      val obl = Json.parse(masterTransactionAssetFiles.Service.getOrEmpty(id, constants.File.OBL).context.getOrElse(Json.toJson(Serializable.OBL("", "", "", "", "", "", new Date, "", 0, 0)).toString)).as[Serializable.OBL]
      Ok(views.html.component.master.issueAssetOBL(views.companion.master.IssueAssetOBL.form.fill(views.companion.master.IssueAssetOBL.Data(id, obl.billOfLadingID, obl.portOfLoading, obl.shipperName, obl.shipperAddress, obl.notifyPartyName, obl.notifyPartyAddress, obl.dateOfShipping, obl.deliveryTerm, obl.weightOfConsignment, obl.declaredAssetValue)), masterTransactionAssetFiles.Service.getOrNone(id, constants.File.OBL)))
  }

  def issueAssetOBL: Action[AnyContent] = withTraderLoginAction.authenticated {
    implicit loginState =>
      implicit request =>
        views.companion.master.IssueAssetOBL.form.bindFromRequest().fold(
          formWithErrors => {
            BadRequest(views.html.component.master.issueAssetOBL(formWithErrors, masterTransactionAssetFiles.Service.getOrNone(formWithErrors.data(constants.FormField.REQUEST_ID.name), constants.File.OBL)))
          },
          issueAssetOBLData => {
            try {
              if (masterTransactionAssetFiles.Service.checkFileExists(issueAssetOBLData.requestID, constants.File.OBL)) {
                masterTransactionAssetFiles.Service.insertOrUpdateContext(masterTransaction.AssetFile(issueAssetOBLData.requestID, constants.File.OBL, "", None, Option(Json.toJson(Serializable.OBL(issueAssetOBLData.billOfLadingNumber, issueAssetOBLData.portOfLoading, issueAssetOBLData.shipperName, issueAssetOBLData.shipperAddress, issueAssetOBLData.notifyPartyName, issueAssetOBLData.notifyPartyAddress, issueAssetOBLData.shipmentDate, issueAssetOBLData.deliveryTerm, issueAssetOBLData.assetQuantity, issueAssetOBLData.assetPrice)).toString), None))
                val invoice = Json.parse(masterTransactionAssetFiles.Service.getOrEmpty(issueAssetOBLData.requestID, constants.File.INVOICE).context.getOrElse(Json.toJson(Serializable.Invoice("", new Date)).toString)).as[Serializable.Invoice]
                PartialContent(views.html.component.master.issueAssetInvoice(views.companion.master.IssueAssetInvoice.form.fill(views.companion.master.IssueAssetInvoice.Data(issueAssetOBLData.requestID, invoice.invoiceNumber, invoice.invoiceDate)), masterTransactionAssetFiles.Service.getOrNone(issueAssetOBLData.requestID, constants.File.INVOICE)))
              } else {
                InternalServerError(views.html.index(failures = Seq(constants.Response.DOCUMENT_NOT_FOUND)))
              }
            }
            catch {
              case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            }
          }
        )
  }

  def issueAssetInvoiceForm(id: String): Action[AnyContent] = Action {
    implicit request =>
      val invoice = Json.parse(masterTransactionAssetFiles.Service.getOrEmpty(id, constants.File.INVOICE).context.getOrElse(Json.toJson(Serializable.Invoice("", new Date)).toString)).as[Serializable.Invoice]
      PartialContent(views.html.component.master.issueAssetInvoice(views.companion.master.IssueAssetInvoice.form.fill(views.companion.master.IssueAssetInvoice.Data(id, invoice.invoiceNumber, invoice.invoiceDate)), masterTransactionAssetFiles.Service.getOrNone(id, constants.File.INVOICE)))
  }

  def issueAssetInvoice: Action[AnyContent] = withTraderLoginAction.authenticated {
    implicit loginState =>
      implicit request =>
        views.companion.master.IssueAssetInvoice.form.bindFromRequest().fold(
          formWithErrors => {
            BadRequest(views.html.component.master.issueAssetInvoice(formWithErrors, masterTransactionAssetFiles.Service.getOrNone(formWithErrors.data(constants.FormField.REQUEST_ID.name), constants.File.INVOICE)))
          },
          issueAssetInvoiceData => {
            try {
              if (masterTransactionAssetFiles.Service.checkFileExists(issueAssetInvoiceData.requestID, constants.File.INVOICE)) {
                masterTransactionAssetFiles.Service.insertOrUpdateContext(masterTransaction.AssetFile(issueAssetInvoiceData.requestID, constants.File.INVOICE, "", None, Option(Json.toJson(Serializable.Invoice(issueAssetInvoiceData.invoiceNumber, issueAssetInvoiceData.invoiceDate)).toString()), None))
                PartialContent(views.html.component.master.issueAssetDocument(issueAssetInvoiceData.requestID, masterTransactionAssetFiles.Service.getDocuments(issueAssetInvoiceData.requestID, constants.File.TRADER_ASSET_DOCUMENT_TYPES_UPLOAD_PAGE)))
              } else {
                InternalServerError(views.html.index(failures = Seq(constants.Response.DOCUMENT_NOT_FOUND)))
              }
            }
            catch {
              case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            }
          }
        )
  }

  def viewPendingIssueAssetRequests: Action[AnyContent] = withZoneLoginAction.authenticated {
    implicit loginState =>
      implicit request =>
        try {
          withUsernameToken.Ok(views.html.component.master.viewPendingIssueAssetRequests(masterTransactionIssueAssetRequests.Service.getPendingIssueAssetRequests(masterAccounts.Service.getIDsForAddresses(blockchainAclAccounts.Service.getAddressesUnderZone(masterZones.Service.getZoneId(loginState.username))), constants.Status.Asset.REQUESTED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
  }

  def viewAssetDocuments(id: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.viewVerificationTraderAssetDouments(masterTransactionAssetFiles.Service.getAllDocuments(id)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def verifyAssetDocument(id: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        masterTransactionAssetFiles.Service.accept(id = id, documentType = documentType)
        pushNotification.sendNotification(masterTransactionIssueAssetRequests.Service.getAccountID(id), constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
        withUsernameToken.Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def rejectAssetDocument(id: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        masterTransactionAssetFiles.Service.reject(id = id, documentType = documentType)
        pushNotification.sendNotification(masterTransactionIssueAssetRequests.Service.getAccountID(id), constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
        withUsernameToken.Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def rejectIssueAssetRequestForm(requestID: String): Action[AnyContent] = Action {
    implicit request =>
      Ok(views.html.component.master.rejectIssueAssetRequest(views.companion.master.RejectIssueAssetRequest.form, requestID))
  }

  def rejectIssueAssetRequest: Action[AnyContent] = withZoneLoginAction.authenticated {
    implicit loginState =>
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

  def issueAssetForm(requestID: String, accountID: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, takerAddress: Option[String]): Action[AnyContent] = Action {
    implicit request =>
      Ok(views.html.component.master.issueAsset(views.companion.master.IssueAsset.form, requestID, accountID, documentHash, assetType, assetPrice, quantityUnit, assetQuantity, takerAddress))
  }

  def issueAsset: Action[AnyContent] = withZoneLoginAction.authenticated {
    implicit loginState =>
      implicit request =>
        views.companion.master.IssueAsset.form.bindFromRequest().fold(
          formWithErrors => {
            BadRequest(views.html.component.master.issueAsset(formWithErrors, formWithErrors.data(constants.Form.REQUEST_ID), formWithErrors.data(constants.Form.ACCOUNT_ID), formWithErrors.data(constants.Form.DOCUMENT_HASH), formWithErrors.data(constants.Form.ASSET_TYPE), formWithErrors.data(constants.Form.ASSET_PRICE).toInt, formWithErrors.data(constants.Form.QUANTITY_UNIT), formWithErrors.data(constants.Form.ASSET_QUANTITY).toInt, Option(formWithErrors.data(constants.Form.TAKER_ADDRESS))))
          },
          issueAssetData => {
            try {
              val toAddress = masterAccounts.Service.getAddress(issueAssetData.accountID)
              if (masterTransactionIssueAssetRequests.Service.verifyRequestedStatus(issueAssetData.requestID) && masterTransactionAssetFiles.Service.checkAllAssetFilesVerified(issueAssetData.requestID)) {
                val ticketID = transaction.process[blockchainTransaction.IssueAsset, transactionsIssueAsset.Request](
                  entity = blockchainTransaction.IssueAsset(from = loginState.address, to = toAddress, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, moderated = true, gas = issueAssetData.gas, takerAddress = issueAssetData.takerAddress, ticketID = "", mode = transactionMode),
                  blockchainTransactionCreate = blockchainTransactionIssueAssets.Service.create,
                  request = transactionsIssueAsset.Request(transactionsIssueAsset.BaseRequest(from = loginState.address, gas = issueAssetData.gas.toString), to = toAddress, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice.toString, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity.toString, moderated = true, takerAddress = issueAssetData.takerAddress.getOrElse(""), mode = transactionMode),
                  action = transactionsIssueAsset.Service.post,
                  onSuccess = blockchainTransactionIssueAssets.Utility.onSuccess,
                  onFailure = blockchainTransactionIssueAssets.Utility.onFailure,
                  updateTransactionHash = blockchainTransactionIssueAssets.Service.updateTransactionHash
                )
                masterTransactionIssueAssetRequests.Service.updateTicketID(issueAssetData.requestID, ticketID)
                withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ASSET_ISSUED)))
              } else {
                PreconditionFailed(views.html.index(failures = Seq(constants.Response.ALL_ASSET_FILES_NOT_VERIFIED, constants.Response.REQUEST_ALREADY_APPROVED_OR_REJECTED)))
              }
            }
            catch {
              case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
          transactionsIssueAsset.Service.post(transactionsIssueAsset.Request(transactionsIssueAsset.BaseRequest(from = issueAssetData.from, gas = issueAssetData.gas.toString), to = issueAssetData.to, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice.toString, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity.toString, moderated = issueAssetData.moderated, takerAddress = issueAssetData.takerAddress, mode = issueAssetData.mode))
          Ok(views.html.index(successes = Seq(constants.Response.ASSET_ISSUED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
