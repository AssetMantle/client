package controllers

import java.nio.file.{Files, NoSuchFileException}

import controllers.actions.{WithGenesisLoginAction, WithOrganizationLoginAction, WithUserLoginAction, WithZoneLoginAction}
import exceptions.BaseException
import javax.inject._
import models.{blockchain, master}
import org.apache.commons.codec.binary.Base64
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import views.companion.master.FileUpload

import scala.concurrent.ExecutionContext

@Singleton
class FileController @Inject()(messagesControllerComponents: MessagesControllerComponents, blockchainACLs: blockchain.ACLAccounts, masterAccounts: master.Accounts, masterZones: master.Zones, masterOrganizations: master.Organizations, masterAccountKYCs: master.AccountKYCs, imageProcess: utilities.ImageProcess, fileResourceManager: utilities.FileResourceManager, withGenesisLoginAction: WithGenesisLoginAction, withUserLoginAction: WithUserLoginAction, masterZoneKYCs: master.ZoneKYCs, withZoneLoginAction: WithZoneLoginAction, masterOrganizationKYCs: master.OrganizationKYCs, withOrganizationLoginAction: WithOrganizationLoginAction)(implicit exec: ExecutionContext, configuration: Configuration, wsClient: WSClient) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val uploadAccountKycBankDetailsPath = configuration.get[String]("upload.accountKYCsBankDetailsPath")

  private val uploadAccountKycIdentificationPath = configuration.get[String]("upload.accountKYCsIdentificationPath")

  private val uploadZoneKycBankDetailsPath = configuration.get[String]("upload.zoneKYCsBankDetailsPath")

  private val uploadZoneKycIdentificationPath = configuration.get[String]("upload.zoneKYCsIdentificationPath")

  private val uploadOrganizationKycBankDetailsPath = configuration.get[String]("upload.organizationKYCsBankDetailsPath")

  private val uploadOrganizationKycIdentificationPath = configuration.get[String]("upload.organizationKYCsIdentificationPath")

  def userKYCUploadForm(documentType: String, uploadType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.userFileUpload(documentType, uploadType))
  }

  def uploadUserKYC(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errors.mkString("\n"))
      },
      fileUploadInfo => {
        try {
          request.body.file("file") match {
            case None => BadRequest("No file")
            case Some(file) =>
              utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getAccountFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeUserKYC(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit username =>
    implicit request =>
      val path = fileResourceManager.getAccountFilePath(documentType)
      try {
        val (fileName, encodedBase64) = fileResourceManager.fileExtensionFromName(name) match {
          case constants.File.JPEG | constants.File.JPG | constants.File.PNG => imageProcess.convertToThumbnail(name, path)
          case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(fileResourceManager.convertToByteArray(fileResourceManager.newFile(path, name)))).toString, fileResourceManager.fileExtensionFromName(name)).mkString("."), null)
        }
        masterAccountKYCs.Service.create(id = username, documentType = documentType, fileName = fileName, file = Option(encodedBase64))
        fileResourceManager.renameFile(path, name, fileName)
        Ok(Messages(constants.Response.UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => fileResourceManager.deleteFile(path, name)
          BadRequest(Messages(baseException.failure.message))
        case e: Exception => fileResourceManager.deleteFile(path, name)
          BadRequest(Messages(e.getMessage))
      }
  }

  def updateUserKYC(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { username =>
    implicit request =>
      val newPath = fileResourceManager.getAccountFilePath(documentType)
      try {
        val oldAccountKYC = masterAccountKYCs.Service.get(id = username, documentType = documentType)
        val (fileName, encodedBase64) = fileResourceManager.fileExtensionFromName(name) match {
          case constants.File.JPEG | constants.File.JPG | constants.File.PNG => imageProcess.convertToThumbnail(name, newPath)
          case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(fileResourceManager.convertToByteArray(fileResourceManager.newFile(newPath, name)))).toString, fileResourceManager.fileExtensionFromName(name)).mkString("."), null)
        }
        fileResourceManager.deleteFile(fileResourceManager.getAccountFilePath(oldAccountKYC.documentType), oldAccountKYC.fileName)
        masterAccountKYCs.Service.updateOldDocument(id = username, documentType = documentType, fileName = fileName, file = Option(encodedBase64))
        fileResourceManager.renameFile(newPath, name, fileName)
        Ok(Messages(constants.Response.UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => fileResourceManager.deleteFile(newPath, name)
          BadRequest(Messages(baseException.failure.message))
        case e: Exception => fileResourceManager.deleteFile(newPath, name)
          BadRequest(Messages(e.getMessage))
      }
  }

  def uploadZoneKycUser(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errors.mkString("\n"))
      },
      fileUploadInfo => {
        try {
          request.body.file("file") match {
            case None => BadRequest("No file")
            case Some(file) =>
              utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getZoneFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeZoneKycUser(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit username =>
    implicit request =>
      val path = fileResourceManager.getZoneFilePath(documentType)
      try {
        val (fileName, encodedBase64) = fileResourceManager.fileExtensionFromName(name) match {
          case constants.File.JPEG | constants.File.JPG | constants.File.PNG => imageProcess.convertToThumbnail(name, path)
          case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(fileResourceManager.convertToByteArray(fileResourceManager.newFile(path, name)))).toString, fileResourceManager.fileExtensionFromName(name)).mkString("."), null)
        }
        masterZoneKYCs.Service.create(id = username, documentType = documentType, fileName = fileName, file = Option(encodedBase64))
        fileResourceManager.renameFile(path, name, fileName)
        Ok(Messages(constants.Response.UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => fileResourceManager.deleteFile(path, name)
          BadRequest(Messages(baseException.failure.message))
        case e: Exception => fileResourceManager.deleteFile(path, name)
          BadRequest(Messages(e.getMessage))
      }
  }

  def updateZoneKycUser(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { username =>
    implicit request =>
      val newPath = fileResourceManager.getZoneFilePath(documentType)
      try {
        val oldAccountKYC = masterZoneKYCs.Service.get(id = username, documentType = documentType)
        val (fileName, encodedBase64) = fileResourceManager.fileExtensionFromName(name) match {
          case constants.File.JPEG | constants.File.JPG | constants.File.PNG => imageProcess.convertToThumbnail(name, newPath)
          case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(fileResourceManager.convertToByteArray(fileResourceManager.newFile(newPath, name)))).toString, fileResourceManager.fileExtensionFromName(name)).mkString("."), null)
        }
        fileResourceManager.deleteFile(fileResourceManager.getZoneFilePath(oldAccountKYC.documentType), oldAccountKYC.fileName)
        masterZoneKYCs.Service.updateOldDocument(id = username, documentType = documentType, fileName = fileName, file = Option(encodedBase64))
        fileResourceManager.renameFile(newPath, name, fileName)
        Ok(Messages(constants.Response.UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => fileResourceManager.deleteFile(newPath, name)
          BadRequest(Messages(baseException.failure.message))
        case e: Exception => fileResourceManager.deleteFile(newPath, name)
          BadRequest(Messages(e.getMessage))
      }
  }

  def uploadOrganizationKycUser(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errors.mkString("\n"))
      },
      fileUploadInfo => {
        try {
          request.body.file("file") match {
            case None => BadRequest("No file")
            case Some(file) =>
              utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getOrganizationFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeOrganizationKycUser(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit username =>
    implicit request =>
      val path = fileResourceManager.getOrganizationFilePath(documentType)
      try {
        val (fileName, encodedBase64) = fileResourceManager.fileExtensionFromName(name) match {
          case constants.File.JPEG | constants.File.JPG | constants.File.PNG => imageProcess.convertToThumbnail(name, path)
          case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(fileResourceManager.convertToByteArray(fileResourceManager.newFile(path, name)))).toString, fileResourceManager.fileExtensionFromName(name)).mkString("."), null)
        }
        masterOrganizationKYCs.Service.create(id = username, documentType = documentType, fileName = fileName, file = Option(encodedBase64))
        fileResourceManager.renameFile(path, name, fileName)
        Ok(Messages(constants.Response.UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => fileResourceManager.deleteFile(path, name)
          BadRequest(Messages(baseException.failure.message))
        case e: Exception => fileResourceManager.deleteFile(path, name)
          BadRequest(Messages(e.getMessage))
      }
  }

  def updateOrganizationKycUser(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { username =>
    implicit request =>
      val newPath = fileResourceManager.getOrganizationFilePath(documentType)
      try {
        val oldAccountKYC = masterOrganizationKYCs.Service.get(id = username, documentType = documentType)
        val (fileName, encodedBase64) = fileResourceManager.fileExtensionFromName(name) match {
          case constants.File.JPEG | constants.File.JPG | constants.File.PNG => imageProcess.convertToThumbnail(name, newPath)
          case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(fileResourceManager.convertToByteArray(fileResourceManager.newFile(newPath, name)))).toString, fileResourceManager.fileExtensionFromName(name)).mkString("."), null)
        }
        fileResourceManager.deleteFile(fileResourceManager.getOrganizationFilePath(oldAccountKYC.documentType), oldAccountKYC.fileName)
        masterOrganizationKYCs.Service.updateOldDocument(id = username, documentType = documentType, fileName = fileName, file = Option(encodedBase64))
        fileResourceManager.renameFile(newPath, name, fileName)
        Ok(Messages(constants.Response.UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => fileResourceManager.deleteFile(newPath, name)
          BadRequest(Messages(baseException.failure.message))
        case e: Exception => fileResourceManager.deleteFile(newPath, name)
          BadRequest(Messages(e.getMessage))
      }
  }

  def zoneKYCUploadForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.zoneFileUpload(documentType))
  }

  def uploadZoneKYC(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errors.mkString("\n"))
      },
      fileUploadInfo => {
        try {
          request.body.file("file") match {
            case None => BadRequest("No file")
            case Some(file) =>
              utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getZoneFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeZoneKYC(name: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit username =>
    implicit request =>
      val path = fileResourceManager.getZoneFilePath(documentType)
      try {
        val (fileName, encodedBase64) = fileResourceManager.fileExtensionFromName(name) match {
          case constants.File.JPEG | constants.File.JPG | constants.File.PNG => imageProcess.convertToThumbnail(name, path)
          case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(fileResourceManager.convertToByteArray(fileResourceManager.newFile(path, name)))).toString, fileResourceManager.fileExtensionFromName(name)).mkString("."), null)
        }
        masterZoneKYCs.Service.create(id = username, documentType = documentType, fileName = fileName, file = Option(encodedBase64))
        fileResourceManager.renameFile(path, name, fileName)
        Ok(Messages(constants.Response.UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => fileResourceManager.deleteFile(path, name)
          BadRequest(Messages(baseException.failure.message))
        case e: Exception => fileResourceManager.deleteFile(path, name)
          BadRequest(Messages(e.getMessage))
      }
  }

  def updateZoneKYC(name: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { username =>
    implicit request =>
      val newPath = fileResourceManager.getZoneFilePath(documentType)
      try {
        val oldAccountKYC = masterZoneKYCs.Service.get(id = username, documentType = documentType)
        val (fileName, encodedBase64) = fileResourceManager.fileExtensionFromName(name) match {
          case constants.File.JPEG | constants.File.JPG | constants.File.PNG => imageProcess.convertToThumbnail(name, newPath)
          case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(fileResourceManager.convertToByteArray(fileResourceManager.newFile(newPath, name)))).toString, fileResourceManager.fileExtensionFromName(name)).mkString("."), null)
        }
        fileResourceManager.deleteFile(fileResourceManager.getZoneFilePath(oldAccountKYC.documentType), oldAccountKYC.fileName)
        masterZoneKYCs.Service.updateOldDocument(id = username, documentType = documentType, fileName = fileName, file = Option(encodedBase64))
        fileResourceManager.renameFile(newPath, name, fileName)
        Ok(Messages(constants.Response.UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => fileResourceManager.deleteFile(newPath, name)
          BadRequest(Messages(baseException.failure.message))
        case e: Exception => fileResourceManager.deleteFile(newPath, name)
          BadRequest(Messages(e.getMessage))
      }
  }

  def organizationKYCUploadForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.organizationFileUpload(documentType))
  }


  def uploadOrganizationKYC(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errors.mkString("\n"))
      },
      fileUploadInfo => {
        try {
          request.body.file("file") match {
            case None => BadRequest("No file")
            case Some(file) =>
              utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getOrganizationFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeOrganizationKYC(name: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit username =>
    implicit request =>
      val path = fileResourceManager.getOrganizationFilePath(documentType)
      try {
        val (fileName, encodedBase64) = fileResourceManager.fileExtensionFromName(name) match {
          case constants.File.JPEG | constants.File.JPG | constants.File.PNG => imageProcess.convertToThumbnail(name, path)
          case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(fileResourceManager.convertToByteArray(fileResourceManager.newFile(path, name)))).toString, fileResourceManager.fileExtensionFromName(name)).mkString("."), null)
        }
        masterOrganizationKYCs.Service.create(id = username, documentType = documentType, fileName = fileName, file = Option(encodedBase64))
        fileResourceManager.renameFile(path, name, fileName)
        Ok(Messages(constants.Response.UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => fileResourceManager.deleteFile(path, name)
          BadRequest(Messages(baseException.failure.message))
        case e: Exception => fileResourceManager.deleteFile(path, name)
          BadRequest(Messages(e.getMessage))
      }
  }

  def updateOrganizationKYC(name: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { username =>
    implicit request =>
      val newPath = fileResourceManager.getOrganizationFilePath(documentType)
      try {
        val oldAccountKYC = masterOrganizationKYCs.Service.get(id = username, documentType = documentType)
        val (fileName, encodedBase64) = fileResourceManager.fileExtensionFromName(name) match {
          case constants.File.JPEG | constants.File.JPG | constants.File.PNG => imageProcess.convertToThumbnail(name, newPath)
          case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(fileResourceManager.convertToByteArray(fileResourceManager.newFile(newPath, name)))).toString, fileResourceManager.fileExtensionFromName(name)).mkString("."), null)
        }
        fileResourceManager.deleteFile(fileResourceManager.getOrganizationFilePath(oldAccountKYC.documentType), oldAccountKYC.fileName)
        masterOrganizationKYCs.Service.updateOldDocument(id = username, documentType = documentType, fileName = fileName, file = Option(encodedBase64))
        fileResourceManager.renameFile(newPath, name, fileName)
        Ok(Messages(constants.Response.UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => fileResourceManager.deleteFile(newPath, name)
          BadRequest(Messages(baseException.failure.message))
        case e: Exception => fileResourceManager.deleteFile(newPath, name)
          BadRequest(Messages(e.getMessage))
      }
  }

  def serveFileToGenesis(fileName: String, documentType: String): Action[AnyContent] = withGenesisLoginAction.authenticated { username =>
    implicit request =>
      try {
        documentType match {
          case constants.File.BANK_DETAILS => Ok.sendFile(new java.io.File(uploadZoneKycBankDetailsPath + fileName))
          case constants.File.IDENTIFICATION => Ok.sendFile(new java.io.File(uploadZoneKycIdentificationPath + fileName))
          case _ => BadRequest
        }
      } catch {
        case _: NoSuchFileException => Ok(views.html.index(failures = Seq(constants.Response.NO_SUCH_FILE_EXCEPTION)))
        case _: Exception => Ok(views.html.index(failures = Seq(constants.Response.GENERIC_EXCEPTION)))
      }
  }

  def serveFileToZone(accountID: String, fileName: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { username =>
    implicit request =>
      try {
        if (blockchainACLs.Service.get(masterAccounts.Service.getAddress(accountID)).zoneID == masterZones.Service.getAccountId(username)) {
          documentType match {
            case constants.File.BANK_DETAILS => Ok.sendFile(new java.io.File(uploadZoneKycBankDetailsPath + fileName))
            case constants.File.IDENTIFICATION => Ok.sendFile(new java.io.File(uploadZoneKycIdentificationPath + fileName))
            case _ => BadRequest
          }
        } else {
          Ok(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case _: NoSuchFileException => Ok(views.html.index(failures = Seq(constants.Response.NO_SUCH_FILE_EXCEPTION)))
        case _: Exception => Ok(views.html.index(failures = Seq(constants.Response.GENERIC_EXCEPTION)))
      }
  }

}
