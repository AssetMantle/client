package controllers

import java.nio.file.Files

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject._
import models.master.{AccountFile, AccountKYC}
import models.{blockchain, master, masterTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import views.companion.master.FileUpload

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FileController @Inject()(
                                messagesControllerComponents: MessagesControllerComponents,
                                fileResourceManager: utilities.FileResourceManager,
                                masterAccountFiles: master.AccountFiles,
                                masterAccountKYCs: master.AccountKYCs,
                                withLoginAction: WithLoginAction,
                                withUsernameToken: WithUsernameToken,
                                withoutLoginAction: WithoutLoginAction,
                                withoutLoginActionAsync: WithoutLoginActionAsync,
                              )(implicit executionContext: ExecutionContext, configuration: Configuration, wsClient: WSClient) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.FILE_CONTROLLER

  def uploadAccountKYCForm(documentType: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadAccountKYC), utilities.String.getJsRouteFunction(routes.javascript.FileController.storeAccountKYC), documentType))
  }

  def updateAccountKYCForm(documentType: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
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
            case None => BadRequest(views.html.profile(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getAccountKYCFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeAccountKYC(name: String, documentType: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val storeFile = fileResourceManager.storeFile[AccountKYC](
        name = name,
        path = fileResourceManager.getAccountKYCFilePath(documentType),
        document = AccountKYC(id = loginState.username, documentType = documentType, status = None, fileName = name, file = None),
        masterCreate = masterAccountKYCs.Service.create
      )

      def accountKYC = masterAccountKYCs.Service.get(loginState.username, documentType)

      def getResult(accountKYC: Option[AccountKYC]) = documentType match {
        case constants.File.AccountKYC.IDENTIFICATION => withUsernameToken.PartialContent(views.html.component.master.userViewUploadOrUpdateIdentification(accountKYC, documentType))
        case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
      }

      (for {
        _ <- storeFile
        accountKYC <- accountKYC
        result <- getResult(accountKYC)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def updateAccountKYC(name: String, documentType: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val oldDocument = masterAccountKYCs.Service.tryGet(id = loginState.username, documentType = documentType)

      def updateFile(oldDocument: AccountKYC): Future[Boolean] = fileResourceManager.updateFile[AccountKYC](
        name = name,
        path = fileResourceManager.getAccountKYCFilePath(documentType),
        oldDocument = oldDocument,
        updateOldDocument = masterAccountKYCs.Service.updateOldDocument
      )

      def accountKYC = masterAccountKYCs.Service.get(loginState.username, documentType)

      def getResult(accountKYC: Option[AccountKYC]) = documentType match {
        case constants.File.AccountKYC.IDENTIFICATION => withUsernameToken.PartialContent(views.html.component.master.userViewUploadOrUpdateIdentification(accountKYC, documentType))
        case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
      }

      (for {
        oldDocument <- oldDocument
        _ <- updateFile(oldDocument)
        accountKYC <- accountKYC
        result <- getResult(accountKYC)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def getAccountKYCFile(fileName: String, documentType: String) = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val checkFileNameExists = masterAccountKYCs.Service.checkFileNameExists(id = loginState.username, fileName = fileName)

      (for {
        checkFileNameExists <- checkFileNameExists
      } yield if (checkFileNameExists) Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getAccountKYCFilePath(documentType), fileName = fileName)) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def uploadAccountFileForm(documentType: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadAccountFile), utilities.String.getJsRouteFunction(routes.javascript.FileController.storeAccountFile), documentType))
  }

  def updateAccountFileForm(documentType: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
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
      val storeFile = fileResourceManager.storeFile[AccountFile](
        name = name,
        path = fileResourceManager.getAccountFilePath(documentType),
        document = AccountFile(id = loginState.username, documentType = documentType, fileName = name, file = None),
        masterCreate = masterAccountFiles.Service.create
      )
      (for {
        _ <- storeFile
        result <- withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateAccountFile(name: String, documentType: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val oldDocument = masterAccountFiles.Service.tryGet(id = loginState.username, documentType = documentType)

      def updateFile(oldDocument: AccountFile): Future[Boolean] = fileResourceManager.updateFile[AccountFile](
        name = name,
        path = fileResourceManager.getAccountFilePath(documentType),
        oldDocument = oldDocument,
        updateOldDocument = masterAccountFiles.Service.updateOldDocument
      )

      (for {
        oldDocument <- oldDocument
        _ <- updateFile(oldDocument)
        result <- withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def file(fileName: String, documentType: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val path: Future[String] = loginState.userType match {
        case constants.User.USER =>
          val checkFileNameExistsAccountKYCs = masterAccountKYCs.Service.checkFileNameExists(id = loginState.username, fileName = fileName)
          for {
            checkFileNameExistsAccountKYCs <- checkFileNameExistsAccountKYCs
          } yield if (checkFileNameExistsAccountKYCs) fileResourceManager.getAccountKYCFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
        case _ =>
          val checkFileNameExistsAccountFiles = masterAccountFiles.Service.checkFileNameExists(id = loginState.username, fileName = fileName)
          for {
            checkFileNameExistsAccountFiles <- checkFileNameExistsAccountFiles
          } yield if (checkFileNameExistsAccountFiles) fileResourceManager.getAccountFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
      }
      (for {
        path <- path
      } yield Ok.sendFile(utilities.FileOperations.fetchFile(path = path, fileName = fileName))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }
}
