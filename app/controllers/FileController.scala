package controllers

import java.nio.file.Files
import java.util.Date

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject._
import models.common.Serializable
import models.master.{AccountKYC, Asset, Negotiations, Trader}
import models.masterTransaction.AssetFile
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
                                withLoginAction: WithLoginAction,
                                masterAccountFiles: master.AccountFiles,
                                masterTransactionAssetFiles: masterTransaction.AssetFiles,
                                masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                masterNegotiations: Negotiations,
                                masterZones: master.Zones,
                                masterOrganizations: master.Organizations,
                                masterTraders: master.Traders,
                                masterAccountKYCs: master.AccountKYCs,
                                fileResourceManager: utilities.FileResourceManager,
                                withGenesisLoginAction: WithGenesisLoginAction,
                                withUserLoginAction: WithUserLoginAction,
                                masterZoneKYCs: master.ZoneKYCs,
                                withZoneLoginAction: WithZoneLoginAction,
                                masterOrganizationKYCs: master.OrganizationKYCs,
                                withOrganizationLoginAction: WithOrganizationLoginAction,
                                masterTraderKYCs: master.TraderKYCs,
                                withTraderLoginAction: WithTraderLoginAction,
                                masterAssets: master.Assets,
                                withUsernameToken: WithUsernameToken,
                              )(implicit executionContext: ExecutionContext, configuration: Configuration, wsClient: WSClient) extends AbstractController(messagesControllerComponents) with I18nSupport {

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
      val storeFile = fileResourceManager.storeFile[master.AccountKYC](
        name = name,
        path = fileResourceManager.getAccountKYCFilePath(documentType),
        document = master.AccountKYC(id = loginState.username, documentType = documentType, status = None, fileName = name, file = None),
        masterCreate = masterAccountKYCs.Service.create
      )

      def accountKYC = masterAccountKYCs.Service.get(loginState.username, documentType)

      def getResult(accountKYC: Option[AccountKYC]) = documentType match {
        case constants.File.IDENTIFICATION => withUsernameToken.PartialContent(views.html.component.master.userViewUploadOrUpdateIdentification(accountKYC, documentType))
        case constants.File.BANK_ACCOUNT_DETAIL => withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
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
      val oldDocumentFileName = masterAccountKYCs.Service.getFileName(id = loginState.username, documentType = documentType)

      def updateFile(oldDocumentFileName: String): Future[Boolean] = fileResourceManager.updateFile[master.AccountKYC](
        name = name,
        path = fileResourceManager.getAccountKYCFilePath(documentType),
        oldDocumentFileName = oldDocumentFileName,
        document = master.AccountKYC(id = loginState.username, documentType = documentType, status = None, fileName = name, file = None),
        updateOldDocument = masterAccountKYCs.Service.updateOldDocument
      )

      def accountKYC = masterAccountKYCs.Service.get(loginState.username, documentType)

      def getResult(accountKYC: Option[AccountKYC]) = documentType match {
        case constants.File.IDENTIFICATION => withUsernameToken.PartialContent(views.html.component.master.userViewUploadOrUpdateIdentification(accountKYC, documentType))
        case constants.File.BANK_ACCOUNT_DETAIL => withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
      }

      (for {
        oldDocumentFileName <- oldDocumentFileName
        _ <- updateFile(oldDocumentFileName)
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

  //TODO Shall we verify for genesis
  def genesisAccessedFile(fileName: String, documentType: String): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future {
        Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getZoneKYCFilePath(documentType), fileName = fileName))
      }.recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneAccessedOrganizationKYCFile(organizationID: String, fileName: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationZoneID = masterOrganizations.Service.tryGetZoneID(organizationID)
      val userZoneID = masterZones.Service.tryGetID(loginState.username)
      (for {
        organizationZoneID <- organizationZoneID
        userZoneID <- userZoneID
      } yield {
        if (organizationZoneID == userZoneID) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getOrganizationKYCFilePath(documentType), fileName = fileName))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneAccessedTraderKYCFile(traderID: String, fileName: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderZoneID = masterTraders.Service.tryGetZoneID(traderID)
      val userZoneID = masterZones.Service.tryGetID(loginState.username)
      (for {
        traderZoneID <- traderZoneID
        userZoneID <- userZoneID
      } yield {
        if (traderZoneID == userZoneID) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getTraderKYCFilePath(documentType), fileName = fileName))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationAccessedTraderKYCFile(traderID: String, fileName: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderOrganizationID = masterTraders.Service.getOrganizationIDByAccountID(loginState.username)
      val userOrganizationID = masterOrganizations.Service.tryGetID(loginState.username)
      (for {
        traderOrganizationID <- traderOrganizationID
        userOrganizationID <- userOrganizationID
      } yield {
        if (traderOrganizationID == userOrganizationID) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getTraderKYCFilePath(documentType), fileName = fileName))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  //TODO Shall we check if exists?
  def userAccessedZoneKYCFile(documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val id = masterZones.Service.tryGetID(loginState.username)

      def fileName(id: String): Future[String] = masterZoneKYCs.Service.getFileName(id = id, documentType = documentType)

      (for {
        id <- id
        fileName <- fileName(id)
      } yield Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getZoneKYCFilePath(documentType), fileName = fileName))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  //TODO Shall we check if exists?
  def userAccessedOrganizationKYCFile(documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val id = masterOrganizations.Service.tryGetID(loginState.username)

      def fileName(id: String): Future[String] = masterOrganizationKYCs.Service.getFileName(id = id, documentType = documentType)

      (for {
        id <- id
        fileName <- fileName(id)
      } yield Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getOrganizationKYCFilePath(documentType), fileName = fileName))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  //TODO Shall we check if exists?
  def userAccessedTraderKYCFile(documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val id = masterTraders.Service.tryGetID(loginState.username)

      def fileName(id: String): Future[String] = masterTraderKYCs.Service.getFileName(id = id, documentType = documentType)

      (for {
        id <- id
        fileName <- fileName(id)
      } yield Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getTraderKYCFilePath(documentType), fileName = fileName))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneAccessedNegotiationFile(id: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def fileName: Future[String] = masterTransactionNegotiationFiles.Service.tryGetFileName(id = id, documentType = documentType)

      def getTraderZOneID(id: String): Future[String] = masterTraders.Service.tryGetZoneID(id)

      (for {
        zoneID <- zoneID
        negotiation <- negotiation
        fileName <- fileName
        sellerTraderZoneID <- getTraderZOneID(negotiation.sellerTraderID)
        buyerTraderZoneID <- getTraderZOneID(negotiation.buyerTraderID)
      } yield {
        if (sellerTraderZoneID == zoneID || buyerTraderZoneID == zoneID) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getZoneNegotiationFilePath(documentType), fileName = fileName))
        } else Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
      }).recover {
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
      val storeFile = fileResourceManager.storeFile[master.AccountFile](
        name = name,
        path = fileResourceManager.getAccountFilePath(documentType),
        document = master.AccountFile(id = loginState.username, documentType = documentType, fileName = name, file = None),
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
      val oldDocumentFileName = masterAccountFiles.Service.getFileName(id = loginState.username, documentType = documentType)

      def updateFile(oldDocumentFileName: String): Future[Boolean] = fileResourceManager.updateFile[master.AccountFile](
        name = name,
        path = fileResourceManager.getAccountFilePath(documentType),
        oldDocumentFileName = oldDocumentFileName,
        document = master.AccountFile(id = loginState.username, documentType = documentType, fileName = name, file = None),
        updateOldDocument = masterAccountFiles.Service.updateOldDocument
      )

      (for {
        oldDocumentFileName <- oldDocumentFileName
        _ <- updateFile(oldDocumentFileName)
        result <- withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def file(fileName: String, documentType: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val path: Future[String] = loginState.userType match {
        case constants.User.ZONE =>
          val zoneID = masterZones.Service.tryGetID(loginState.username)

          def checkFileNameExistsZoneKYCs(zoneID: String): Future[Boolean] = masterZoneKYCs.Service.checkFileNameExists(id = zoneID, fileName = fileName)

          for {
            zoneID <- zoneID
            checkFileNameExistsZoneKYCs <- checkFileNameExistsZoneKYCs(zoneID)
          } yield if (checkFileNameExistsZoneKYCs) fileResourceManager.getZoneKYCFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
        case constants.User.ORGANIZATION =>
          val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

          def checkFileNameExistsOrganizationKYCs(organizationID: String): Future[Boolean] = masterOrganizationKYCs.Service.checkFileNameExists(id = organizationID, fileName = fileName)

          for {
            organizationID <- organizationID
            checkFileNameExistsOrganizationKYCs <- checkFileNameExistsOrganizationKYCs(organizationID)
          } yield if (checkFileNameExistsOrganizationKYCs) fileResourceManager.getOrganizationKYCFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
        case constants.User.TRADER =>
          val traderID = masterTraders.Service.tryGetID(loginState.username)

          def checkFileNameExistsTraderKYCs(traderID: String): Future[Boolean] = masterTraderKYCs.Service.checkFileNameExists(id = traderID, fileName = fileName)

          for {
            traderID <- traderID
            checkFileNameExistsTraderKYCs <- checkFileNameExistsTraderKYCs(traderID)
          } yield if (checkFileNameExistsTraderKYCs) fileResourceManager.getTraderKYCFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
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
