package controllers

import java.nio.file.Files

import controllers.actions.WithUserLoginAction
import exceptions.BaseException
import javax.inject._
import models.master
import org.apache.commons.codec.binary.Base64
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import utilities.FileResourceManager
import views.companion.master.FileUpload

import scala.concurrent.ExecutionContext

@Singleton
class FileUploadController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccountKYCs: master.AccountKYCs, withUserLoginAction: WithUserLoginAction)(implicit exec: ExecutionContext, configuration: Configuration, wsClient: WSClient) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val uploadImagesPath = configuration.get[String]("upload.accountKYCsImagesPath")

  private val uploadPDFsPath = configuration.get[String]("upload.accountKYCsPDFsPath")

  private val uploadDOCsPath = configuration.get[String]("upload.accountKYCsDOCsPath")

  private implicit val logger: Logger = Logger(this.getClass)

  def uploadForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.fileUpload())
  }

  def uploadImages = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errors.mkString("\n"))
      },
      fileUploadInfo => {
        try {
          request.body.file("file") match {
            case None => BadRequest("No file")
            case Some(file) =>
              utilities.FileUploader.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, uploadImagesPath)
              Ok
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def uploadPDFs = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errors.mkString("\n"))
      },
      fileUploadInfo => {
        try {
          request.body.file("file") match {
            case None => BadRequest("No file")
            case Some(file) =>
              utilities.FileUploader.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, uploadPDFsPath)
              Ok
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def uploadDOCs = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errors.mkString("\n"))
      },
      fileUploadInfo => {
        try {
          request.body.file("file") match {
            case None => BadRequest("No file")
            case Some(file) =>
              utilities.FileUploader.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, uploadDOCsPath)
              Ok
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeImage(name: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit username =>
    implicit request =>
      try {
        val (fileName, encodedBase64) = utilities.ImageUploadProcess.convertToThumbnail(name, uploadImagesPath)
        masterAccountKYCs.Service.create(id = username, documentType = constants.File.IMAGES, fileName = fileName, file = Option(encodedBase64))
        utilities.FileResourceManager.renameFile(uploadImagesPath, name, fileName)
        Ok(Messages(constants.Response.UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => utilities.FileResourceManager.deleteFile(uploadPDFsPath, name)
          BadRequest(Messages(baseException.failure.message))
        case e: Exception => utilities.FileResourceManager.deleteFile(uploadPDFsPath, name)
          BadRequest(Messages(e.getMessage))
      }
  }

  def updateImage(name: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit username =>
    implicit request =>
      try {
        val (fileName, encodedBase64) = utilities.ImageUploadProcess.convertToThumbnail(name, uploadImagesPath)
        val oldAccountKYC = masterAccountKYCs.Service.get(id = username, documentType = constants.File.IMAGES)
        utilities.FileResourceManager.deleteFile(uploadImagesPath, oldAccountKYC.fileName)
        masterAccountKYCs.Service.updateOldDocument(id = username, documentType = constants.File.IMAGES, fileName = fileName, file = Option(encodedBase64))
        utilities.FileResourceManager.renameFile(uploadImagesPath, name, fileName)
        Ok(Messages(constants.Response.UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => utilities.FileResourceManager.deleteFile(uploadPDFsPath, name)
          BadRequest(Messages(baseException.failure.message))
        case e: Exception => utilities.FileResourceManager.deleteFile(uploadPDFsPath, name)
          BadRequest(Messages(e.getMessage))
      }
  }

  def storePDF(name: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit username =>
    implicit request =>
      try {
        val fileName = List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(utilities.FileResourceManager.convertToByteArray(utilities.FileResourceManager.newFile(uploadPDFsPath, name)))).toString, FileResourceManager.fileExtensionFromName(name)).mkString(".")
        masterAccountKYCs.Service.create(id = username, documentType = constants.File.PDFS, fileName = fileName, file = null)
        utilities.FileResourceManager.renameFile(uploadPDFsPath, name, fileName)
        Ok(Messages(constants.Response.UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => utilities.FileResourceManager.deleteFile(uploadPDFsPath, name)
          BadRequest(Messages(baseException.failure.message))
        case e: Exception => utilities.FileResourceManager.deleteFile(uploadPDFsPath, name)
          BadRequest(Messages(e.getMessage))
      }
  }

  def updatePDF(name: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit username =>
    implicit request =>
      try {
        val fileName = List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(utilities.FileResourceManager.convertToByteArray(utilities.FileResourceManager.newFile(uploadPDFsPath, name)))).toString, FileResourceManager.fileExtensionFromName(name)).mkString(".")
        val oldAccountKYC = masterAccountKYCs.Service.get(id = username, documentType = constants.File.PDFS)
        utilities.FileResourceManager.deleteFile(uploadPDFsPath, oldAccountKYC.fileName)
        masterAccountKYCs.Service.updateOldDocument(id = username, documentType = constants.File.PDFS, fileName = fileName, file = null)
        utilities.FileResourceManager.renameFile(uploadPDFsPath, name, fileName)
        Ok(Messages(constants.Response.UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => utilities.FileResourceManager.deleteFile(uploadPDFsPath, name)
          BadRequest(Messages(baseException.failure.message))
        case e: Exception => utilities.FileResourceManager.deleteFile(uploadPDFsPath, name)
          BadRequest(Messages(e.getMessage))
      }
  }

  def storeDOC(name: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit username =>
    implicit request =>
      try {
        val fileName = List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(utilities.FileResourceManager.convertToByteArray(utilities.FileResourceManager.newFile(uploadDOCsPath, name)))).toString, FileResourceManager.fileExtensionFromName(name)).mkString(".")
        masterAccountKYCs.Service.create(id = username, documentType = constants.File.DOCS, fileName = fileName, file = null)
        utilities.FileResourceManager.renameFile(uploadDOCsPath, name, fileName)
        Ok(Messages(constants.Response.UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => utilities.FileResourceManager.deleteFile(uploadPDFsPath, name)
          BadRequest(Messages(baseException.failure.message))
        case e: Exception => utilities.FileResourceManager.deleteFile(uploadPDFsPath, name)
          BadRequest(Messages(e.getMessage))
      }
  }

  def updateDOC(name: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit username =>
    implicit request =>
      try {
        val fileName = List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(utilities.FileResourceManager.convertToByteArray(utilities.FileResourceManager.newFile(uploadDOCsPath, name)))).toString, FileResourceManager.fileExtensionFromName(name)).mkString(".")
        val oldAccountKYC = masterAccountKYCs.Service.get(id = username, documentType = constants.File.DOCS)
        utilities.FileResourceManager.deleteFile(uploadDOCsPath, oldAccountKYC.fileName)
        masterAccountKYCs.Service.updateOldDocument(id = username, documentType = constants.File.DOCS, fileName = fileName, file = null)
        utilities.FileResourceManager.renameFile(uploadDOCsPath, name, fileName)
        Ok(Messages(constants.Response.UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => utilities.FileResourceManager.deleteFile(uploadPDFsPath, name)
          BadRequest(Messages(baseException.failure.message))
        case e: Exception => utilities.FileResourceManager.deleteFile(uploadPDFsPath, name)
          BadRequest(Messages(e.getMessage))
      }
  }
}
