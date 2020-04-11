package controllers

import java.nio.file.Files
import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject._
import models.common.Serializable
import models.master.{OrganizationBackgroundCheck, Trader}
import models.masterTransaction.AssetFile
import models.{blockchain, master, masterTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import views.companion.master.FileUpload

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BackgroundCheckController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, masterTraderBackgroundChecks: master.TraderBackgroundChecks, masterOrganizationBackgroundChecks: master.OrganizationBackgroundChecks, masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles, masterTransactionIssueAssetRequests: masterTransaction.IssueAssetRequests, masterZones: master.Zones, masterOrganizations: master.Organizations, masterTraders: master.Traders, fileResourceManager: utilities.FileResourceManager, withZoneLoginAction: WithZoneLoginAction, withUserLoginAction: WithUserLoginAction, withTraderLoginAction: WithTraderLoginAction, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration, wsClient: WSClient) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_BACKGROUND_CHECK

  def uploadTraderBackgroundCheckFileForm(documentType: String, traderID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.BackgroundCheckController.uploadTraderBackgroundCheckFile), utilities.String.getJsRouteFunction(routes.javascript.BackgroundCheckController.storeTraderBackgroundCheckFile), documentType, traderID))
  }

  def updateTraderBackgroundCheckFileForm(documentType: String, traderID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.BackgroundCheckController.uploadTraderBackgroundCheckFile), utilities.String.getJsRouteFunction(routes.javascript.BackgroundCheckController.updateTraderBackgroundCheckFile), documentType, traderID))
  }

  def uploadTraderBackgroundCheckFile(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },

      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.profile(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getBackgroundCheckFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeTraderBackgroundCheckFile(name: String, documentType: String, traderID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val storeFile = fileResourceManager.storeFile[master.TraderBackgroundCheck](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getBackgroundCheckFilePath(documentType),
        document = master.TraderBackgroundCheck(id = traderID, documentType = documentType, fileName = name, file = None, status = Option(true)),
        masterCreate = masterTraderBackgroundChecks.Service.create
      )

      def allDocuments = masterTraderBackgroundChecks.Service.getAllDocuments(traderID)

      val trader = masterTraders.Service.tryGet(traderID)

      (for {
        _ <- storeFile
        allDocuments <- allDocuments
        trader <- trader
        result <- withUsernameToken.PartialContent(views.html.component.master.zoneUploadOrUpdateTraderBackgroundCheck(allDocuments, trader))
      } yield {
        result
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def updateTraderBackgroundCheckFile(name: String, documentType: String, traderID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val getOldDocumentFileName = masterTraderBackgroundChecks.Service.getFileName(id = traderID, documentType = documentType)

      def updateFile(oldDocumentFileName: String): Future[Boolean] = fileResourceManager.updateFile[master.TraderBackgroundCheck](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getBackgroundCheckFilePath(documentType),
        oldDocumentFileName = oldDocumentFileName,
        document = master.TraderBackgroundCheck(id = traderID, documentType = documentType, fileName = name, file = None, status = Option(true)),
        updateOldDocument = masterTraderBackgroundChecks.Service.updateOldDocument
      )

      def allDocuments = masterTraderBackgroundChecks.Service.getAllDocuments(traderID)

      val trader = masterTraders.Service.tryGet(traderID)

      (for {
        oldDocumentFileName <- getOldDocumentFileName
        _ <- updateFile(oldDocumentFileName)
        allDocuments <- allDocuments
        trader <- trader
        result <- withUsernameToken.PartialContent(views.html.component.master.zoneUploadOrUpdateTraderBackgroundCheck(allDocuments, trader))
      } yield {
        result
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def uploadOrUpdateTraderBackgroundCheckFile(traderID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val trader = masterTraders.Service.tryGet(traderID)

      def allDocuments = masterTraderBackgroundChecks.Service.getAllDocuments(traderID)
      (for {
        zoneID <- zoneID
        trader <- trader
        allDocuments <- allDocuments
      } yield {
        if (zoneID == trader.zoneID) {
          Ok(views.html.component.master.zoneUploadOrUpdateTraderBackgroundCheck(allDocuments, trader))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def uploadOrganizationBackgroundCheckFileForm(documentType: String, organizationID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.BackgroundCheckController.uploadOrganizationBackgroundCheckFile), utilities.String.getJsRouteFunction(routes.javascript.BackgroundCheckController.storeOrganizationBackgroundCheckFile), documentType, organizationID))
  }

  def updateOrganizationBackgroundCheckFileForm(documentType: String, organizationID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.BackgroundCheckController.uploadOrganizationBackgroundCheckFile), utilities.String.getJsRouteFunction(routes.javascript.BackgroundCheckController.updateOrganizationBackgroundCheckFile), documentType, organizationID))
  }

  def uploadOrganizationBackgroundCheckFile(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },

      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.profile(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getBackgroundCheckFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeOrganizationBackgroundCheckFile(name: String, documentType: String, organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val storeFile = fileResourceManager.storeFile[OrganizationBackgroundCheck](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getBackgroundCheckFilePath(documentType),
        document = OrganizationBackgroundCheck(id = organizationID, documentType = documentType, fileName = name, file = None, status = Option(true)),
        masterCreate = masterOrganizationBackgroundChecks.Service.create
      )

      def allDocuments = masterOrganizationBackgroundChecks.Service.getAllDocuments(organizationID)

      val organization = masterOrganizations.Service.tryGet(organizationID)

      (for {
        _ <- storeFile
        allDocuments <- allDocuments
        organization <- organization
        result <- withUsernameToken.PartialContent(views.html.component.master.zoneUploadOrUpdateOrganizationBackgroundCheck(allDocuments, organization))
      } yield {
        result
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def updateOrganizationBackgroundCheckFile(name: String, documentType: String, organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val getOldDocumentFileName = masterOrganizationBackgroundChecks.Service.getFileName(id = organizationID, documentType = documentType)

      def updateFile(oldDocumentFileName: String): Future[Boolean] = fileResourceManager.updateFile[master.OrganizationBackgroundCheck](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getBackgroundCheckFilePath(documentType),
        oldDocumentFileName = oldDocumentFileName,
        document = master.OrganizationBackgroundCheck(id = organizationID, documentType = documentType, fileName = name, file = None, status = Option(true)),
        updateOldDocument = masterOrganizationBackgroundChecks.Service.updateOldDocument
      )

      def allDocuments = masterOrganizationBackgroundChecks.Service.getAllDocuments(organizationID)

      val organization = masterOrganizations.Service.tryGet(organizationID)

      (for {
        oldDocumentFileName <- getOldDocumentFileName
        _ <- updateFile(oldDocumentFileName)
        allDocuments <- allDocuments
        organization <- organization
        result <- withUsernameToken.PartialContent(views.html.component.master.zoneUploadOrUpdateOrganizationBackgroundCheck(allDocuments, organization))
      } yield {
        result
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def uploadOrUpdateOrganizationBackgroundCheckFile(organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val organization = masterOrganizations.Service.tryGet(organizationID)
      val allDocuments = masterOrganizationBackgroundChecks.Service.getAllDocuments(organizationID)
      (for {
        allDocuments <- allDocuments
        zoneID <- zoneID
        organization <- organization
        result <- withUsernameToken.Ok(views.html.component.master.zoneUploadOrUpdateOrganizationBackgroundCheck(allDocuments, organization))
      } yield {
        if (zoneID == organization.zoneID) {
          result
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneAccessedOrganizationBackgroundCheckFile(organizationID: String, fileName: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationZoneID = masterOrganizations.Service.tryGetZoneID(organizationID)
      val userZoneID = masterZones.Service.tryGetID(loginState.username)
      (for {
        organizationZoneID <- organizationZoneID
        userZoneID <- userZoneID
      } yield {
        if (organizationZoneID == userZoneID) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getBackgroundCheckFilePath(documentType), fileName = fileName))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneAccessedTraderBackgroundCheckFile(traderID: String, fileName: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderZoneID = masterTraders.Service.tryGetZoneID(traderID)
      val userZoneID = masterZones.Service.tryGetID(loginState.username)
      (for {
        traderZoneID <- traderZoneID
        userZoneID <- userZoneID
      } yield {
        if (traderZoneID == userZoneID) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getBackgroundCheckFilePath(documentType), fileName = fileName))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }
}
