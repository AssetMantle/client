package controllers

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import queries.{GetTruliooAuthentication, GetTruliooConsents, GetTruliooCountryCodes, GetTruliooCountrySubdivisions, GetTruliooDataSources, GetTruliooDetailedConsents, GetTruliooEntities, GetTruliooFields, GetTruliooRecommendedFields, GetTruliooTransactionRecords}
import transactions.TruliooVerify
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
class BackgroundCheckController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                          getTruliooAuthentication: GetTruliooAuthentication,
                                          getTruliooCountryCodes: GetTruliooCountryCodes,
                                          getTruliooEntities: GetTruliooEntities,
                                          getTruliooConsents: GetTruliooConsents,
                                          getTruliooDetailedConsents: GetTruliooDetailedConsents,
                                          getTruliooDataSources: GetTruliooDataSources,
                                          getTruliooFields: GetTruliooFields,
                                          getTruliooRecommendedFields: GetTruliooRecommendedFields,
                                          getTruliooTransactionRecords: GetTruliooTransactionRecords,
                                          getTruliooCountrySubdivisions: GetTruliooCountrySubdivisions,
                                          truliooVerify: TruliooVerify,
                                            withLoginAction: WithLoginAction, masterTraderBackgroundChecks: master.TraderBackgroundChecks, masterOrganizationBackgroundChecks: master.OrganizationBackgroundChecks, masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles, masterTransactionIssueAssetRequests: masterTransaction.IssueAssetRequests, masterTransactionNegotiationRequests: masterTransaction.NegotiationRequests, masterZones: master.Zones, masterOrganizations: master.Organizations, masterTraders: master.Traders, fileResourceManager: utilities.FileResourceManager, withZoneLoginAction: WithZoneLoginAction, withUserLoginAction: WithUserLoginAction, withTraderLoginAction: WithTraderLoginAction, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration, wsClient: WSClient) extends AbstractController(messagesControllerComponents) with I18nSupport {

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
      val trader = masterTraders.Service.get(traderID)

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

      val trader = masterTraders.Service.get(traderID)

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
      val zoneID = masterZones.Service.getID(loginState.username)
      val trader = masterTraders.Service.get(traderID)
      val allDocuments = masterTraderBackgroundChecks.Service.getAllDocuments(traderID)
      (for {
        allDocuments <- allDocuments
        zoneID <- zoneID
        trader <- trader
        result <- withUsernameToken.Ok(views.html.component.master.zoneUploadOrUpdateTraderBackgroundCheck(allDocuments, trader))
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

      val storeFile = fileResourceManager.storeFile[master.OrganizationBackgroundCheck](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getBackgroundCheckFilePath(documentType),
        document = master.OrganizationBackgroundCheck(id = organizationID, documentType = documentType, fileName = name, file = None, status = Option(true)),
        masterCreate = masterOrganizationBackgroundChecks.Service.create
      )

      def allDocuments = masterOrganizationBackgroundChecks.Service.getAllDocuments(organizationID)
      val organization = masterOrganizations.Service.get(organizationID)

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

      val organization = masterOrganizations.Service.get(organizationID)

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
      val zoneID = masterZones.Service.getID(loginState.username)
      val organization = masterOrganizations.Service.get(organizationID)
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
      val organizationZoneID = masterOrganizations.Service.getZoneID(organizationID)
      val userZoneID = masterZones.Service.getID(loginState.username)
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
      val traderZoneID = masterTraders.Service.getZoneID(traderID)
      val userZoneID = masterZones.Service.getID(loginState.username)
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

  def testTruliooAPI: Action[AnyContent] = Action.async { implicit request =>
    //    authTest
    //    countryCodes
        entities
    //    fields
    //    recommdendedFields
    //    consents
    //    detailedConsents
    //    countrySubdivisions
    //    dataSources
//    transactionRecord
//    verify
  }

  //auth test
  def authTest = {
    val response = getTruliooAuthentication.Service.get()
    (for {
      response <- response
    } yield {
      val printValue = response.body.mkString
      println(printValue)
      Ok(views.html.test(printValue))
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
    }
  }

  //country Codes
  def countryCodes = {
    val response = getTruliooCountryCodes.Service.get()
    (for {
      response <- response
    } yield {
      val printValue = response.body
      println(printValue, printValue.foreach(x => println(x)))
      Ok(views.html.test(printValue.mkString))
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
    }
  }

  //entities
  def entities = {
    val response = getTruliooEntities.Service.get(countryCode = "AU")
    (for {
      response <- response
    } yield {
      val printValue = response
      println(printValue, printValue.foreach(x => println(x)))
      Ok(views.html.test(printValue.mkString))
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
    }
  }

  //_fields
  def fields = {
    val response = getTruliooFields.Service.get(countryCode = "AU")
    (for {
      response <- response
    } yield {
      val printValue = response
      println(printValue)
      Ok(views.html.test(printValue.toString))
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
    }
  }
  // recommendedFields
  def  recommdendedFields= {
    val response = getTruliooRecommendedFields.Service.get(countryCode = "AU")
    (for {
      response <- response
    } yield {
      val printValue = response
      println(printValue)
      Ok(views.html.test(printValue.toString))
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
    }
  }

  //consents
  def consents = {
    val response = getTruliooConsents.Service.get(countryCode = "AU")
    (for {
      response <- response
    } yield {
      val printValue = response
      println(printValue)
      Ok(views.html.test(printValue.body.mkString))
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
    }
  }

  // detailedConsents
  def detailedConsents = {
    val response = getTruliooDetailedConsents.Service.get(countryCode = "AU")
    (for {
      response <- response
    } yield {
      val printValue = response
      println(printValue)
      Ok(views.html.test(printValue.toString))
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
    }
  }

  // countrySubdivisions
  def countrySubdivisions = {
    val response = getTruliooCountrySubdivisions.Service.get(countryCode = "AU")
    (for {
      response <- response
    } yield {
      val printValue = response
      println(printValue)
      Ok(views.html.test(printValue.toString))
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
    }
  }

  // dataSources
  def dataSources = {
    val response = getTruliooDataSources.Service.get(countryCode = "AU")
    (for {
      response <- response
    } yield {
      val printValue = response
      println(printValue)
      Ok(views.html.test(printValue.toString))
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
    }
  }

  // transactionRecord
  def transactionRecord = {
    val response = getTruliooTransactionRecords.Service.get(id = "255e2d66-0b03-6203-0aff-9b8f7df8fe9f")
    (for {
      response <- response
    } yield {
      val printValue = response
      println(printValue.TransactionID)
      Ok(views.html.test(printValue.CountryCode + printValue.CustomerReferenceID).toString()+ printValue.Errors.mkString + printValue.InputFields.mkString)
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
    }
  }

  // POST verify
  def verify = {
    val response = truliooVerify.Service.post(request =  truliooVerify.Request(true,false,"Identity Verification" ,Seq("Visa Verification"),"AU", truliooVerify.DataFields(truliooVerify.PersonInfo("John","Smith", 5, 3, 1983),None,None,None)))
    (for {
      response <- response
    } yield {
      val printValue = response.body
      println(printValue)
      Ok(views.html.test(printValue.toString))
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
    }
  }
}
