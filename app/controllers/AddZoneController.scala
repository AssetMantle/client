package controllers

import java.nio.file.Files

import controllers.actions.{WithGenesisLoginAction, WithUserLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master}
import models.common.Serializable._
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.master.FileUpload

import scala.concurrent.ExecutionContext

@Singleton
class AddZoneController @Inject()(messagesControllerComponents: MessagesControllerComponents, fileResourceManager: utilities.FileResourceManager, withZoneLoginAction: WithZoneLoginAction, transaction: utilities.Transaction, utilitiesNotification: utilities.Notification, blockchainAccounts: blockchain.Accounts, masterZoneKYCs: master.ZoneKYCs, transactionsAddZone: transactions.AddZone, blockchainZones: models.blockchain.Zones, blockchainTransactionAddZones: blockchainTransaction.AddZones, masterAccounts: master.Accounts, masterZones: master.Zones, withUserLoginAction: WithUserLoginAction, withGenesisLoginAction: WithGenesisLoginAction, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ADD_ZONE

  def addZoneForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val zone = masterZones.Service.getByAccountID(loginState.username)
        Ok(views.html.component.master.addZone(views.companion.master.AddZone.form.fill(views.companion.master.AddZone.Data(name = zone.name, currency = zone.currency, address = views.companion.master.AddZone.AddressData(addressLine1 = zone.address.addressLine1, addressLine2 = zone.address.addressLine2, landmark = zone.address.landmark, city = zone.address.city, country = zone.address.country, zipCode = zone.address.zipCode, phone = zone.address.phone)))))
      } catch {
        case _: BaseException => Ok(views.html.component.master.addZone(views.companion.master.AddZone.form))
      }
  }

  def addZone(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddZone.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.addZone(formWithErrors))
        },
        addZoneData => {
          try {
            val id = masterZones.Service.insertOrUpdate(accountID = loginState.username, name = addZoneData.name, currency = addZoneData.currency, address = Address(addressLine1 = addZoneData.address.addressLine1, addressLine2 = addZoneData.address.addressLine2, landmark = addZoneData.address.landmark, city = addZoneData.address.city, country = addZoneData.address.country, zipCode = addZoneData.address.zipCode, phone = addZoneData.address.phone))
            PartialContent(views.html.component.master.userUploadOrUpdateZoneKYC(masterZoneKYCs.Service.getAllDocuments(id)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def userUploadOrUpdateZoneKYCView(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.userUploadOrUpdateZoneKYC(masterZoneKYCs.Service.getAllDocuments(masterZones.Service.getID(loginState.username))))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userUploadZoneKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.AddZoneController.userUploadZoneKYC), utilities.String.getJsRouteFunction(routes.javascript.AddZoneController.userStoreZoneKYC), documentType))
  }

  def userUploadZoneKYC(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.index(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getZoneKYCFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def userStoreZoneKYC(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.storeFile[master.ZoneKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getZoneKYCFilePath(documentType),
          document = master.ZoneKYC(id = masterZones.Service.getID(loginState.username), documentType = documentType, status = None, fileName = name, file = None),
          masterCreate = masterZoneKYCs.Service.create
        )
        PartialContent(views.html.component.master.userUploadOrUpdateZoneKYC(masterZoneKYCs.Service.getAllDocuments(masterZones.Service.getID(loginState.username))))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userUpdateZoneKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.AddZoneController.userUploadZoneKYC), utilities.String.getJsRouteFunction(routes.javascript.AddZoneController.userUpdateZoneKYC), documentType))
  }

  def userUpdateZoneKYC(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val id = masterZones.Service.getID(loginState.username)
        fileResourceManager.updateFile[master.ZoneKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getZoneKYCFilePath(documentType),
          oldDocumentFileName = masterZoneKYCs.Service.getFileName(id = id, documentType = documentType),
          document = master.ZoneKYC(id = id, documentType = documentType, status = None, fileName = name, file = None),
          updateOldDocument = masterZoneKYCs.Service.updateOldDocument
        )
        PartialContent(views.html.component.master.userUploadOrUpdateZoneKYC(masterZoneKYCs.Service.getAllDocuments(masterZones.Service.getID(loginState.username))))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def reviewZoneCompletionForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val zone = masterZones.Service.getByAccountID(loginState.username)
        Ok(views.html.component.master.reviewZoneCompletion(views.companion.master.ZoneCompletion.form, zone = zone, zoneKYCs = masterZoneKYCs.Service.getAllDocuments(zone.id)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def reviewZoneCompletion(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ZoneCompletion.form.bindFromRequest().fold(
        formWithErrors => {
          try {
            val zone = masterZones.Service.getByAccountID(loginState.username)
            BadRequest(views.html.component.master.reviewZoneCompletion(formWithErrors, zone = zone, zoneKYCs = masterZoneKYCs.Service.getAllDocuments(zone.id)))
          } catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        reviewZoneCompletionData => {
          try {
            val id = masterZones.Service.getID(loginState.username)
            if (reviewZoneCompletionData.completion && masterZoneKYCs.Service.checkAllKYCFileTypesExists(id)) {
              masterZones.Service.markZoneFormCompleted(id)
              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ZONE_ADDED_FOR_VERIFICATION)))
            } else {
              val zone = masterZones.Service.getByAccountID(loginState.username)
              BadRequest(views.html.component.master.reviewZoneCompletion(views.companion.master.ZoneCompletion.form, zone = zone, zoneKYCs = masterZoneKYCs.Service.getAllDocuments(zone.id)))
            }
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def verifyZoneForm(zoneID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.verifyZone(views.companion.master.VerifyZone.form, zoneID))
  }

  def verifyZone: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyZone.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.verifyZone(formWithErrors, formWithErrors.data(constants.Form.ZONE_ID)))
        },
        verifyZoneData => {
          try {
            if (masterZoneKYCs.Service.checkAllKYCFilesVerified(verifyZoneData.zoneID)) {
              val zoneAccountAddress = masterAccounts.Service.getAddress(masterZones.Service.getAccountId(verifyZoneData.zoneID))
              transaction.process[blockchainTransaction.AddZone, transactionsAddZone.Request](
                entity = blockchainTransaction.AddZone(from = loginState.address, to = zoneAccountAddress, zoneID = verifyZoneData.zoneID, gas = verifyZoneData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionAddZones.Service.create,
                request = transactionsAddZone.Request(transactionsAddZone.BaseReq(from = loginState.address, gas = verifyZoneData.gas.toString), to = zoneAccountAddress, zoneID = verifyZoneData.zoneID, password = verifyZoneData.password, mode = transactionMode),
                action = transactionsAddZone.Service.post,
                onSuccess = blockchainTransactionAddZones.Utility.onSuccess,
                onFailure = blockchainTransactionAddZones.Utility.onFailure,
                updateTransactionHash = blockchainTransactionAddZones.Service.updateTransactionHash
              )
              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ZONE_VERIFIED)))
            } else {
              PreconditionFailed(views.html.index(failures = Seq(constants.Response.ALL_KYC_FILES_NOT_VERIFIED)))
            }
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def viewPendingVerifyZoneRequests: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.viewPendingVerifyZoneRequests(masterZones.Service.getVerifyZoneRequests))
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def viewKYCDocuments(zoneID: String): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.viewVerificationZoneKYCDouments(masterZoneKYCs.Service.getAllDocuments(zoneID)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def changeZoneKYCDocumentStatusForm(zoneID: String, documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.changeZoneKYCDocumentStatus(views.companion.master.ChangeZoneKYCDocumentStatus.form.fill(views.companion.master.ChangeZoneKYCDocumentStatus.Data(zoneID = zoneID, documentType = documentType, status = false))))
  }

  def changeZoneKYCDocumentStatus(): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ChangeZoneKYCDocumentStatus.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.changeZoneKYCDocumentStatus(formWithErrors))
        },
        changeZoneKYCDocumentStatusData => {
          try {
            if (changeZoneKYCDocumentStatusData.status) {
              masterZoneKYCs.Service.verify(id = changeZoneKYCDocumentStatusData.zoneID, documentType = changeZoneKYCDocumentStatusData.documentType)
              utilitiesNotification.send(masterZones.Service.getAccountId(changeZoneKYCDocumentStatusData.zoneID), constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
            } else {
              masterZoneKYCs.Service.reject(id = changeZoneKYCDocumentStatusData.zoneID, documentType = changeZoneKYCDocumentStatusData.documentType)
              utilitiesNotification.send(masterZones.Service.getAccountId(changeZoneKYCDocumentStatusData.zoneID), constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
            }
            Redirect(routes.ViewController.genesisRequest())
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def rejectVerifyZoneRequestForm(zoneID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.rejectVerifyZoneRequest(views.companion.master.RejectVerifyZoneRequest.form, zoneID))
  }

  def rejectVerifyZoneRequest: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RejectVerifyZoneRequest.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.rejectVerifyZoneRequest(formWithErrors, formWithErrors.data(constants.Form.ZONE_ID)))
        },
        rejectVerifyZoneRequestData => {
          try {
            masterZones.Service.rejectZone(rejectVerifyZoneRequestData.zoneID)
            masterZoneKYCs.Service.rejectAll(masterZones.Service.getAccountId(rejectVerifyZoneRequestData.zoneID))
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.VERIFY_ZONE_REJECTED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def viewZonesInGenesis: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.viewZonesInGenesis(masterZones.Service.getAllVerified))
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def uploadZoneKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.AddZoneController.uploadZoneKYC), utilities.String.getJsRouteFunction(routes.javascript.AddZoneController.storeZoneKYC), documentType))
  }

  def uploadZoneKYC(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.index(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getZoneKYCFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeZoneKYC(name: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.storeFile[master.ZoneKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getZoneKYCFilePath(documentType),
          document = master.ZoneKYC(id = masterZones.Service.getID(loginState.username), documentType = documentType, fileName = name, file = None, status = None),
          masterCreate = masterZoneKYCs.Service.create
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateZoneKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.AddZoneController.uploadZoneKYC), utilities.String.getJsRouteFunction(routes.javascript.AddZoneController.updateZoneKYC), documentType))
  }

  def updateZoneKYC(name: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val id = masterZones.Service.getID(loginState.username)
        fileResourceManager.updateFile[master.ZoneKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getZoneKYCFilePath(documentType),
          oldDocumentFileName = masterZoneKYCs.Service.getFileName(id = id, documentType = documentType),
          document = master.ZoneKYC(id = id, documentType = documentType, status = None, fileName = name, file = None),
          updateOldDocument = masterZoneKYCs.Service.updateOldDocument
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def blockchainAddZoneForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.addZone(views.companion.blockchain.AddZone.form))
  }

  def blockchainAddZone: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.AddZone.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.addZone(formWithErrors))
      },
      addZoneData => {
        try {
          transactionsAddZone.Service.post(transactionsAddZone.Request(transactionsAddZone.BaseReq(from = addZoneData.from, gas = addZoneData.gas.toString), to = addZoneData.to, zoneID = addZoneData.zoneID, password = addZoneData.password, mode = transactionMode))
          Ok(views.html.index(successes = Seq(constants.Response.ZONE_ADDED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
