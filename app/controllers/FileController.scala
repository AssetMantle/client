package controllers

import java.nio.file.{Files, NoSuchFileException}

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject._
import models.{blockchain, master}
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import views.companion.master.FileUpload

import scala.concurrent.ExecutionContext

@Singleton
class FileController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, masterAccountFiles: master.AccountFiles, blockchainACLs: blockchain.ACLAccounts, masterAccounts: master.Accounts, masterZones: master.Zones, masterOrganizations: master.Organizations, masterTraders: master.Traders, masterAccountKYCs: master.AccountKYCs, fileResourceManager: utilities.FileResourceManager, withGenesisLoginAction: WithGenesisLoginAction, withUserLoginAction: WithUserLoginAction, masterZoneKYCs: master.ZoneKYCs, withZoneLoginAction: WithZoneLoginAction, masterOrganizationKYCs: master.OrganizationKYCs, withOrganizationLoginAction: WithOrganizationLoginAction, masterTraderKYCs: master.TraderKYCs, withTraderLoginAction: WithTraderLoginAction, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext, configuration: Configuration, wsClient: WSClient) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.FILE_CONTROLLER

  private val uploadAccountKycBankDetailsPath: String = configuration.get[String]("upload.account.bankDetailsPath")

  private val uploadAccountKycIdentificationPath: String = configuration.get[String]("upload.account.identificationPath")

  private val uploadAccountProfilePicturePath: String = configuration.get[String]("upload.account.profilePicturePath")

  private val uploadZoneKycBankDetailsPath: String = configuration.get[String]("upload.zone.bankDetailsPath")

  private val uploadZoneKycIdentificationPath: String = configuration.get[String]("upload.zone.identificationPath")

  private val uploadOrganizationKycBankDetailsPath: String = configuration.get[String]("upload.organization.bankDetailsPath")

  private val uploadOrganizationKycIdentificationPath: String = configuration.get[String]("upload.organization.identificationPath")

  private val uploadTraderKycIdentificationPath: String = configuration.get[String]("upload.trader.identificationPath")

  def checkAccountKycFileExists(accountID: String, documentType: String): Action[AnyContent] = Action { implicit request =>
    if (masterAccountKYCs.Service.checkFileExists(id = accountID, documentType = documentType)) Ok else NoContent
  }

  def checkZoneKycFileExists(accountID: String, documentType: String): Action[AnyContent] = Action { implicit request =>
    if (masterZoneKYCs.Service.checkFileExists(id = accountID, documentType = documentType)) Ok else NoContent
  }

  def checkOrganizationKycFileExists(accountID: String, documentType: String): Action[AnyContent] = Action { implicit request =>
    if (masterOrganizationKYCs.Service.checkFileExists(id = accountID, documentType = documentType)) Ok else NoContent
  }

  def checkTraderKycFileExists(accountID: String, documentType: String): Action[AnyContent] = Action { implicit request =>
    if (masterTraderKYCs.Service.checkFileExists(id = accountID, documentType = documentType)) Ok else NoContent
  }

  def uploadUserKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.userKycFileUpload(documentType = documentType))
  }

  def updateUserKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.userKycFileUpdate(documentType = documentType))
  }

  def uploadUserKyc(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errors.mkString("\n"))
      },
      fileUploadInfo => {
        try {
          request.body.file("file") match {
            case None => BadRequest(Messages(constants.Response.NO_FILE.message))
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
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
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
          document = master.AccountKYC(id = loginState.username, documentType = documentType, status = None, fileName = name, file  = None),
          updateOldDocument = masterAccountKYCs.Service.updateOldDocument
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def uploadUserZoneKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.userZoneKycFileUpload(documentType = documentType))
  }

  def updateUserZoneKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.userZoneKycFileUpdate(documentType = documentType))
  }

  def uploadUserZoneKyc(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errors.mkString("\n"))
      },
      fileUploadInfo => {
        try {
          request.body.file("file") match {
            case None => BadRequest(Messages(constants.Response.NO_FILE.message))
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
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
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
          document = master.ZoneKYC(id = loginState.username, documentType = documentType, status = None, fileName = name, file  = None),
          updateOldDocument = masterZoneKYCs.Service.updateOldDocument
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def uploadUserOrganizationKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.userOrganizationKycFileUpload(documentType = documentType))
  }

  def updateUserOrganizationKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.userOrganizationKycFileUpdate(documentType = documentType))
  }

  def uploadUserOrganizationKyc(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errors.mkString("\n"))
      },
      fileUploadInfo => {
        try {
          request.body.file("file") match {
            case None => BadRequest(Messages(constants.Response.NO_FILE.message))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getOrganizationKycFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeUserOrganizationKyc(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.storeFile[master.OrganizationKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getOrganizationKycFilePath(documentType),
          document = master.OrganizationKYC(id = loginState.username, documentType = documentType, status = None, fileName = name, file = None),
          masterCreate = masterOrganizationKYCs.Service.create
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def updateUserOrganizationKyc(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.updateFile[master.OrganizationKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getOrganizationKycFilePath(documentType),
          oldDocumentFileName = masterOrganizationKYCs.Service.getFileName(id = loginState.username, documentType = documentType),
          document = master.OrganizationKYC(id = loginState.username, documentType = documentType, status = None, fileName = name, file  = None),
          updateOldDocument = masterOrganizationKYCs.Service.updateOldDocument
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def uploadUserTraderKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.userTraderKycFileUpload(documentType = documentType))
  }

  def updateUserTraderKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.userTraderKycFileUpdate(documentType = documentType))
  }

  def uploadUserTraderKyc(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errors.mkString("\n"))
      },
      fileUploadInfo => {
        try {
          request.body.file("file") match {
            case None => BadRequest(Messages(constants.Response.NO_FILE.message))
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

  def storeUserTraderKyc(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
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
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def updateUserTraderKyc(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.updateFile[master.TraderKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getTraderKycFilePath(documentType),
          oldDocumentFileName = masterTraderKYCs.Service.getFileName(id = loginState.username, documentType = documentType),
          document = master.TraderKYC(id = loginState.username, documentType = documentType, fileName = name, file  = None, zoneStatus = None, organizationStatus = None),
          updateOldDocument = masterTraderKYCs.Service.updateOldDocument
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def uploadZoneKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.zoneKycFileUpload(documentType = documentType))
  }

  def updateZoneKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.zoneKycFileUpdate(documentType = documentType))
  }

  def uploadZoneKyc(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errors.mkString("\n"))
      },
      fileUploadInfo => {
        try {
          request.body.file("file") match {
            case None => BadRequest(Messages(constants.Response.NO_FILE.message))
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
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
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
          document = master.ZoneKYC(id = loginState.username, documentType = documentType, status = None, fileName = name, file  = None),
          updateOldDocument = masterZoneKYCs.Service.updateOldDocument
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def uploadOrganizationKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.organizationKycFileUpload(documentType = documentType))
  }

  def updateOrganizationKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.organizationKycFileUpdate(documentType = documentType))
  }

  def uploadOrganizationKyc(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errors.mkString("\n"))
      },
      fileUploadInfo => {
        try {
          request.body.file("file") match {
            case None => BadRequest(Messages(constants.Response.NO_FILE.message))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getOrganizationKycFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeOrganizationKyc(name: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.storeFile[master.OrganizationKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getOrganizationKycFilePath(documentType),
          document = master.OrganizationKYC(id = loginState.username, documentType = documentType, status = None, fileName = name, file = None),
          masterCreate = masterOrganizationKYCs.Service.create
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def updateOrganizationKyc(name: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.updateFile[master.OrganizationKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getOrganizationKycFilePath(documentType),
          oldDocumentFileName = masterOrganizationKYCs.Service.getFileName(id = loginState.username, documentType = documentType),
          document = master.OrganizationKYC(id = loginState.username, documentType = documentType, status = None, fileName = name, file  = None),
          updateOldDocument = masterOrganizationKYCs.Service.updateOldDocument
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }


  def uploadTraderKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.traderKycFileUpload(documentType = documentType))
  }

  def updateTraderKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.traderKycFileUpdate(documentType = documentType))
  }

  def uploadTraderKyc(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errors.mkString("\n"))
      },
      fileUploadInfo => {
        try {
          request.body.file("file") match {
            case None => BadRequest(Messages(constants.Response.NO_FILE.message))
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

  def storeTraderKyc(name: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
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
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def updateTraderKyc(name: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.updateFile[master.TraderKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getTraderKycFilePath(documentType),
          oldDocumentFileName = masterTraderKYCs.Service.getFileName(id = loginState.username, documentType = documentType),
          document = master.TraderKYC(id = loginState.username, documentType = documentType, fileName = name, file  = None, zoneStatus = None, organizationStatus = None),
          updateOldDocument = masterTraderKYCs.Service.updateOldDocument
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }


  def genesisAccessedFile(fileName: String, documentType: String): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        documentType match {
          case constants.File.BANK_DETAILS => withUserLoginAction.Ok.sendFile(utilities.FileOperations.fetchFile(path = uploadZoneKycBankDetailsPath, fileName = fileName))
          case constants.File.IDENTIFICATION => withUserLoginAction.Ok.sendFile(utilities.FileOperations.fetchFile(path = uploadZoneKycIdentificationPath, fileName = fileName))
          case _ => Unauthorized
        }
      } catch {
        case _: NoSuchFileException => InternalServerError(views.html.index(failures = Seq(constants.Response.NO_SUCH_FILE_EXCEPTION)))
        case _: Exception => InternalServerError(views.html.index(failures = Seq(constants.Response.GENERIC_EXCEPTION)))
      }
  }

  def zoneAccessedOrganizationFile(accountID: String, fileName: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        if (masterOrganizations.Service.getByAccountID(accountID).zoneID == masterZones.Service.getZoneId(loginState.username)) {
          documentType match {
            case constants.File.BANK_DETAILS => withUserLoginAction.Ok.sendFile(utilities.FileOperations.fetchFile(path = uploadOrganizationKycBankDetailsPath, fileName = fileName))
            case constants.File.IDENTIFICATION => withUserLoginAction.Ok.sendFile(utilities.FileOperations.fetchFile(path = uploadOrganizationKycIdentificationPath, fileName = fileName))
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

  def zoneAccessedTraderFile(accountID: String, fileName: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        if (masterTraders.Service.getByAccountID(accountID).zoneID == masterZones.Service.getZoneId(loginState.username)) {
          documentType match {
            case constants.File.IDENTIFICATION => withUserLoginAction.Ok.sendFile(utilities.FileOperations.fetchFile(path = uploadTraderKycIdentificationPath, fileName = fileName))
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
        if (masterTraders.Service.getByAccountID(accountID).organizationID == masterOrganizations.Service.getByAccountID(loginState.username).id) {
          documentType match {
            case constants.File.IDENTIFICATION => withUserLoginAction.Ok.sendFile(utilities.FileOperations.fetchFile(path = uploadTraderKycIdentificationPath, fileName = fileName))
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

  def uploadAccountFileForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadAccountFile(documentType = documentType))
  }

  def updateAccountFileForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateAccountFile(documentType = documentType))
  }

  def uploadAccountFile(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errors.mkString("\n"))
      },
      fileUploadInfo => {
        try {
          request.body.file("file") match {
            case None => BadRequest(Messages(constants.Response.NO_FILE.message))
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
        withUserLoginAction.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
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
          document = master.AccountFile(id = loginState.username, documentType = documentType, fileName = name, file  = None),
          updateOldDocument = masterAccountFiles.Service.updateOldDocument
        )
        withUserLoginAction.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def file(fileName: String, documentType: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val path: String = loginState.userType match {
          case constants.User.ZONE => if (masterZoneKYCs.Service.checkFileNameExists(id = loginState.username, fileName = fileName)) fileResourceManager.getZoneKycFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
          case constants.User.ORGANIZATION => if (masterOrganizationKYCs.Service.checkFileNameExists(id = loginState.username, fileName = fileName)) fileResourceManager.getOrganizationKycFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
          case constants.User.TRADER => if (masterTraderKYCs.Service.checkFileNameExists(id = loginState.username, fileName = fileName)) fileResourceManager.getTraderKycFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
          case constants.User.USER => if (masterAccountKYCs.Service.checkFileNameExists(id = loginState.username, fileName = fileName)) fileResourceManager.getAccountKycFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
          case _ => if (masterAccountFiles.Service.checkFileNameExists(id = loginState.username, fileName = fileName)) fileResourceManager.getAccountFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
        }
        Ok.sendFile(utilities.FileOperations.fetchFile(path = path, fileName = fileName))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
        case _: NoSuchFileException => InternalServerError(Messages(constants.Response.NO_SUCH_FILE_EXCEPTION.message))
        case _: Exception => InternalServerError(Messages(constants.Response.GENERIC_EXCEPTION.message))
      }
  }

}
