package controllers

import java.util.Date

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Serializable
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class IssueAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, utilitiesNotification: utilities.Notification, masterTraders: master.Traders, transaction: utilities.Transaction, blockchainAclAccounts: blockchain.ACLAccounts, masterZones: master.Zones, masterAccounts: master.Accounts, masterTransactionAssetFiles: masterTransaction.AssetFiles, withTraderLoginAction: WithTraderLoginAction, withZoneLoginAction: WithZoneLoginAction, blockchainAssets: blockchain.Assets, transactionsIssueAsset: transactions.IssueAsset, blockchainTransactionIssueAssets: blockchainTransaction.IssueAssets, masterTransactionIssueAssetRequests: masterTransaction.IssueAssetRequests, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ISSUE_ASSET

  def issueAssetRequestForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val asset = masterTransactionIssueAssetRequests.Service.getIssueAssetByID(id)
        if (asset.accountID == loginState.username) {
          withUsernameToken.Ok(views.html.component.master.issueAssetRequest(views.companion.master.ConfirmIssueAssetTransaction.form.fill(views.companion.master.ConfirmIssueAssetTransaction.Data(id, Option(0), Option(""))), asset))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def assetDetail(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val asset = masterTransactionIssueAssetRequests.Service.getIssueAssetByID(id)
        if (asset.accountID == loginState.username) {
          Ok(views.html.component.master.assetDetail(asset, masterTransactionAssetFiles.Service.getAllDocuments(id)))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def issueAssetRequest: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmIssueAssetTransaction.form.bindFromRequest().fold(
        formWithErrors => {
          try {
            BadRequest(views.html.component.master.issueAssetRequest(formWithErrors, masterTransactionIssueAssetRequests.Service.getIssueAssetByID(formWithErrors.data(constants.FormField.REQUEST_ID.name))))
          } catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        confirmTransactionData => {
          try {
            masterTransactionIssueAssetRequests.Service.updateDocumentHash(confirmTransactionData.requestID, Option(utilities.FileOperations.combinedHash(masterTransactionAssetFiles.Service.getAllDocuments(confirmTransactionData.requestID))))
            val asset = masterTransactionIssueAssetRequests.Service.getIssueAssetByID(confirmTransactionData.requestID)
            if (asset.physicalDocumentsHandledVia == constants.Form.COMDEX) {
              masterTransactionIssueAssetRequests.Service.updateStatusAndComment(confirmTransactionData.requestID, constants.Status.Asset.REQUESTED)
              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ISSUE_ASSET_REQUEST_SENT)))
            } else {
              val ticketID = transaction.process[blockchainTransaction.IssueAsset, transactionsIssueAsset.Request](
                entity = blockchainTransaction.IssueAsset(from = loginState.address, to = loginState.address, documentHash = asset.documentHash.getOrElse(throw new BaseException(constants.Response.DOCUMENT_NOT_FOUND)), assetType = asset.assetType, assetPrice = asset.assetPrice, quantityUnit = asset.quantityUnit, assetQuantity = asset.assetQuantity, moderated = false, takerAddress = asset.takerAddress, gas = confirmTransactionData.gas.getOrElse(throw new BaseException(constants.Response.GAS_NOT_GIVEN)), ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionIssueAssets.Service.create,
                request = transactionsIssueAsset.Request(transactionsIssueAsset.BaseReq(from = loginState.address, gas = confirmTransactionData.gas.getOrElse(throw new BaseException(constants.Response.GAS_NOT_GIVEN)).toString), to = loginState.address, password = confirmTransactionData.password.getOrElse(throw new BaseException(constants.Response.PASSWORD_NOT_GIVEN)), documentHash = asset.documentHash.getOrElse(throw new BaseException(constants.Response.DOCUMENT_NOT_FOUND)), assetType = asset.assetType, assetPrice = asset.assetPrice.toString, quantityUnit = asset.quantityUnit, assetQuantity = asset.assetQuantity.toString, moderated = false, takerAddress = asset.takerAddress.getOrElse(""), mode = transactionMode),
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

  def issueAssetDetailForm(id: Option[String]): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        id match {
          case Some(requestID) => val assetRequest = masterTransactionIssueAssetRequests.Service.getIssueAssetByID(requestID)
            if (assetRequest.accountID == loginState.username) {
              withUsernameToken.Ok(views.html.component.master.issueAssetDetail(views.companion.master.IssueAssetDetail.form.fill(views.companion.master.IssueAssetDetail.Data(Option(assetRequest.id), assetRequest.assetType, assetRequest.quantityUnit, assetRequest.assetQuantity, assetRequest.assetPrice, assetRequest.takerAddress, assetRequest.shipmentDetails.commodityName, assetRequest.shipmentDetails.quality, assetRequest.shipmentDetails.deliveryTerm, assetRequest.shipmentDetails.tradeType, assetRequest.shipmentDetails.portOfLoading, assetRequest.shipmentDetails.portOfDischarge, assetRequest.shipmentDetails.shipmentDate, assetRequest.physicalDocumentsHandledVia, assetRequest.paymentTerms))))
            } else {
              Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
            }
          case None => withUsernameToken.Ok(views.html.component.master.issueAssetDetail(views.companion.master.IssueAssetDetail.form))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
            masterTransactionIssueAssetRequests.Service.insertOrUpdate(id = id, ticketID = None, pegHash = None, accountID = loginState.username, documentHash = None, assetType = issueAssetDetailData.assetType, quantityUnit = issueAssetDetailData.quantityUnit, assetQuantity = issueAssetDetailData.assetQuantity, assetPrice = issueAssetDetailData.assetPrice, takerAddress = issueAssetDetailData.takerAddress, shipmentDetails = Serializable.ShipmentDetails(issueAssetDetailData.commodityName, issueAssetDetailData.quality, issueAssetDetailData.deliveryTerm, issueAssetDetailData.tradeType, issueAssetDetailData.portOfLoading, issueAssetDetailData.portOfDischarge, issueAssetDetailData.shipmentDate), physicalDocumentsHandledVia = issueAssetDetailData.physicalDocumentsHandledVia, paymentTerms = issueAssetDetailData.comdexPaymentTerms, status = constants.Status.Asset.INCOMPLETE_DETAILS)
            val obl = masterTransactionAssetFiles.Service.getOrEmpty(id, constants.File.OBL).documentContent.getOrElse(Serializable.OBL("", "", "", "", "", "", new Date, "", 0, 0)).asInstanceOf[Serializable.OBL]
            withUsernameToken.PartialContent(views.html.component.master.issueAssetOBL(views.companion.master.IssueAssetOBL.form.fill(views.companion.master.IssueAssetOBL.Data(id, obl.billOfLadingID, obl.portOfLoading, obl.shipperName, obl.shipperAddress, obl.notifyPartyName, obl.notifyPartyAddress, obl.dateOfShipping, obl.deliveryTerm, obl.weightOfConsignment, obl.declaredAssetValue)), masterTransactionAssetFiles.Service.getOrNone(id, constants.File.OBL)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def issueAssetOBLForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        if (masterTransactionIssueAssetRequests.Service.getAccountID(id) == loginState.username) {
          val obl = masterTransactionAssetFiles.Service.getOrEmpty(id, constants.File.OBL).documentContent.getOrElse(Serializable.OBL("", "", "", "", "", "", new Date, "", 0, 0)).asInstanceOf[Serializable.OBL]
          withUsernameToken.PartialContent(views.html.component.master.issueAssetOBL(views.companion.master.IssueAssetOBL.form.fill(views.companion.master.IssueAssetOBL.Data(id, obl.billOfLadingID, obl.portOfLoading, obl.shipperName, obl.shipperAddress, obl.notifyPartyName, obl.notifyPartyAddress, obl.dateOfShipping, obl.deliveryTerm, obl.weightOfConsignment, obl.declaredAssetValue)), masterTransactionAssetFiles.Service.getOrNone(id, constants.File.OBL)))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
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
                masterTransactionAssetFiles.Service.insertOrUpdateContext(masterTransaction.AssetFile(issueAssetOBLData.requestID, constants.File.OBL, "", None, Option(Serializable.OBL(issueAssetOBLData.billOfLadingNumber, issueAssetOBLData.portOfLoading, issueAssetOBLData.shipperName, issueAssetOBLData.shipperAddress, issueAssetOBLData.notifyPartyName, issueAssetOBLData.notifyPartyAddress, issueAssetOBLData.shipmentDate, issueAssetOBLData.deliveryTerm, issueAssetOBLData.assetQuantity, issueAssetOBLData.assetPrice)), None))
                val invoice = masterTransactionAssetFiles.Service.getOrEmpty(issueAssetOBLData.requestID, constants.File.INVOICE).documentContent.getOrElse(Serializable.Invoice("", new Date)).asInstanceOf[Serializable.Invoice]
                withUsernameToken.PartialContent(views.html.component.master.issueAssetInvoice(views.companion.master.IssueAssetInvoice.form.fill(views.companion.master.IssueAssetInvoice.Data(issueAssetOBLData.requestID, invoice.invoiceNumber, invoice.invoiceDate)), masterTransactionAssetFiles.Service.getOrNone(issueAssetOBLData.requestID, constants.File.INVOICE)))
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

  def issueAssetInvoiceForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        if (masterTransactionIssueAssetRequests.Service.getAccountID(id) == loginState.username) {
          val invoice = masterTransactionAssetFiles.Service.getOrEmpty(id, constants.File.INVOICE).documentContent.getOrElse(Serializable.Invoice("", new Date)).asInstanceOf[Serializable.Invoice]
          withUsernameToken.PartialContent(views.html.component.master.issueAssetInvoice(views.companion.master.IssueAssetInvoice.form.fill(views.companion.master.IssueAssetInvoice.Data(id, invoice.invoiceNumber, invoice.invoiceDate)), masterTransactionAssetFiles.Service.getOrNone(id, constants.File.INVOICE)))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
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
                masterTransactionAssetFiles.Service.insertOrUpdateContext(masterTransaction.AssetFile(issueAssetInvoiceData.requestID, constants.File.INVOICE, "", None, Option(Serializable.Invoice(issueAssetInvoiceData.invoiceNumber, issueAssetInvoiceData.invoiceDate)), None))
                withUsernameToken.PartialContent(views.html.component.master.issueAssetDocument(issueAssetInvoiceData.requestID, masterTransactionAssetFiles.Service.getDocuments(issueAssetInvoiceData.requestID, constants.File.TRADER_ASSET_DOCUMENT_TYPES_UPLOAD_PAGE)))
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
          Ok(views.html.component.master.viewPendingIssueAssetRequests(masterTransactionIssueAssetRequests.Service.getPendingIssueAssetRequests(masterAccounts.Service.getIDsForAddresses(blockchainAclAccounts.Service.getAddressesUnderZone(masterZones.Service.getID(loginState.username))), constants.Status.Asset.REQUESTED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
  }

  def viewAssetDocuments(id: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.viewVerificationTraderAssetDouments(masterTransactionAssetFiles.Service.getAllDocuments(id)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateAssetDocumentStatusForm(fileID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        if (masterZones.Service.getID(loginState.username) == masterTraders.Service.getZoneIDByAccountID(masterTransactionIssueAssetRequests.Service.getAccountID(fileID))) {
          withUsernameToken.Ok(views.html.component.master.updateAssetDocumentStatus(file = masterTransactionAssetFiles.Service.get(id = fileID, documentType = documentType) ))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateAssetDocumentStatus(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.UpdateAssetDocumentStatus.form.bindFromRequest().fold(
        formWithErrors => {
          try {
            BadRequest(views.html.component.master.updateAssetDocumentStatus(formWithErrors, masterTransactionAssetFiles.Service.get(id = formWithErrors(constants.FormField.FILE_ID.name).value.get, documentType = formWithErrors(constants.FormField.DOCUMENT_TYPE.name).value.get)))
          } catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        updateAssetDocumentStatusData => {
          try {
            if (updateAssetDocumentStatusData.status) {
              masterTransactionAssetFiles.Service.accept(id = updateAssetDocumentStatusData.fileID, documentType = updateAssetDocumentStatusData.documentType)
              utilitiesNotification.send(masterTransactionIssueAssetRequests.Service.getAccountID(updateAssetDocumentStatusData.fileID), constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
            } else {
              masterTransactionAssetFiles.Service.reject(id = updateAssetDocumentStatusData.fileID, documentType = updateAssetDocumentStatusData.documentType)
              utilitiesNotification.send(masterTransactionIssueAssetRequests.Service.getAccountID(updateAssetDocumentStatusData.fileID), constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
            }
            withUsernameToken.PartialContent(views.html.component.master.updateAssetDocumentStatus(file = masterTransactionAssetFiles.Service.get(id = updateAssetDocumentStatusData.fileID, documentType = updateAssetDocumentStatusData.documentType) ))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
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
      Ok(views.html.component.master.issueAsset(views.companion.master.IssueAsset.form.fill(views.companion.master.IssueAsset.Data(requestID = requestID, accountID = accountID, documentHash = documentHash, assetType = assetType, assetPrice = assetPrice, quantityUnit = quantityUnit, assetQuantity = assetQuantity, takerAddress = takerAddress, gas = constants.FormField.GAS.minimumValue, password = ""))))
  }

  def issueAsset: Action[AnyContent] = withZoneLoginAction.authenticated {
    implicit loginState =>
      implicit request =>
        views.companion.master.IssueAsset.form.bindFromRequest().fold(
          formWithErrors => {
            BadRequest(views.html.component.master.issueAsset(formWithErrors))
          },
          issueAssetData => {
            try {
              val toAddress = masterAccounts.Service.getAddress(issueAssetData.accountID)
              if (masterTransactionIssueAssetRequests.Service.verifyRequestedStatus(issueAssetData.requestID) && masterTransactionAssetFiles.Service.checkAllAssetFilesVerified(issueAssetData.requestID)) {
                val ticketID = transaction.process[blockchainTransaction.IssueAsset, transactionsIssueAsset.Request](
                  entity = blockchainTransaction.IssueAsset(from = loginState.address, to = toAddress, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, moderated = true, gas = issueAssetData.gas, takerAddress = issueAssetData.takerAddress, ticketID = "", mode = transactionMode),
                  blockchainTransactionCreate = blockchainTransactionIssueAssets.Service.create,
                  request = transactionsIssueAsset.Request(transactionsIssueAsset.BaseReq(from = loginState.address, gas = issueAssetData.gas.toString), to = toAddress, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice.toString, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity.toString, moderated = true, takerAddress = issueAssetData.takerAddress.getOrElse(""), mode = transactionMode),
                  action = transactionsIssueAsset.Service.post,
                  onSuccess = blockchainTransactionIssueAssets.Utility.onSuccess,
                  onFailure = blockchainTransactionIssueAssets.Utility.onFailure,
                  updateTransactionHash = blockchainTransactionIssueAssets.Service.updateTransactionHash
                )
                masterTransactionIssueAssetRequests.Service.updateTicketID(issueAssetData.requestID, ticketID)
                withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ASSET_ISSUED)))
              } else {
                PreconditionFailed(views.html.index(failures = Seq(constants.Response.ALL_ASSET_FILES_NOT_VERIFIED)))
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
          transactionsIssueAsset.Service.post(transactionsIssueAsset.Request(transactionsIssueAsset.BaseReq(from = issueAssetData.from, gas = issueAssetData.gas.toString), to = issueAssetData.to, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice.toString, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity.toString, moderated = issueAssetData.moderated, takerAddress = issueAssetData.takerAddress, mode = issueAssetData.mode))
          Ok(views.html.index(successes = Seq(constants.Response.ASSET_ISSUED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
