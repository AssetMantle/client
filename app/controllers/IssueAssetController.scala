package controllers

import java.util.Date

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
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
    Ok(views.html.component.master.issueAssetRequest(views.companion.master.ConfirmTransaction.form.fill(views.companion.master.ConfirmTransaction.Data(id, 0, "")), masterTransactionIssueAssetRequests.Service.getIssueAssetByID(id)))
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
                entity = blockchainTransaction.IssueAsset(from = loginState.address, to = loginState.address, documentHash = asset.documentHash, assetType = asset.assetType, assetPrice = asset.assetPrice, quantityUnit = asset.quantityUnit, assetQuantity = asset.assetQuantity, moderated = false, gas = confirmTransactionData.gas, takerAddress = if (asset.takerAddress == Option("")) null else asset.takerAddress, status = null, txHash = null, ticketID = "", mode = transactionMode, code = null),
                blockchainTransactionCreate = blockchainTransactionIssueAssets.Service.create,
                request = transactionsIssueAsset.Request(transactionsIssueAsset.BaseRequest(from = loginState.address), to = loginState.address, password = confirmTransactionData.password, documentHash = asset.documentHash, assetType = asset.assetType, assetPrice = asset.assetPrice.toString, quantityUnit = asset.quantityUnit, assetQuantity = asset.assetQuantity.toString, moderated = false, gas = confirmTransactionData.gas.toString, takerAddress = asset.takerAddress.getOrElse(""), mode = transactionMode),
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
        val shipmentDetails = Json.parse(assetRequest.shipmentDetails).as[masterTransaction.IssueAssetRequestDetails.ShipmentDetails]
        Ok(views.html.component.master.issueAssetDetail(views.companion.master.IssueAssetDetail.form.fill(views.companion.master.IssueAssetDetail.Data(Option(assetRequest.id), assetRequest.documentHash, assetRequest.assetType, assetRequest.quantityUnit, assetRequest.assetQuantity, assetRequest.assetPrice, assetRequest.takerAddress.getOrElse(""), shipmentDetails.commodityName, shipmentDetails.quality, shipmentDetails.deliveryTerm, shipmentDetails.tradeType, shipmentDetails.portOfLoading, shipmentDetails.portOfDischarge, shipmentDetails.shipmentDate, assetRequest.physicalDocumentsHandledVia, assetRequest.paymentTerms))))
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
            masterTransactionIssueAssetRequests.Service.insertOrUpdate(id = id, ticketID = None, pegHash = None, accountID = loginState.username, documentHash = issueAssetDetailData.documentHash, assetType = issueAssetDetailData.assetType, quantityUnit = issueAssetDetailData.quantityUnit, assetQuantity = issueAssetDetailData.assetQuantity, assetPrice = issueAssetDetailData.assetPrice, takerAddress = if (issueAssetDetailData.takerAddress == "") None else Option(issueAssetDetailData.takerAddress), shipmentDetails = masterTransaction.IssueAssetRequestDetails.ShipmentDetails(issueAssetDetailData.commodityName, issueAssetDetailData.quality, issueAssetDetailData.deliveryTerm, issueAssetDetailData.tradeType, issueAssetDetailData.portOfLoading, issueAssetDetailData.portOfDischarge, issueAssetDetailData.shipmentDate), physicalDocumentsHandledVia = issueAssetDetailData.physicalDocumentsHandledVia, paymentTerms = issueAssetDetailData.comdexPaymentTerms, status = constants.Status.Asset.INCOMPLETE_DETAILS)
            val obl = Json.parse(masterTransactionAssetFiles.Service.getOrEmpty(id, constants.File.OBL).context.getOrElse(Json.toJson(masterTransaction.AssetFileTypeContext.OBL("", "", "", "", "", "", new Date(0), "", 0, 0)).toString)).as[masterTransaction.AssetFileTypeContext.OBL]
            PartialContent(views.html.component.master.issueAssetOBL(views.companion.master.IssueAssetOBL.form.fill(views.companion.master.IssueAssetOBL.Data(id, obl.billOfLadingId, obl.portOfLoading, obl.shipperName, obl.shipperAddress, obl.notifyPartyName, obl.notifyPartyAddress, obl.dateOfShipping, obl.deliveryTerm, obl.weightOfConsignment, obl.declaredAssetValue))))

            //              PartialContent(views.html.index(successes = Seq(constants.Response.ISSUE_ASSET_REQUEST_SENT)))
            //           }
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }

      )
  }

  def issueAssetOBLForm(id: String): Action[AnyContent] = Action {
    implicit request =>
      val obl = Json.parse(masterTransactionAssetFiles.Service.getOrEmpty(id, constants.File.OBL).context.getOrElse(Json.toJson(masterTransaction.AssetFileTypeContext.OBL("", "", "", "", "", "", new Date(0), "", 0, 0)).toString)).as[masterTransaction.AssetFileTypeContext.OBL]
      Ok(views.html.component.master.issueAssetOBL(views.companion.master.IssueAssetOBL.form.fill(views.companion.master.IssueAssetOBL.Data(id, obl.billOfLadingId, obl.portOfLoading, obl.shipperName, obl.shipperAddress, obl.notifyPartyName, obl.notifyPartyAddress, obl.dateOfShipping, obl.deliveryTerm, obl.weightOfConsignment, obl.declaredAssetValue))))
  }

  def issueAssetOBL: Action[AnyContent] = withTraderLoginAction.authenticated {
    implicit loginState =>
      implicit request =>
        views.companion.master.IssueAssetOBL.form.bindFromRequest().fold(
          formWithErrors => {
            BadRequest(views.html.component.master.issueAssetOBL(formWithErrors))
          },
          issueAssetOBLData => {
            try {
              //            PartialContent(views.html.component.master.issueAssetDetails())
              masterTransactionAssetFiles.Service.insertOrUpdateContext(masterTransaction.AssetFile(issueAssetOBLData.requestID, constants.File.OBL, masterTransactionAssetFiles.Service.getOrEmpty(issueAssetOBLData.requestID, constants.File.OBL).fileName, None, Option(Json.toJson(masterTransaction.AssetFileTypeContext.OBL(issueAssetOBLData.billOfLadingNumber, issueAssetOBLData.portOfLoading, issueAssetOBLData.shipperName, issueAssetOBLData.shipperAddress, issueAssetOBLData.notifyPartyName, issueAssetOBLData.notifyPartyAddress, issueAssetOBLData.shipmentDate, issueAssetOBLData.deliveryTerm, issueAssetOBLData.assetQuantity, issueAssetOBLData.assetPrice)).toString), None))
              val invoice = Json.parse(masterTransactionAssetFiles.Service.getOrEmpty(issueAssetOBLData.requestID, constants.File.INVOICE).context.getOrElse(Json.toJson(masterTransaction.AssetFileTypeContext.Invoice("", new Date(0))).toString)).as[masterTransaction.AssetFileTypeContext.Invoice]
              PartialContent(views.html.component.master.issueAssetInvoice(views.companion.master.IssueAssetInvoice.form.fill(views.companion.master.IssueAssetInvoice.Data(issueAssetOBLData.requestID, invoice.invoiceNumber, invoice.invoiceDate))))
            }
            catch {
              case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            }
          }
        )
  }

  def issueAssetInvoiceForm(id: String): Action[AnyContent] = Action {
    implicit request =>
      val invoice = Json.parse(masterTransactionAssetFiles.Service.getOrEmpty(id, constants.File.INVOICE).context.getOrElse(Json.toJson(masterTransaction.AssetFileTypeContext.Invoice("", new Date(0))).toString)).as[masterTransaction.AssetFileTypeContext.Invoice]
      PartialContent(views.html.component.master.issueAssetInvoice(views.companion.master.IssueAssetInvoice.form.fill(views.companion.master.IssueAssetInvoice.Data(id, invoice.invoiceNumber, invoice.invoiceDate))))
  }

  def issueAssetInvoice: Action[AnyContent] = withTraderLoginAction.authenticated {
    implicit loginState =>
      implicit request =>
        views.companion.master.IssueAssetInvoice.form.bindFromRequest().fold(
          formWithErrors => {
            BadRequest(views.html.component.master.issueAssetInvoice(formWithErrors))
          },
          issueAssetInvoiceData => {
            try {
              //            PartialContent(views.html.component.master.issueAssetDetails())
              masterTransactionAssetFiles.Service.insertOrUpdateContext(masterTransaction.AssetFile(issueAssetInvoiceData.requestID, constants.File.INVOICE, "", None, Option(Json.toJson(masterTransaction.AssetFileTypeContext.Invoice(issueAssetInvoiceData.invoiceNumber, issueAssetInvoiceData.invoiceDate)).toString()), None))
              PartialContent(views.html.component.master.issueAssetDocument(issueAssetInvoiceData.requestID))
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
        masterTransactionAssetFiles.Service.updateFileStatus(id = id, documentType = documentType, status = true)
        pushNotification.sendNotification(masterTransactionIssueAssetRequests.Service.getAccountID(id), constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
        withUsernameToken.Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def rejectAssetDocument(id: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        masterTransactionAssetFiles.Service.updateFileStatus(id = id, documentType = documentType, status = false)
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
              if (masterTransactionIssueAssetRequests.Service.getStatus(issueAssetData.requestID) == constants.Status.Asset.REQUESTED) {
                val ticketID = transaction.process[blockchainTransaction.IssueAsset, transactionsIssueAsset.Request](
                  entity = blockchainTransaction.IssueAsset(from = loginState.address, to = toAddress, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, moderated = true, gas = issueAssetData.gas, takerAddress = if (issueAssetData.takerAddress == "") null else Option(issueAssetData.takerAddress), status = null, txHash = null, ticketID = "", mode = transactionMode, code = null),
                  blockchainTransactionCreate = blockchainTransactionIssueAssets.Service.create,
                  request = transactionsIssueAsset.Request(transactionsIssueAsset.BaseRequest(from = loginState.address), to = toAddress, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice.toString, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity.toString, moderated = true, gas = issueAssetData.gas.toString, takerAddress = issueAssetData.takerAddress, mode = transactionMode),
                  action = transactionsIssueAsset.Service.post,
                  onSuccess = blockchainTransactionIssueAssets.Utility.onSuccess,
                  onFailure = blockchainTransactionIssueAssets.Utility.onFailure,
                  updateTransactionHash = blockchainTransactionIssueAssets.Service.updateTransactionHash
                )
                masterTransactionIssueAssetRequests.Service.updateTicketID(issueAssetData.requestID, ticketID)
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
            transactionsIssueAsset.Service.post(transactionsIssueAsset.Request(transactionsIssueAsset.BaseRequest(from = issueAssetData.from), to = issueAssetData.to, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice.toString, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity.toString, gas = issueAssetData.gas.toString, moderated = issueAssetData.moderated, takerAddress = issueAssetData.takerAddress, mode = issueAssetData.mode))
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
