package controllers

import java.nio.file.Files
import java.util.Date

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject._
import models.common.Serializable
import models.{blockchain, master, masterTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import views.companion.master.FileUpload

import scala.concurrent.ExecutionContext

@Singleton
class FileController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, masterAccountFiles: master.AccountFiles, masterTransactionAssetFiles: masterTransaction.AssetFiles, masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles, masterTransactionIssueAssetRequests: masterTransaction.IssueAssetRequests, masterTransactionNegotiationRequests: masterTransaction.NegotiationRequests, blockchainACLs: blockchain.ACLAccounts, masterAccounts: master.Accounts, masterZones: master.Zones, masterOrganizations: master.Organizations, masterTraders: master.Traders, masterAccountKYCs: master.AccountKYCs, fileResourceManager: utilities.FileResourceManager, withGenesisLoginAction: WithGenesisLoginAction, withUserLoginAction: WithUserLoginAction, masterZoneKYCs: master.ZoneKYCs, withZoneLoginAction: WithZoneLoginAction, masterOrganizationKYCs: master.OrganizationKYCs, withOrganizationLoginAction: WithOrganizationLoginAction, masterTraderKYCs: master.TraderKYCs, withTraderLoginAction: WithTraderLoginAction, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration, wsClient: WSClient) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.FILE_CONTROLLER

  def uploadAccountKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadAccountKYC), utilities.String.getJsRouteFunction(routes.javascript.FileController.storeAccountKYC), documentType))
  }

  def updateAccountKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadAccountKYC), utilities.String.getJsRouteFunction(routes.javascript.FileController.updateAccountKYC), documentType))
  }

  def uploadAccountKYC(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.index(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getAccountKYCFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeAccountKYC(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.storeFile[master.AccountKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getAccountKYCFilePath(documentType),
          document = master.AccountKYC(id = loginState.username, documentType = documentType, status = None, fileName = name, file = None),
          masterCreate = masterAccountKYCs.Service.create
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateAccountKYC(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.updateFile[master.AccountKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getAccountKYCFilePath(documentType),
          oldDocumentFileName = masterAccountKYCs.Service.getFileName(id = loginState.username, documentType = documentType),
          document = master.AccountKYC(id = loginState.username, documentType = documentType, status = None, fileName = name, file = None),
          updateOldDocument = masterAccountKYCs.Service.updateOldDocument
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  //TODO Shall we verify for genesis
  def genesisAccessedFile(fileName: String, documentType: String): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getZoneKYCFilePath(documentType), fileName = fileName))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneAccessedOrganizationKYCFile(organizationID: String, fileName: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        if (masterOrganizations.Service.getZoneID(organizationID) == masterZones.Service.getID(loginState.username)) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getOrganizationKYCFilePath(documentType), fileName = fileName))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneAccessedTraderKYCFile(traderID: String, fileName: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        if (masterTraders.Service.getZoneID(traderID) == masterZones.Service.getID(loginState.username)) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getTraderKYCFilePath(documentType), fileName = fileName))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationAccessedTraderKYCFile(traderID: String, fileName: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        if (masterTraders.Service.getOrganizationID(traderID) == masterOrganizations.Service.getID(loginState.username)) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getTraderKYCFilePath(documentType), fileName = fileName))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def uploadTraderAssetForm(documentType: String, issueAssetRequestID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadTraderAsset), utilities.String.getJsRouteFunction(routes.javascript.FileController.storeTraderAsset), documentType, issueAssetRequestID))
  }

  def updateTraderAssetForm(documentType: String, issueAssetRequestID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadTraderAsset), utilities.String.getJsRouteFunction(routes.javascript.FileController.updateTraderAsset), documentType, issueAssetRequestID))
  }

  def uploadTraderAsset(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
          BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(Messages(constants.Response.NO_FILE.message))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getTraderAssetFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeTraderAsset(name: String, documentType: String, issueAssetRequestID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.storeFile[masterTransaction.AssetFile](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getTraderAssetFilePath(documentType),
          document = masterTransaction.AssetFile(id = issueAssetRequestID, documentType = documentType, fileName = name, file = None, documentContent = None, status = None),
          masterCreate = masterTransactionAssetFiles.Service.create
        )
        documentType match {
          case constants.File.OBL =>
            masterTransactionAssetFiles.Service.getOrEmpty(issueAssetRequestID, constants.File.OBL).documentContent match {
              case Some(oblContent: Serializable.OBL) => withUsernameToken.PartialContent(views.html.component.master.issueAssetOBL(views.companion.master.IssueAssetOBL.form.fill(views.companion.master.IssueAssetOBL.Data(issueAssetRequestID, oblContent.billOfLadingID, oblContent.portOfLoading, oblContent.shipperName, oblContent.shipperAddress, oblContent.notifyPartyName, oblContent.notifyPartyAddress, oblContent.dateOfShipping, oblContent.deliveryTerm, oblContent.weightOfConsignment, oblContent.declaredAssetValue)), masterTransactionAssetFiles.Service.getOrNone(issueAssetRequestID, constants.File.OBL)))
              case None => withUsernameToken.PartialContent(views.html.component.master.issueAssetOBL(views.companion.master.IssueAssetOBL.form.fill(views.companion.master.IssueAssetOBL.Data(issueAssetRequestID, "", "", "", "", "", "", new Date, "", 0, 0)), masterTransactionAssetFiles.Service.getOrNone(issueAssetRequestID, constants.File.OBL)))
            }
          case constants.File.INVOICE =>
            masterTransactionAssetFiles.Service.getOrEmpty(issueAssetRequestID, constants.File.INVOICE).documentContent match {
              case Some(invoiceContent: Serializable.Invoice) => withUsernameToken.PartialContent(views.html.component.master.issueAssetInvoice(views.companion.master.IssueAssetInvoice.form.fill(views.companion.master.IssueAssetInvoice.Data(issueAssetRequestID, invoiceContent.invoiceNumber, invoiceContent.invoiceDate)), masterTransactionAssetFiles.Service.getOrNone(issueAssetRequestID, constants.File.INVOICE)))
              case None => withUsernameToken.PartialContent(views.html.component.master.issueAssetInvoice(views.companion.master.IssueAssetInvoice.form.fill(views.companion.master.IssueAssetInvoice.Data(issueAssetRequestID, "", new Date)), masterTransactionAssetFiles.Service.getOrNone(issueAssetRequestID, constants.File.INVOICE)))
            }
          case constants.File.CONTRACT | constants.File.PACKING_LIST | constants.File.COO | constants.File.COA | constants.File.OTHER => withUsernameToken.PartialContent(views.html.component.master.issueAssetDocument(issueAssetRequestID, masterTransactionAssetFiles.Service.getDocuments(issueAssetRequestID, constants.File.TRADER_ASSET_DOCUMENT_TYPES_UPLOAD_PAGE)))
          case _ => withUsernameToken.Ok(views.html.index())
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateTraderAsset(name: String, documentType: String, issueAssetRequestID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.updateFile[masterTransaction.AssetFile](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getTraderAssetFilePath(documentType),
          oldDocumentFileName = masterTransactionAssetFiles.Service.getFileName(id = issueAssetRequestID, documentType = documentType),
          document = masterTransaction.AssetFile(id = issueAssetRequestID, documentType = documentType, fileName = name, file = None, documentContent = None, status = None),
          updateOldDocument = masterTransactionAssetFiles.Service.insertOrUpdateOldDocument
        )
        documentType match {
          case constants.File.OBL =>
            masterTransactionAssetFiles.Service.getOrEmpty(issueAssetRequestID, constants.File.OBL).documentContent match {
              case Some(oblContent: Serializable.OBL) => withUsernameToken.PartialContent(views.html.component.master.issueAssetOBL(views.companion.master.IssueAssetOBL.form.fill(views.companion.master.IssueAssetOBL.Data(issueAssetRequestID, oblContent.billOfLadingID, oblContent.portOfLoading, oblContent.shipperName, oblContent.shipperAddress, oblContent.notifyPartyName, oblContent.notifyPartyAddress, oblContent.dateOfShipping, oblContent.deliveryTerm, oblContent.weightOfConsignment, oblContent.declaredAssetValue)), masterTransactionAssetFiles.Service.getOrNone(issueAssetRequestID, constants.File.OBL)))
              case None => withUsernameToken.PartialContent(views.html.component.master.issueAssetOBL(views.companion.master.IssueAssetOBL.form.fill(views.companion.master.IssueAssetOBL.Data(issueAssetRequestID, "", "", "", "", "", "", new Date, "", 0, 0)), masterTransactionAssetFiles.Service.getOrNone(issueAssetRequestID, constants.File.OBL)))
            }
          case constants.File.INVOICE =>
            masterTransactionAssetFiles.Service.getOrEmpty(issueAssetRequestID, constants.File.INVOICE).documentContent match {
              case Some(invoiceContent: Serializable.Invoice) => withUsernameToken.PartialContent(views.html.component.master.issueAssetInvoice(views.companion.master.IssueAssetInvoice.form.fill(views.companion.master.IssueAssetInvoice.Data(issueAssetRequestID, invoiceContent.invoiceNumber, invoiceContent.invoiceDate)), masterTransactionAssetFiles.Service.getOrNone(issueAssetRequestID, constants.File.INVOICE)))
              case None => withUsernameToken.PartialContent(views.html.component.master.issueAssetInvoice(views.companion.master.IssueAssetInvoice.form.fill(views.companion.master.IssueAssetInvoice.Data(issueAssetRequestID, "", new Date)), masterTransactionAssetFiles.Service.getOrNone(issueAssetRequestID, constants.File.INVOICE)))
            }
          case constants.File.CONTRACT | constants.File.PACKING_LIST | constants.File.COO | constants.File.COA | constants.File.OTHER => withUsernameToken.PartialContent(views.html.component.master.issueAssetDocument(issueAssetRequestID, masterTransactionAssetFiles.Service.getDocuments(issueAssetRequestID, constants.File.TRADER_ASSET_DOCUMENT_TYPES_UPLOAD_PAGE)))
          case _ => withUsernameToken.Ok(views.html.index())
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def uploadTraderNegotiationForm(documentType: String, negotiationRequestID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadTraderNegotiation), utilities.String.getJsRouteFunction(routes.javascript.FileController.storeTraderNegotiation), documentType, negotiationRequestID))
  }

  def updateTraderNegotiationForm(documentType: String, negotiationRequestID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadTraderNegotiation), utilities.String.getJsRouteFunction(routes.javascript.FileController.updateTraderNegotiation), documentType, negotiationRequestID))
  }

  def uploadTraderNegotiation(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(Messages(constants.Response.NO_FILE.message))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getTraderNegotiationFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeTraderNegotiation(name: String, documentType: String, negotiationRequestID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.storeFile[masterTransaction.NegotiationFile](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getTraderNegotiationFilePath(documentType),
          document = masterTransaction.NegotiationFile(id = negotiationRequestID, documentType = documentType, fileName = name, file = None, documentContent = None, status = None),
          masterCreate = masterTransactionNegotiationFiles.Service.create
        )
        documentType match {
          case constants.File.BUYER_CONTRACT => withUsernameToken.PartialContent(views.html.component.master.confirmBuyerBidDocument(masterTransactionNegotiationFiles.Service.getOrNone(negotiationRequestID, constants.File.BUYER_CONTRACT), negotiationRequestID, constants.File.BUYER_CONTRACT))
          case constants.File.SELLER_CONTRACT => withUsernameToken.PartialContent(views.html.component.master.confirmSellerBidDocument(masterTransactionNegotiationFiles.Service.getOrNone(negotiationRequestID, constants.File.SELLER_CONTRACT), negotiationRequestID, constants.File.SELLER_CONTRACT))
          case constants.File.FIAT_PROOF => withUsernameToken.PartialContent(views.html.component.master.buyerExecuteOrderDocument(masterTransactionNegotiationFiles.Service.getOrNone(negotiationRequestID, constants.File.FIAT_PROOF), negotiationRequestID, constants.File.FIAT_PROOF))
          case constants.File.AWB_PROOF => withUsernameToken.PartialContent(views.html.component.master.sellerExecuteOrderDocument(masterTransactionNegotiationFiles.Service.getOrNone(negotiationRequestID, constants.File.AWB_PROOF), negotiationRequestID, constants.File.AWB_PROOF))
          case _ => withUsernameToken.Ok(views.html.index())
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateTraderNegotiation(name: String, documentType: String, negotiationRequestID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.updateFile[masterTransaction.NegotiationFile](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getTraderNegotiationFilePath(documentType),
          oldDocumentFileName = masterTransactionNegotiationFiles.Service.getFileName(id = negotiationRequestID, documentType = documentType),
          document = masterTransaction.NegotiationFile(id = negotiationRequestID, documentType = documentType, fileName = name, file = None, documentContent = None, status = None),
          updateOldDocument = masterTransactionNegotiationFiles.Service.insertOrUpdateOldDocument
        )
        documentType match {
          case constants.File.BUYER_CONTRACT => withUsernameToken.PartialContent(views.html.component.master.confirmBuyerBidDocument(masterTransactionNegotiationFiles.Service.getOrNone(negotiationRequestID, constants.File.BUYER_CONTRACT), negotiationRequestID, constants.File.BUYER_CONTRACT))
          case constants.File.SELLER_CONTRACT => withUsernameToken.PartialContent(views.html.component.master.confirmSellerBidDocument(masterTransactionNegotiationFiles.Service.getOrNone(negotiationRequestID, constants.File.SELLER_CONTRACT), negotiationRequestID, constants.File.SELLER_CONTRACT))
          case constants.File.FIAT_PROOF => withUsernameToken.PartialContent(views.html.component.master.buyerExecuteOrderDocument(masterTransactionNegotiationFiles.Service.getOrNone(negotiationRequestID, constants.File.FIAT_PROOF), negotiationRequestID, constants.File.FIAT_PROOF))
          case constants.File.AWB_PROOF => withUsernameToken.PartialContent(views.html.component.master.sellerExecuteOrderDocument(masterTransactionNegotiationFiles.Service.getOrNone(negotiationRequestID, constants.File.AWB_PROOF), negotiationRequestID, constants.File.AWB_PROOF))
          case _ => withUsernameToken.Ok(views.html.index())
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def uploadZoneNegotiationForm(documentType: String, negotiationRequestID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadZoneNegotiation), utilities.String.getJsRouteFunction(routes.javascript.FileController.storeZoneNegotiation), documentType, negotiationRequestID))
  }

  def updateZoneNegotiationForm(documentType: String, negotiationRequestID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadZoneNegotiation), utilities.String.getJsRouteFunction(routes.javascript.FileController.updateZoneNegotiation), documentType, negotiationRequestID))
  }

  def uploadZoneNegotiation(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(Messages(constants.Response.NO_FILE.message))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getZoneNegotiationFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeZoneNegotiation(name: String, documentType: String, negotiationRequestID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.storeFile[masterTransaction.NegotiationFile](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getZoneNegotiationFilePath(documentType),
          document = masterTransaction.NegotiationFile(id = negotiationRequestID, documentType = documentType, fileName = name, file = None, documentContent = None, status = None),
          masterCreate = masterTransactionNegotiationFiles.Service.create
        )
        documentType match {
          case constants.File.BUYER_CONTRACT => withUsernameToken.PartialContent(views.html.component.master.confirmBuyerBidDocument(masterTransactionNegotiationFiles.Service.getOrNone(negotiationRequestID, constants.File.BUYER_CONTRACT), negotiationRequestID, constants.File.BUYER_CONTRACT))
          case constants.File.SELLER_CONTRACT => withUsernameToken.PartialContent(views.html.component.master.confirmSellerBidDocument(masterTransactionNegotiationFiles.Service.getOrNone(negotiationRequestID, constants.File.SELLER_CONTRACT), negotiationRequestID, constants.File.SELLER_CONTRACT))
          case constants.File.FIAT_PROOF => withUsernameToken.PartialContent(views.html.component.master.moderatedBuyerExecuteOrderDocument(masterTransactionNegotiationFiles.Service.getOrNone(negotiationRequestID, constants.File.FIAT_PROOF), negotiationRequestID, constants.File.FIAT_PROOF))
          case constants.File.AWB_PROOF => withUsernameToken.PartialContent(views.html.component.master.moderatedSellerExecuteOrderDocument(masterTransactionNegotiationFiles.Service.getOrNone(negotiationRequestID, constants.File.AWB_PROOF), negotiationRequestID, constants.File.AWB_PROOF))
          case _ => withUsernameToken.Ok(views.html.index())
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateZoneNegotiation(name: String, documentType: String, negotiationRequestID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.updateFile[masterTransaction.NegotiationFile](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getZoneNegotiationFilePath(documentType),
          oldDocumentFileName = masterTransactionNegotiationFiles.Service.getFileName(id = negotiationRequestID, documentType = documentType),
          document = masterTransaction.NegotiationFile(id = negotiationRequestID, documentType = documentType, fileName = name, file = None, documentContent = None, status = None),
          updateOldDocument = masterTransactionNegotiationFiles.Service.insertOrUpdateOldDocument
        )
        documentType match {
          case constants.File.BUYER_CONTRACT => withUsernameToken.PartialContent(views.html.component.master.confirmBuyerBidDocument(masterTransactionNegotiationFiles.Service.getOrNone(negotiationRequestID, constants.File.BUYER_CONTRACT), negotiationRequestID, constants.File.BUYER_CONTRACT))
          case constants.File.SELLER_CONTRACT => withUsernameToken.PartialContent(views.html.component.master.confirmSellerBidDocument(masterTransactionNegotiationFiles.Service.getOrNone(negotiationRequestID, constants.File.SELLER_CONTRACT), negotiationRequestID, constants.File.SELLER_CONTRACT))
          case constants.File.FIAT_PROOF => withUsernameToken.PartialContent(views.html.component.master.moderatedBuyerExecuteOrderDocument(masterTransactionNegotiationFiles.Service.getOrNone(negotiationRequestID, constants.File.FIAT_PROOF), negotiationRequestID, constants.File.FIAT_PROOF))
          case constants.File.AWB_PROOF => withUsernameToken.PartialContent(views.html.component.master.moderatedSellerExecuteOrderDocument(masterTransactionNegotiationFiles.Service.getOrNone(negotiationRequestID, constants.File.AWB_PROOF), negotiationRequestID, constants.File.AWB_PROOF))
          case _ => withUsernameToken.Ok(views.html.index())
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }
  //TODO Shall we check if exists?
  def userAccessedZoneKYCFile(documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getZoneKYCFilePath(documentType), fileName = masterZoneKYCs.Service.getFileName(id = masterZones.Service.getID(loginState.username), documentType = documentType)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }
  //TODO Shall we check if exists?
  def userAccessedOrganizationKYCFile(documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getOrganizationKYCFilePath(documentType), fileName = masterOrganizationKYCs.Service.getFileName(id = masterOrganizations.Service.getID(loginState.username), documentType = documentType)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }
  //TODO Shall we check if exists?
  def userAccessedTraderKYCFile(documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getTraderKYCFilePath(documentType), fileName = masterTraderKYCs.Service.getFileName(id = masterTraders.Service.getID(loginState.username), documentType = documentType)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneAccessedAssetFile(id: String, fileName: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        if (masterTraders.Service.getZoneIDByAccountID(masterTransactionIssueAssetRequests.Service.getAccountID(id)) == masterZones.Service.getID(loginState.username)) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getTraderAssetFilePath(documentType), fileName = fileName))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneAccessedNegotiationFile(id: String, fileName: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        if (masterTraders.Service.getByAccountID(masterTransactionNegotiationRequests.Service.getSellerAccountID(id)).zoneID == masterZones.Service.getID(loginState.username) || masterTraders.Service.getByAccountID(masterTransactionNegotiationRequests.Service.getBuyerAccountID(id)).zoneID == masterZones.Service.getID(loginState.username)) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getZoneNegotiationFilePath(documentType), fileName = fileName))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationAccessedFile(accountID: String, fileName: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        if (masterTraders.Service.getOrganizationIDByAccountID(loginState.username) == masterOrganizations.Service.getID(loginState.username)) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getTraderKYCFilePath(documentType), fileName = fileName))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def uploadAccountFileForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadAccountFile), utilities.String.getJsRouteFunction(routes.javascript.FileController.storeAccountFile), documentType))
  }

  def updateAccountFileForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadAccountFile), utilities.String.getJsRouteFunction(routes.javascript.FileController.updateAccountFile), documentType))
  }

  def uploadAccountFile(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.index(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getAccountFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeAccountFile(name: String, documentType: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.storeFile[master.AccountFile](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getAccountFilePath(documentType),
          document = master.AccountFile(id = loginState.username, documentType = documentType, fileName = name, file = None),
          masterCreate = masterAccountFiles.Service.create
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateAccountFile(name: String, documentType: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.updateFile[master.AccountFile](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getAccountFilePath(documentType),
          oldDocumentFileName = masterAccountFiles.Service.getFileName(id = loginState.username, documentType = documentType),
          document = master.AccountFile(id = loginState.username, documentType = documentType, fileName = name, file = None),
          updateOldDocument = masterAccountFiles.Service.updateOldDocument
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def file(fileName: String, documentType: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val path: String = loginState.userType match {
          case constants.User.ZONE => if (masterZoneKYCs.Service.checkFileNameExists(id = loginState.username, fileName = fileName)) fileResourceManager.getZoneKYCFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
          case constants.User.ORGANIZATION => if (masterOrganizationKYCs.Service.checkFileNameExists(id = masterOrganizations.Service.getID(loginState.username), fileName = fileName)) fileResourceManager.getOrganizationKYCFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
          case constants.User.TRADER => if (masterTraderKYCs.Service.checkFileNameExists(id = masterTraders.Service.getID(loginState.username), fileName = fileName)) fileResourceManager.getTraderKYCFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
          case constants.User.USER => if (masterAccountKYCs.Service.checkFileNameExists(id = loginState.username, fileName = fileName)) fileResourceManager.getAccountKYCFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
          case _ => if (masterAccountFiles.Service.checkFileNameExists(id = loginState.username, fileName = fileName)) fileResourceManager.getAccountFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
        }
        Ok.sendFile(utilities.FileOperations.fetchFile(path = path, fileName = fileName))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def tradingFile(id: String, fileName: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val path: String = documentType match {
          case constants.File.CONTRACT | constants.File.OBL | constants.File.PACKING_LIST | constants.File.INVOICE | constants.File.COO | constants.File.COA | constants.File.OTHER => if (masterTransactionIssueAssetRequests.Service.getAccountID(id) == loginState.username) fileResourceManager.getTraderAssetFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
          case constants.File.BUYER_CONTRACT | constants.File.SELLER_CONTRACT | constants.File.FIAT_PROOF | constants.File.AWB_PROOF => if (masterTransactionNegotiationRequests.Service.checkNegotiationAndAccountIDExists(id, loginState.username)) fileResourceManager.getTraderNegotiationFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
        }
        Ok.sendFile(utilities.FileOperations.fetchFile(path = path, fileName = fileName))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

}
