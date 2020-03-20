package controllers

import java.nio.file.Files
import java.util.Date

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject._
import models.common.Serializable
import models.master.{Trader}
import models.masterTransaction.AssetFile
import models.{blockchain, master, masterTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import views.companion.master.FileUpload

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WorldCheckController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, masterTraderWorldChecks: master.TraderWorldChecks, masterOrganizationWorldChecks: master.OrganizationWorldChecks, masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles, masterTransactionIssueAssetRequests: masterTransaction.IssueAssetRequests, masterTransactionNegotiationRequests: masterTransaction.NegotiationRequests, masterZones: master.Zones, masterOrganizations: master.Organizations, masterTraders: master.Traders, fileResourceManager: utilities.FileResourceManager, withZoneLoginAction: WithZoneLoginAction, withUserLoginAction: WithUserLoginAction, withTraderLoginAction: WithTraderLoginAction, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration, wsClient: WSClient) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_WORLD_CHECK

  def uploadTraderWorldCheckFileForm(documentType: String, traderID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.WorldCheckController.uploadTraderWorldCheckFile), utilities.String.getJsRouteFunction(routes.javascript.WorldCheckController.storeTraderWorldCheckFile), documentType, traderID))
  }

  def updateTraderWorldCheckFileForm(documentType: String, traderID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.WorldCheckController.uploadTraderWorldCheckFile), utilities.String.getJsRouteFunction(routes.javascript.WorldCheckController.updateTraderWorldCheckFile), documentType, traderID))
  }

  def uploadTraderWorldCheckFile(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },

      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.profile(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getWorldCheckFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeTraderWorldCheckFile(name: String, documentType: String, traderID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val storeFile = fileResourceManager.storeFile[master.TraderWorldCheck](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getWorldCheckFilePath(documentType),
        document = master.TraderWorldCheck(id = traderID, documentType = documentType, fileName = name, file = None, status = Option(true)),
        masterCreate = masterTraderWorldChecks.Service.create
      )

      def allDocuments = masterTraderWorldChecks.Service.getAllDocuments(traderID)
      val trader = masterTraders.Service.get(traderID)

      (for {
        _ <- storeFile
        allDocuments <- allDocuments
        trader <- trader
        result <- withUsernameToken.PartialContent(views.html.component.master.zoneUploadOrUpdateTraderWorldCheck(allDocuments, trader))
      } yield {
        result
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def updateTraderWorldCheckFile(name: String, documentType: String, traderID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val getOldDocumentFileName = masterTraderWorldChecks.Service.getFileName(id = traderID, documentType = documentType)

      def updateFile(oldDocumentFileName: String): Future[Boolean] = fileResourceManager.updateFile[master.TraderWorldCheck](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getWorldCheckFilePath(documentType),
        oldDocumentFileName = oldDocumentFileName,
        document = master.TraderWorldCheck(id = traderID, documentType = documentType, fileName = name, file = None, status = Option(true)),
        updateOldDocument = masterTraderWorldChecks.Service.updateOldDocument
      )

      def allDocuments = masterTraderWorldChecks.Service.getAllDocuments(traderID)

      val trader = masterTraders.Service.get(traderID)

      (for {
        oldDocumentFileName <- getOldDocumentFileName
        _ <- updateFile(oldDocumentFileName)
        allDocuments <- allDocuments
        trader <- trader
        result <- withUsernameToken.PartialContent(views.html.component.master.zoneUploadOrUpdateTraderWorldCheck(allDocuments, trader))
      } yield {
        result
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def uploadOrUpdateTraderWorldCheckFile(traderID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.getID(loginState.username)
      val trader = masterTraders.Service.get(traderID)
      val allDocuments = masterTraderWorldChecks.Service.getAllDocuments(traderID)
      (for {
        allDocuments <- allDocuments
        zoneID <- zoneID
        trader <- trader
        result <- withUsernameToken.Ok(views.html.component.master.zoneUploadOrUpdateTraderWorldCheck(allDocuments, trader))
      } yield {
        if (zoneID == trader.zoneID) {
          result
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }
/////////////////////////////////////////////////
def uploadOrganizationWorldCheckFileForm(documentType: String, organizationID: String): Action[AnyContent] = Action { implicit request =>
  Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.WorldCheckController.uploadOrganizationWorldCheckFile), utilities.String.getJsRouteFunction(routes.javascript.WorldCheckController.storeOrganizationWorldCheckFile), documentType, organizationID))
}

  def updateOrganizationWorldCheckFileForm(documentType: String, organizationID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.WorldCheckController.uploadOrganizationWorldCheckFile), utilities.String.getJsRouteFunction(routes.javascript.WorldCheckController.updateOrganizationWorldCheckFile), documentType, organizationID))
  }

  def uploadOrganizationWorldCheckFile(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },

      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.profile(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getWorldCheckFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeOrganizationWorldCheckFile(name: String, documentType: String, organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val storeFile = fileResourceManager.storeFile[master.OrganizationWorldCheck](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getWorldCheckFilePath(documentType),
        document = master.OrganizationWorldCheck(id = organizationID, documentType = documentType, fileName = name, file = None, status = Option(true)),
        masterCreate = masterOrganizationWorldChecks.Service.create
      )

      def allDocuments = masterOrganizationWorldChecks.Service.getAllDocuments(organizationID)
      val organization = masterOrganizations.Service.get(organizationID)

      (for {
        _ <- storeFile
        allDocuments <- allDocuments
        organization <- organization
        result <- withUsernameToken.PartialContent(views.html.component.master.zoneUploadOrUpdateOrganizationWorldCheck(allDocuments, organization))
      } yield {
        result
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def updateOrganizationWorldCheckFile(name: String, documentType: String, organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val getOldDocumentFileName = masterOrganizationWorldChecks.Service.getFileName(id = organizationID, documentType = documentType)

      def updateFile(oldDocumentFileName: String): Future[Boolean] = fileResourceManager.updateFile[master.OrganizationWorldCheck](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getWorldCheckFilePath(documentType),
        oldDocumentFileName = oldDocumentFileName,
        document = master.OrganizationWorldCheck(id = organizationID, documentType = documentType, fileName = name, file = None, status = Option(true)),
        updateOldDocument = masterOrganizationWorldChecks.Service.updateOldDocument
      )

      def allDocuments = masterOrganizationWorldChecks.Service.getAllDocuments(organizationID)

      val organization = masterOrganizations.Service.get(organizationID)

      (for {
        oldDocumentFileName <- getOldDocumentFileName
        _ <- updateFile(oldDocumentFileName)
        allDocuments <- allDocuments
        organization <- organization
        result <- withUsernameToken.PartialContent(views.html.component.master.zoneUploadOrUpdateOrganizationWorldCheck(allDocuments, organization))
      } yield {
        result
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def uploadOrUpdateOrganizationWorldCheckFile(organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.getID(loginState.username)
      val organization = masterOrganizations.Service.get(organizationID)
      val allDocuments = masterOrganizationWorldChecks.Service.getAllDocuments(organizationID)
      (for {
        allDocuments <- allDocuments
        zoneID <- zoneID
        organization <- organization
        result <- withUsernameToken.Ok(views.html.component.master.zoneUploadOrUpdateOrganizationWorldCheck(allDocuments, organization))
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

  /////////////////////////////////////////////////
  def zoneAccessedOrganizationWorldCheckFile(organizationID: String, fileName: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationZoneID = masterOrganizations.Service.getZoneID(organizationID)
      val userZoneID = masterZones.Service.getID(loginState.username)
      (for {
        organizationZoneID <- organizationZoneID
        userZoneID <- userZoneID
      } yield {
        if (organizationZoneID == userZoneID) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getWorldCheckFilePath(documentType), fileName = fileName))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneAccessedTraderWorldCheckFile(traderID: String, fileName: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderZoneID = masterTraders.Service.getZoneID(traderID)
      val userZoneID = masterZones.Service.getID(loginState.username)
      (for {
        traderZoneID <- traderZoneID
        userZoneID <- userZoneID
      } yield {
        if (traderZoneID == userZoneID) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getWorldCheckFilePath(documentType), fileName = fileName))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }
}
