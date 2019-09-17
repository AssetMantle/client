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

  def storeUserKyc(name: String, documentType: String, id: String = ""): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
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

  def updateUserKyc(name: String, documentType: String, id: String = ""): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
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

  def storeUserZoneKyc(name: String, documentType: String, id: String = ""): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
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

  def updateUserZoneKyc(name: String, documentType: String, id: String = ""): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
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

  def uploadUserTraderKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadUserTraderKyc), utilities.String.getJsRouteFunction(routes.javascript.FileController.storeUserTraderKyc), documentType))
  }

  def updateUserTraderKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadUserTraderKyc), utilities.String.getJsRouteFunction(routes.javascript.FileController.updateUserTraderKyc), documentType))
  }

  def uploadUserTraderKyc(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errors.mkString("\n"))
      },
      fileUploadInfo => {
        try {
          request.body.file("file") match {
            case None => BadRequest(views.html.index(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getTraderKycFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeUserTraderKyc(name: String, documentType: String, id: String = ""): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.storeFile[master.TraderKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getTraderKycFilePath(documentType),
          document = master.TraderKYC(id = loginState.username, documentType = documentType, fileName = name, file = None, zoneStatus = None, organizationStatus = None),
          masterCreate = masterTraderKYCs.Service.create
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateUserTraderKyc(name: String, documentType: String, id: String = ""): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.updateFile[master.TraderKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getTraderKycFilePath(documentType),
          oldDocumentFileName = masterTraderKYCs.Service.getFileName(id = loginState.username, documentType = documentType),
          document = master.TraderKYC(id = loginState.username, documentType = documentType, fileName = name, file = None, zoneStatus = None, organizationStatus = None),
          updateOldDocument = masterTraderKYCs.Service.updateOldDocument
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

  def storeZoneKyc(name: String, documentType: String, id: String = ""): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
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

  def updateZoneKyc(name: String, documentType: String, id: String = ""): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
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
        if (masterTraders.Service.geOrganizationIDByAccountID(loginState.username) == masterOrganizations.Service.getID(loginState.username)) {
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

  def storeAccountFile(name: String, documentType: String, id: String = ""): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
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

  def updateAccountFile(name: String, documentType: String, id: String = ""): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
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
