package controllers

import java.nio.file.Files

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject._
import models.common.Serializable.UBO
import models.master.{OrganizationBackgroundCheck, TraderBackgroundCheck}
import models.{blockchain, master, masterTransaction, memberCheck}
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import queries.GetMemberCheckMemberScanResult
import transactions.MemberCheckMemberScan
import views.companion.master.FileUpload

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class BackgroundCheckController @Inject()(
                                           messagesControllerComponents: MessagesControllerComponents,
                                           masterTraderBackgroundChecks: master.TraderBackgroundChecks,
                                           masterOrganizationBackgroundChecks: master.OrganizationBackgroundChecks,
                                           masterZones: master.Zones,
                                           masterOrganizations: master.Organizations,
                                           masterTraders: master.Traders,
                                           memberCheckMemberScans: memberCheck.MemberScans,
                                           getMemberCheckMemberScanResult: GetMemberCheckMemberScanResult,
                                           postMemberCheckMemberScan: MemberCheckMemberScan,
                                           fileResourceManager: utilities.FileResourceManager,
                                           withZoneLoginAction: WithZoneLoginAction,
                                           withGenesisLoginAction: WithGenesisLoginAction,
                                           withUsernameToken: WithUsernameToken
                                         )(implicit executionContext: ExecutionContext, configuration: Configuration, wsClient: WSClient) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_BACKGROUND_CHECK

  def memberScanForm(firstName: String = "Abhishek", lastName: String = "Singh"): Action[AnyContent] = Action.async { implicit request =>
    val ubo = UBO("Abhishek", "Singh", 0.00, "Very Single", "Mr.")
    Future(Ok(views.html.component.master.memberCheckMemberScan(firstName = "Abhishek", lastName = "Singh")))
  }

  def memberScan: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.MemberCheckMemberScan.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.memberCheckMemberScan(formWithErrors, formWithErrors.data(constants.FormField.FIRST_NAME.name), formWithErrors.data(constants.FormField.LAST_NAME.name))))
        },
        memberCheckMemberScanData => {
          val scanID = memberCheckMemberScans.Service.getScanID(memberCheckMemberScanData.firstName, memberCheckMemberScanData.lastName)

          def memberCheckRequest = postMemberCheckMemberScan.Service.post(postMemberCheckMemberScan.Request(firstName = memberCheckMemberScanData.firstName, lastName = memberCheckMemberScanData.lastName))

          def getResult(scanID: Option[Int]) = {
            scanID match {
              case Some(id) => Future(Ok)
              case None =>
                def createMemberScan(scanID: Int) = memberCheckMemberScans.Service.create(memberCheckMemberScanData.firstName, memberCheckMemberScanData.lastName, scanID)

                for {
                  response <- memberCheckRequest
                  createMemberScan <- createMemberScan(response.scanId)
                  result <- withUsernameToken.Ok(views.html.component.master.memberCheckMemberScanResponse(response))
                } yield result
            }
          }

          (for {
            scanID <- scanID
            result <- getResult(scanID)
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
          }
        })
  }

  def memberCheck(id: String): Action[AnyContent] = Action.async { implicit request =>
    val a = getMemberCheckMemberScanResult.Service.get(id)
    println(a)
    for {
      a <- a
    } yield {
      Ok(views.html.component.master.memberCheckMemberScanResult(a))
    }
  }

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

      val storeFile = fileResourceManager.storeFile[TraderBackgroundCheck](
        name = name,
        path = fileResourceManager.getBackgroundCheckFilePath(documentType),
        document = TraderBackgroundCheck(id = traderID, documentType = documentType, fileName = name, file = None),
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

      val getOldDocument = masterTraderBackgroundChecks.Service.tryGet(id = traderID, documentType = documentType)

      def updateFile(oldDocument: TraderBackgroundCheck): Future[Boolean] = fileResourceManager.updateFile[TraderBackgroundCheck](
        name = name,
        path = fileResourceManager.getBackgroundCheckFilePath(documentType),
        oldDocument = oldDocument,
        updateOldDocument = masterTraderBackgroundChecks.Service.updateOldDocument
      )

      def allDocuments = masterTraderBackgroundChecks.Service.getAllDocuments(traderID)

      val trader = masterTraders.Service.tryGet(traderID)

      (for {
        oldDocument <- getOldDocument
        _ <- updateFile(oldDocument)
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
        path = fileResourceManager.getBackgroundCheckFilePath(documentType),
        document = OrganizationBackgroundCheck(id = organizationID, documentType = documentType, fileName = name, file = None),
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
      val oldDocument = masterOrganizationBackgroundChecks.Service.tryGet(id = organizationID, documentType = documentType)

      def updateFile(oldDocument: OrganizationBackgroundCheck): Future[Boolean] = fileResourceManager.updateFile[OrganizationBackgroundCheck](
        name = name,
        path = fileResourceManager.getBackgroundCheckFilePath(documentType),
        oldDocument = oldDocument,
        updateOldDocument = masterOrganizationBackgroundChecks.Service.updateOldDocument
      )

      def allDocuments = masterOrganizationBackgroundChecks.Service.getAllDocuments(organizationID)

      val organization = masterOrganizations.Service.tryGet(organizationID)

      (for {
        oldDocument <- oldDocument
        _ <- updateFile(oldDocument)
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
