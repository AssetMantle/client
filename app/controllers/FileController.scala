package controllers

import java.nio.file.Files
import java.util.Date

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject._
import models.common.Serializable
import models.master.{AccountKYC, Negotiation, Negotiations, Trader}
import models.masterTransaction.AssetFile
import models.{blockchain, master, masterTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import views.companion.master.FileUpload

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FileController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, masterAccountFiles: master.AccountFiles, masterTransactionAssetFiles: masterTransaction.AssetFiles, masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles, masterTransactionIssueAssetRequests: masterTransaction.IssueAssetRequests, masterNegotiations: Negotiations, blockchainACLs: blockchain.ACLAccounts, masterAccounts: master.Accounts, masterZones: master.Zones, masterOrganizations: master.Organizations, masterTraders: master.Traders, masterAccountKYCs: master.AccountKYCs, fileResourceManager: utilities.FileResourceManager, withGenesisLoginAction: WithGenesisLoginAction, withUserLoginAction: WithUserLoginAction, masterZoneKYCs: master.ZoneKYCs, withZoneLoginAction: WithZoneLoginAction, masterOrganizationKYCs: master.OrganizationKYCs, withOrganizationLoginAction: WithOrganizationLoginAction, masterTraderKYCs: master.TraderKYCs, withTraderLoginAction: WithTraderLoginAction, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration, wsClient: WSClient) extends AbstractController(messagesControllerComponents) with I18nSupport {

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
        documentType = documentType,
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
        documentType = documentType,
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

  def uploadTraderAssetForm(documentType: String, id: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadTraderAsset), utilities.String.getJsRouteFunction(routes.javascript.FileController.storeTraderAsset), documentType, id))
  }

  def updateTraderAssetForm(documentType: String, id: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadTraderAsset), utilities.String.getJsRouteFunction(routes.javascript.FileController.updateTraderAsset), documentType, id))
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

  def storeTraderAsset(name: String, documentType: String, negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def storeFile(assetID: String) = fileResourceManager.storeFile[masterTransaction.AssetFile](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getTraderAssetFilePath(documentType),
        document = masterTransaction.AssetFile(id = assetID, documentType = documentType, fileName = name, file = None, documentContent = None, status = None),
        masterCreate = masterTransactionAssetFiles.Service.create
      )

      def getResult(negotiation: Negotiation): Future[Result] = {
        documentType match {
          case constants.File.OBL | constants.File.COO | constants.File.COA =>
            val negotiationFiles = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)

            def assetFiles(assetID: String) = masterTransactionAssetFiles.Service.getAllDocuments(assetID)

            for {
              negotiationFiles <- negotiationFiles
              assetFiles <- assetFiles(negotiation.assetID)
              result <- withUsernameToken.PartialContent(views.html.component.master.tradeDocuments(negotiationID, negotiation, assetFiles, negotiationFiles))
            } yield result
          case _ => withUsernameToken.Ok(views.html.index())
        }
      }

      (for {
        negotiation <- negotiation
        _ <- storeFile(negotiation.assetID)
        result <- getResult(negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateTraderAsset(name: String, documentType: String, negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def oldDocumentFileName(assetID: String) = masterTransactionAssetFiles.Service.getFileName(id = assetID, documentType = documentType)

      def updateFile(oldDocumentFileName: String, assetID: String): Future[Boolean] = fileResourceManager.updateFile[masterTransaction.AssetFile](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getTraderAssetFilePath(documentType),
        oldDocumentFileName = oldDocumentFileName,
        document = masterTransaction.AssetFile(id = assetID, documentType = documentType, fileName = name, file = None, documentContent = None, status = None),
        updateOldDocument = masterTransactionAssetFiles.Service.insertOrUpdateOldDocument
      )

      def getResult(negotiation: Negotiation) = {
        documentType match {
          case constants.File.OBL | constants.File.COO | constants.File.COA =>
            val negotiationFiles = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)

            def assetFiles(assetID: String) = masterTransactionAssetFiles.Service.getAllDocuments(assetID)

            for {
              negotiationFiles <- negotiationFiles
              assetFiles <- assetFiles(negotiation.assetID)
              result <- withUsernameToken.PartialContent(views.html.component.master.tradeDocuments(negotiationID, negotiation, assetFiles, negotiationFiles))
            } yield result
          case _ => withUsernameToken.Ok(views.html.index())
        }
      }

      (for {
        negotiation <- negotiation
        oldDocumentFileName <- oldDocumentFileName(negotiation.assetID)
        _ <- updateFile(oldDocumentFileName, negotiation.assetID)
        result <- getResult(negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def uploadTraderNegotiationForm(documentType: String, negotiationID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadTraderNegotiation), utilities.String.getJsRouteFunction(routes.javascript.FileController.storeTraderNegotiation), documentType, negotiationID))
  }

  def updateTraderNegotiationForm(documentType: String, negotiationID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadTraderNegotiation), utilities.String.getJsRouteFunction(routes.javascript.FileController.updateTraderNegotiation), documentType, negotiationID))
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

  def storeTraderNegotiation(name: String, documentType: String, negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val storeFile = fileResourceManager.storeFile[masterTransaction.NegotiationFile](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getTraderNegotiationFilePath(documentType),
        document = masterTransaction.NegotiationFile(id = negotiationID, documentType = documentType, fileName = name, file = None, documentContent = None, status = None),
        masterCreate = masterTransactionNegotiationFiles.Service.create
      )

      def getResult: Future[Result] = {
        documentType match {
          case constants.File.INVOICE | constants.File.CONTRACT | constants.File.BILL_OF_EXCHANGE =>
            val negotiationFiles = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
            val negotiation = masterNegotiations.Service.tryGet(negotiationID)

            def assetFiles(assetID: String) = masterTransactionAssetFiles.Service.getAllDocuments(assetID)

            for {
              negotiationFiles <- negotiationFiles
              negotiation <- negotiation
              assetFiles <- assetFiles(negotiation.assetID)
              result <- withUsernameToken.PartialContent(views.html.component.master.tradeDocuments(negotiationID, negotiation, assetFiles, negotiationFiles))
            } yield result
          case _ => withUsernameToken.Ok(views.html.index())
        }
      }

      (for {
        _ <- storeFile
        result <- getResult
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateTraderNegotiation(name: String, documentType: String, negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val oldDocumentFileName = masterTransactionNegotiationFiles.Service.getFileName(id = negotiationID, documentType = documentType)

      def updateFile(oldDocumentFileName: String): Future[Boolean] = fileResourceManager.updateFile[masterTransaction.NegotiationFile](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getTraderNegotiationFilePath(documentType),
        oldDocumentFileName = oldDocumentFileName,
        document = masterTransaction.NegotiationFile(id = negotiationID, documentType = documentType, fileName = name, file = None, documentContent = None, status = None),
        updateOldDocument = masterTransactionNegotiationFiles.Service.insertOrUpdateOldDocument
      )

      def getResult: Future[Result] = {
        documentType match {
          case constants.File.INVOICE | constants.File.CONTRACT | constants.File.BILL_OF_EXCHANGE =>
            val negotiationFiles = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
            val negotiation = masterNegotiations.Service.tryGet(negotiationID)

            def assetFiles(assetID: String) = masterTransactionAssetFiles.Service.getAllDocuments(assetID)

            for {
              negotiationFiles <- negotiationFiles
              negotiation <- negotiation
              assetFiles <- assetFiles(negotiation.assetID)
              result <- withUsernameToken.PartialContent(views.html.component.master.tradeDocuments(negotiationID, negotiation, assetFiles, negotiationFiles))
            } yield result
          case _ => withUsernameToken.Ok(views.html.index())
        }
      }

      (for {
        oldDocumentFileName <- oldDocumentFileName
        _ <- updateFile(oldDocumentFileName)
        result <- getResult
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def uploadZoneNegotiationForm(documentType: String, negotiationRequestID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadZoneNegotiation), utilities.String.getJsRouteFunction(routes.javascript.FileController.storeZoneNegotiation), documentType, negotiationRequestID))
  }

  def updateZoneNegotiationForm(documentType: String, negotiationRequestID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadZoneNegotiation), utilities.String.getJsRouteFunction(routes.javascript.FileController.updateZoneNegotiation), documentType, negotiationRequestID))
  }

  def uploadZoneNegotiation(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(Messages(constants.Response.NO_FILE.message))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getZoneNegotiationFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeZoneNegotiation(name: String, documentType: String, negotiationRequestID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val storeFile = fileResourceManager.storeFile[masterTransaction.NegotiationFile](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getZoneNegotiationFilePath(documentType),
        document = masterTransaction.NegotiationFile(id = negotiationRequestID, documentType = documentType, fileName = name, file = None, documentContent = None, status = None),
        masterCreate = masterTransactionNegotiationFiles.Service.create
      )

      def getResult: Future[Result] = {
        documentType match {
          case constants.File.BUYER_CONTRACT =>
            val negotiationFiles = masterTransactionNegotiationFiles.Service.get(negotiationRequestID, constants.File.BUYER_CONTRACT)
            for {
              negotiationFiles <- negotiationFiles
              result <- withUsernameToken.PartialContent(views.html.component.master.confirmBuyerBidDocument(negotiationFiles, negotiationRequestID, constants.File.BUYER_CONTRACT))
            } yield result
          case constants.File.SELLER_CONTRACT =>
            val negotiationFiles = masterTransactionNegotiationFiles.Service.get(negotiationRequestID, constants.File.SELLER_CONTRACT)
            for {
              negotiationFiles <- negotiationFiles
              result <- withUsernameToken.PartialContent(views.html.component.master.confirmSellerBidDocument(negotiationFiles, negotiationRequestID, constants.File.SELLER_CONTRACT))
            } yield result
          case constants.File.FIAT_PROOF =>
            val negotiationFiles = masterTransactionNegotiationFiles.Service.get(negotiationRequestID, constants.File.FIAT_PROOF)
            for {
              negotiationFiles <- negotiationFiles
              result <- withUsernameToken.PartialContent(views.html.component.master.moderatedBuyerExecuteOrderDocument(negotiationFiles, negotiationRequestID, constants.File.FIAT_PROOF))
            } yield result
          case constants.File.AWB_PROOF =>
            val negotiationFiles = masterTransactionNegotiationFiles.Service.get(negotiationRequestID, constants.File.AWB_PROOF)
            for {
              negotiationFiles <- negotiationFiles
              result <- withUsernameToken.PartialContent(views.html.component.master.moderatedSellerExecuteOrderDocument(negotiationFiles, negotiationRequestID, constants.File.AWB_PROOF))
            } yield result
          case _ => withUsernameToken.Ok(views.html.index())
        }
      }

      (for {
        _ <- storeFile
        result <- getResult
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateZoneNegotiation(name: String, documentType: String, negotiationRequestID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val oldDocumentFileName = masterTransactionNegotiationFiles.Service.getFileName(id = negotiationRequestID, documentType = documentType)

      def updateFile(oldDocumentFileName: String): Future[Boolean] = fileResourceManager.updateFile[masterTransaction.NegotiationFile](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getZoneNegotiationFilePath(documentType),
        oldDocumentFileName = oldDocumentFileName,
        document = masterTransaction.NegotiationFile(id = negotiationRequestID, documentType = documentType, fileName = name, file = None, documentContent = None, status = None),
        updateOldDocument = masterTransactionNegotiationFiles.Service.insertOrUpdateOldDocument
      )

      def getResult: Future[Result] = {
        documentType match {
          case constants.File.BUYER_CONTRACT =>
            val optionNegotiationFiles = masterTransactionNegotiationFiles.Service.get(negotiationRequestID, constants.File.BUYER_CONTRACT)
            for {
              optionNegotiationFiles <- optionNegotiationFiles
              result <- withUsernameToken.PartialContent(views.html.component.master.confirmBuyerBidDocument(optionNegotiationFiles, negotiationRequestID, constants.File.BUYER_CONTRACT))
            } yield result
          case constants.File.SELLER_CONTRACT =>
            val optionNegotiationFiles = masterTransactionNegotiationFiles.Service.get(negotiationRequestID, constants.File.SELLER_CONTRACT)
            for {
              optionNegotiationFiles <- optionNegotiationFiles
              result <- withUsernameToken.PartialContent(views.html.component.master.confirmSellerBidDocument(optionNegotiationFiles, negotiationRequestID, constants.File.SELLER_CONTRACT))
            } yield result
          case constants.File.FIAT_PROOF =>
            val optionNegotiationFiles = masterTransactionNegotiationFiles.Service.get(negotiationRequestID, constants.File.FIAT_PROOF)
            for {
              optionNegotiationFiles <- optionNegotiationFiles
              result <- withUsernameToken.PartialContent(views.html.component.master.moderatedBuyerExecuteOrderDocument(optionNegotiationFiles, negotiationRequestID, constants.File.FIAT_PROOF))
            } yield result
          case constants.File.AWB_PROOF =>
            val optionNegotiationFiles = masterTransactionNegotiationFiles.Service.get(negotiationRequestID, constants.File.AWB_PROOF)
            for {
              optionNegotiationFiles <- optionNegotiationFiles
              result <- withUsernameToken.PartialContent(views.html.component.master.moderatedSellerExecuteOrderDocument(optionNegotiationFiles, negotiationRequestID, constants.File.AWB_PROOF))
            } yield result
          case _ => withUsernameToken.Ok(views.html.index())
        }
      }

      (for {
        oldDocumentFileName <- oldDocumentFileName
        _ <- updateFile(oldDocumentFileName)
        result <- getResult
      } yield result
        ).recover {
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

  def zoneAccessedAssetFile(id: String, fileName: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val userZoneID = masterZones.Service.tryGetID(loginState.username)
      val accountID = masterTransactionIssueAssetRequests.Service.getAccountID(id)

      def traderZoneID(accountID: String): Future[String] = masterTraders.Service.tryGetZoneIDByAccountID(accountID)

      (for {
        userZoneID <- userZoneID
        accountID <- accountID
        traderZoneID <- traderZoneID(accountID)
      } yield {
        if (traderZoneID == userZoneID) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getTraderAssetFilePath(documentType), fileName = fileName))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneAccessedNegotiationFile(id: String, fileName: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val userZoneID = masterZones.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getTrader(id: String): Future[Trader] = masterTraders.Service.tryGet(id)

      (for {
        userZoneID <- userZoneID
        negotiation <- negotiation
        sellerTrader <- getTrader(negotiation.sellerTraderID)
        buyerTrader <- getTrader(negotiation.buyerTraderID)
      } yield {
        if (sellerTrader.zoneID == userZoneID || buyerTrader.zoneID == userZoneID) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getZoneNegotiationFilePath(documentType), fileName = fileName))
        } else Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationAccessedFile(accountID: String, fileName: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val userOrganizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val traderOrganizationID = masterTraders.Service.getOrganizationIDByAccountID(accountID)
      (for {
        userOrganizationID <- userOrganizationID
        traderOrganizationID <- traderOrganizationID
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
        documentType = documentType,
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
        documentType = documentType,
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

  def tradingFile(id: String, fileName: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def checkTraderNegotiationExists(traderID: String) = masterNegotiations.Service.checkTraderNegotiationExists(id = id, traderID = traderID)

      (for {
        traderID <- traderID
        traderNegotiationExists <- checkTraderNegotiationExists(traderID)
      } yield {
        val path = documentType match {
          case constants.File.OBL | constants.File.COO | constants.File.COA => if (traderNegotiationExists) fileResourceManager.getTraderAssetFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
          case constants.File.CONTRACT | constants.File.INVOICE | constants.File.OTHER => if (traderNegotiationExists) fileResourceManager.getTraderNegotiationFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
        }
        Ok.sendFile(utilities.FileOperations.fetchFile(path = path, fileName = fileName))
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationAccessedTradingFile(id: String, fileName: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getOrganizationTraderIDs(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      (for {
        organizationID <- organizationID
        negotiation <- negotiation
        organizationTraderIDs <- getOrganizationTraderIDs(organizationID)
      } yield {
        val path = documentType match {
          case constants.File.OBL | constants.File.COO | constants.File.COA => if (organizationTraderIDs.contains(negotiation.buyerTraderID) || organizationTraderIDs.contains(negotiation.sellerTraderID)) fileResourceManager.getTraderAssetFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
          case constants.File.CONTRACT | constants.File.INVOICE | constants.File.OTHER => if (organizationTraderIDs.contains(negotiation.buyerTraderID) || organizationTraderIDs.contains(negotiation.sellerTraderID)) fileResourceManager.getTraderNegotiationFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
        }
        Ok.sendFile(utilities.FileOperations.fetchFile(path = path, fileName = fileName))
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }
}
