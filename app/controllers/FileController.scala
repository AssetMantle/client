package controllers

import java.nio.file.{Files, NoSuchFileException}
import java.util.Date

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject._
import models.{blockchain, master, masterTransaction}
import models.common.Serializable
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import views.companion.master.FileUpload

import scala.concurrent.ExecutionContext

@Singleton
class FileController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, masterAccountFiles: master.AccountFiles, masterTransactionAssetFiles: masterTransaction.AssetFiles, masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles, masterTransactionIssueAssetRequests: masterTransaction.IssueAssetRequests, masterTransactionNegotiationRequests: masterTransaction.NegotiationRequests, blockchainACLs: blockchain.ACLAccounts, masterAccounts: master.Accounts, masterZones: master.Zones, masterOrganizations: master.Organizations, masterTraders: master.Traders, masterAccountKYCs: master.AccountKYCs, fileResourceManager: utilities.FileResourceManager, withGenesisLoginAction: WithGenesisLoginAction, withUserLoginAction: WithUserLoginAction, masterZoneKYCs: master.ZoneKYCs, withZoneLoginAction: WithZoneLoginAction, masterOrganizationKYCs: master.OrganizationKYCs, withOrganizationLoginAction: WithOrganizationLoginAction, masterTraderKYCs: master.TraderKYCs, withTraderLoginAction: WithTraderLoginAction, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration, wsClient: WSClient) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.FILE_CONTROLLER

  private val uploadZoneKycBankDetailsPath: String = configuration.get[String]("upload.zone.bankDetailsPath")

  private val uploadZoneKycIdentificationPath: String = configuration.get[String]("upload.zone.identificationPath")

  private val uploadTraderKycIdentificationPath: String = configuration.get[String]("upload.trader.identificationPath")

  private val uploadTraderAssetContractPath: String = configuration.get[String]("upload.asset.contract")

  private val uploadTraderAssetOBLPath: String = configuration.get[String]("upload.asset.obl")

  private val uploadTraderAssetInvoicePath: String = configuration.get[String]("upload.asset.invoice")

  private val uploadTraderAssetPackingListPath: String = configuration.get[String]("upload.asset.packingList")

  private val uploadTraderAssetCOOPath: String = configuration.get[String]("upload.asset.coo")

  private val uploadTraderAssetCOAPath: String = configuration.get[String]("upload.asset.coa")

  private val uploadTraderAssetOtherPath: String = configuration.get[String]("upload.asset.other")

  private val uploadTraderNegotiationBuyerContractPath: String = configuration.get[String]("upload.negotiation.buyerContract")

  private val uploadTraderNegotiationSellerContractOtherPath: String = configuration.get[String]("upload.negotiation.sellerContract")

  private val uploadTraderNegotiationAWBProofPath: String = configuration.get[String]("upload.negotiation.awbProof")

  private val uploadTraderNegotiationFiatProofPath: String = configuration.get[String]("upload.negotiation.fiatProof")

  def checkAccountKycFileExists(accountID: String, documentType: String): Action[AnyContent] = Action { implicit request =>
    if (masterAccountKYCs.Service.checkFileExists(id = accountID, documentType = documentType)) Ok else NoContent
  }

  def checkTraderAssetFileExists(id: String, documentType: String): Action[AnyContent] = Action { implicit request =>
    if (masterTransactionAssetFiles.Service.checkFileExists(id = id, documentType = documentType)) Ok else NoContent
  }

  def checkTraderNegotiationFileExists(id: String, documentType: String): Action[AnyContent] = Action { implicit request =>
    if (masterTransactionAssetFiles.Service.checkFileExists(id = id, documentType = documentType)) Ok else NoContent
  }

  def uploadUserKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadUserKyc), utilities.String.getJsRouteFunction(routes.javascript.FileController.storeUserKyc), documentType))
  }

  def updateUserKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadUserKyc), utilities.String.getJsRouteFunction(routes.javascript.FileController.updateUserKyc), documentType))
  }

  def uploadUserKyc(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.index(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getAccountKycFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeUserKyc(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.storeFile[master.AccountKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getAccountKycFilePath(documentType),
          document = master.AccountKYC(id = loginState.username, documentType = documentType, status = None, fileName = name, file = None),
          masterCreate = masterAccountKYCs.Service.create
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateUserKyc(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.updateFile[master.AccountKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getAccountKycFilePath(documentType),
          oldDocumentFileName = masterAccountKYCs.Service.getFileName(id = loginState.username, documentType = documentType),
          document = master.AccountKYC(id = loginState.username, documentType = documentType, status = None, fileName = name, file = None),
          updateOldDocument = masterAccountKYCs.Service.updateOldDocument
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }

  }

  def uploadUserZoneKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadUserZoneKyc), utilities.String.getJsRouteFunction(routes.javascript.FileController.storeUserZoneKyc), documentType))
  }

  def updateUserZoneKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadUserZoneKyc), utilities.String.getJsRouteFunction(routes.javascript.FileController.updateUserZoneKyc), documentType))
  }

  def uploadUserZoneKyc(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.index(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getZoneKycFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeUserZoneKyc(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.storeFile[master.ZoneKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getZoneKycFilePath(documentType),
          document = master.ZoneKYC(id = loginState.username, documentType = documentType, status = None, fileName = name, file = None),
          masterCreate = masterZoneKYCs.Service.create
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateUserZoneKyc(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.updateFile[master.ZoneKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getZoneKycFilePath(documentType),
          oldDocumentFileName = masterZoneKYCs.Service.getFileName(id = loginState.username, documentType = documentType),
          document = master.ZoneKYC(id = loginState.username, documentType = documentType, status = None, fileName = name, file = None),
          updateOldDocument = masterZoneKYCs.Service.updateOldDocument
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def uploadZoneKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadZoneKyc), utilities.String.getJsRouteFunction(routes.javascript.FileController.storeZoneKyc), documentType))
  }

  def updateZoneKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadZoneKyc), utilities.String.getJsRouteFunction(routes.javascript.FileController.updateZoneKyc), documentType))
  }

  def uploadZoneKyc(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.index(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getZoneKycFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeZoneKyc(name: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.storeFile[master.ZoneKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getZoneKycFilePath(documentType),
          document = master.ZoneKYC(id = loginState.username, documentType = documentType, fileName = name, file = None, status = None),
          masterCreate = masterZoneKYCs.Service.create
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateZoneKyc(name: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.updateFile[master.ZoneKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getZoneKycFilePath(documentType),
          oldDocumentFileName = masterZoneKYCs.Service.getFileName(id = loginState.username, documentType = documentType),
          document = master.ZoneKYC(id = loginState.username, documentType = documentType, status = None, fileName = name, file = None),
          updateOldDocument = masterZoneKYCs.Service.updateOldDocument
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
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
          document = masterTransaction.AssetFile(id = issueAssetRequestID, documentType = documentType, fileName = name, file = None, context = None, status = None),
          masterCreate = masterTransactionAssetFiles.Service.create
        )
        documentType match {
          case constants.File.OBL =>
            val obl = masterTransactionAssetFiles.Service.getOrEmpty(issueAssetRequestID, constants.File.OBL).context.getOrElse(Serializable.OBL("", "", "", "", "", "", new Date, "", 0, 0)).asInstanceOf[Serializable.OBL]
            PartialContent(views.html.component.master.issueAssetOBL(views.companion.master.IssueAssetOBL.form.fill(views.companion.master.IssueAssetOBL.Data(issueAssetRequestID, obl.billOfLadingID, obl.portOfLoading, obl.shipperName, obl.shipperAddress, obl.notifyPartyName, obl.notifyPartyAddress, obl.dateOfShipping, obl.deliveryTerm, obl.weightOfConsignment, obl.declaredAssetValue)), masterTransactionAssetFiles.Service.getOrNone(issueAssetRequestID, constants.File.OBL)))
          case constants.File.INVOICE =>
            val invoice = masterTransactionAssetFiles.Service.getOrEmpty(issueAssetRequestID, constants.File.INVOICE).context.getOrElse(Serializable.Invoice("", new Date)).asInstanceOf[Serializable.Invoice]
            PartialContent(views.html.component.master.issueAssetInvoice(views.companion.master.IssueAssetInvoice.form.fill(views.companion.master.IssueAssetInvoice.Data(issueAssetRequestID, invoice.invoiceNumber, invoice.invoiceDate)), masterTransactionAssetFiles.Service.getOrNone(issueAssetRequestID, constants.File.INVOICE)))
          case constants.File.CONTRACT | constants.File.PACKING_LIST | constants.File.COO | constants.File.COA | constants.File.OTHER => PartialContent(views.html.component.master.issueAssetDocument(issueAssetRequestID, masterTransactionAssetFiles.Service.getDocuments(issueAssetRequestID, constants.File.TRADER_ASSET_DOCUMENT_TYPES_UPLOAD_PAGE)))
          case _ => Ok(views.html.index())
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
          document = masterTransaction.AssetFile(id = issueAssetRequestID, documentType = documentType, fileName = name, file = None, context = None, status = None),
          updateOldDocument = masterTransactionAssetFiles.Service.insertOrUpdateOldDocument
        )
        documentType match {
          case constants.File.OBL =>
            val obl = masterTransactionAssetFiles.Service.getOrEmpty(issueAssetRequestID, constants.File.OBL).context.getOrElse(Serializable.OBL("", "", "", "", "", "", new Date, "", 0, 0)).asInstanceOf[Serializable.OBL]
            PartialContent(views.html.component.master.issueAssetOBL(views.companion.master.IssueAssetOBL.form.fill(views.companion.master.IssueAssetOBL.Data(issueAssetRequestID, obl.billOfLadingID, obl.portOfLoading, obl.shipperName, obl.shipperAddress, obl.notifyPartyName, obl.notifyPartyAddress, obl.dateOfShipping, obl.deliveryTerm, obl.weightOfConsignment, obl.declaredAssetValue)), masterTransactionAssetFiles.Service.getOrNone(issueAssetRequestID, constants.File.OBL)))
          case constants.File.INVOICE =>
            val invoice = masterTransactionAssetFiles.Service.getOrEmpty(issueAssetRequestID, constants.File.INVOICE).context.getOrElse(Serializable.Invoice("", new Date)).asInstanceOf[Serializable.Invoice]
            PartialContent(views.html.component.master.issueAssetInvoice(views.companion.master.IssueAssetInvoice.form.fill(views.companion.master.IssueAssetInvoice.Data(issueAssetRequestID, invoice.invoiceNumber, invoice.invoiceDate)), masterTransactionAssetFiles.Service.getOrNone(issueAssetRequestID, constants.File.INVOICE)))
          case constants.File.CONTRACT | constants.File.PACKING_LIST | constants.File.COO | constants.File.COA | constants.File.OTHER => PartialContent(views.html.component.master.issueAssetDocument(issueAssetRequestID, masterTransactionAssetFiles.Service.getDocuments(issueAssetRequestID, constants.File.TRADER_ASSET_DOCUMENT_TYPES_UPLOAD_PAGE)))
          case _ => Ok(views.html.index())
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
          document = masterTransaction.NegotiationFile(id = negotiationRequestID, documentType = documentType, fileName = name, file = None, context = None, status = None),
          masterCreate = masterTransactionNegotiationFiles.Service.create
        )
        documentType match {

          case _ => Ok(views.html.index())
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
          oldDocumentFileName = masterTransactionAssetFiles.Service.getFileName(id = negotiationRequestID, documentType = documentType),
          document = masterTransaction.NegotiationFile(id = negotiationRequestID, documentType = documentType, fileName = name, file = None, context = None, status = None),
          updateOldDocument = masterTransactionNegotiationFiles.Service.insertOrUpdateOldDocument
        )
        documentType match {

          case _ => Ok(views.html.index())
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def genesisAccessedFile(fileName: String, documentType: String): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        documentType match {
          case constants.File.BANK_DETAILS => Ok.sendFile(utilities.FileOperations.fetchFile(path = uploadZoneKycBankDetailsPath, fileName = fileName))
          case constants.File.IDENTIFICATION => Ok.sendFile(utilities.FileOperations.fetchFile(path = uploadZoneKycIdentificationPath, fileName = fileName))
          case _ => Unauthorized
        }
      } catch {
        case _: NoSuchFileException => InternalServerError(views.html.index(failures = Seq(constants.Response.NO_SUCH_FILE_EXCEPTION)))
        case _: Exception => InternalServerError(views.html.index(failures = Seq(constants.Response.GENERIC_EXCEPTION)))
      }
  }

  def zoneAccessedOrganizationKYCFile(organizationID: String, fileName: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        if (masterOrganizations.Service.getZoneID(organizationID) == masterZones.Service.getZoneId(loginState.username)) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getOrganizationKycFilePath(documentType), fileName = fileName))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case _: NoSuchFileException => InternalServerError(views.html.index(failures = Seq(constants.Response.NO_SUCH_FILE_EXCEPTION)))
        case _: NoSuchElementException => InternalServerError(views.html.index(failures = Seq(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)))
      }
  }

  def zoneAccessedTraderKYCFile(accountID: String, fileName: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        if (masterTraders.Service.getByAccountID(accountID).zoneID == masterZones.Service.getZoneId(loginState.username)) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getTraderKycFilePath(documentType), fileName = fileName))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case _: NoSuchFileException => InternalServerError(views.html.index(failures = Seq(constants.Response.NO_SUCH_FILE_EXCEPTION)))
        case _: Exception => InternalServerError(views.html.index(failures = Seq(constants.Response.GENERIC_EXCEPTION)))
      }
  }

  def zoneAccessedAssetFile(id: String, fileName: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        if (masterTraders.Service.getByAccountID(masterTransactionIssueAssetRequests.Service.getAccountID(id)).zoneID == masterZones.Service.getZoneId(loginState.username)) {
          documentType match {
            case constants.File.OBL => Ok.sendFile(utilities.FileOperations.fetchFile(path = uploadTraderAssetOBLPath, fileName = fileName))
            case constants.File.CONTRACT => Ok.sendFile(utilities.FileOperations.fetchFile(path = uploadTraderAssetContractPath, fileName = fileName))
            case constants.File.PACKING_LIST => Ok.sendFile(utilities.FileOperations.fetchFile(path = uploadTraderAssetPackingListPath, fileName = fileName))
            case constants.File.INVOICE => Ok.sendFile(utilities.FileOperations.fetchFile(path = uploadTraderAssetInvoicePath, fileName = fileName))
            case constants.File.COO => Ok.sendFile(utilities.FileOperations.fetchFile(path = uploadTraderAssetCOOPath, fileName = fileName))
            case constants.File.COA => Ok.sendFile(utilities.FileOperations.fetchFile(path = uploadTraderAssetCOAPath, fileName = fileName))
            case constants.File.OTHER => Ok.sendFile(utilities.FileOperations.fetchFile(path = uploadTraderAssetOtherPath, fileName = fileName))
            case _ => Unauthorized
          }
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case _: NoSuchFileException => InternalServerError(views.html.index(failures = Seq(constants.Response.NO_SUCH_FILE_EXCEPTION)))
        case _: Exception => InternalServerError(views.html.index(failures = Seq(constants.Response.GENERIC_EXCEPTION)))
      }
  }

  def organizationAccessedFile(accountID: String, fileName: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        if (masterTraders.Service.getOrganizationIDByAccountID(loginState.username) == masterOrganizations.Service.getID(loginState.username)) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getTraderKycFilePath(documentType), fileName = fileName))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case _: NoSuchFileException => InternalServerError(views.html.index(failures = Seq(constants.Response.NO_SUCH_FILE_EXCEPTION)))
        case _: Exception => InternalServerError(views.html.index(failures = Seq(constants.Response.GENERIC_EXCEPTION)))
      }
  }

  def organizationAccessedTraderKYCFile(accountID: String, fileName: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        if (masterTraders.Service.getOrganizationIDByAccountID(loginState.username) == masterOrganizations.Service.getID(loginState.username)) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getTraderKycFilePath(documentType), fileName = fileName))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case _: NoSuchFileException => InternalServerError(views.html.index(failures = Seq(constants.Response.NO_SUCH_FILE_EXCEPTION)))
        case _: Exception => InternalServerError(views.html.index(failures = Seq(constants.Response.GENERIC_EXCEPTION)))
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
          case constants.User.ZONE => if (masterZoneKYCs.Service.checkFileNameExists(id = loginState.username, fileName = fileName)) fileResourceManager.getZoneKycFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
          case constants.User.ORGANIZATION => if (masterOrganizationKYCs.Service.checkFileNameExists(id = masterOrganizations.Service.getID(loginState.username), fileName = fileName)) fileResourceManager.getOrganizationKycFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
          case constants.User.TRADER => if (masterTraderKYCs.Service.checkFileNameExists(id = masterTraders.Service.getID(loginState.username), fileName = fileName)) fileResourceManager.getTraderKycFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
          case constants.User.USER => if (masterAccountKYCs.Service.checkFileNameExists(id = loginState.username, fileName = fileName)) fileResourceManager.getAccountKycFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
          case _ => if (masterAccountFiles.Service.checkFileNameExists(id = loginState.username, fileName = fileName)) fileResourceManager.getAccountFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
        }
        Ok.sendFile(utilities.FileOperations.fetchFile(path = path, fileName = fileName))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        case _: NoSuchFileException => InternalServerError(views.html.index(failures = Seq(constants.Response.NO_SUCH_FILE_EXCEPTION)))
      }
  }

  def tradingFile(id: String, fileName: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val path: String = documentType match {
          case constants.File.CONTRACT | constants.File.OBL | constants.File.PACKING_LIST | constants.File.INVOICE| constants.File.COO| constants.File.COA| constants.File.OTHER => if (masterTransactionIssueAssetRequests.Service.getAccountID(id) == loginState.username) fileResourceManager.getTraderAssetFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
          case constants.File.BUYER_CONTRACT | constants.File.SELLER_CONTRACT | constants.File.FIAT_PROOF | constants.File.AWB_PROOF => if (masterTransactionNegotiationRequests.Service.checkNegotiationAndAccountIDExists(id, loginState.username)) fileResourceManager.getTraderNegotiationFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
       }
        Ok.sendFile(utilities.FileOperations.fetchFile(path = path, fileName = fileName))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        case _: NoSuchFileException => InternalServerError(views.html.index(failures = Seq(constants.Response.NO_SUCH_FILE_EXCEPTION)))
      }
  }

}
