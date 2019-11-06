package controllers

import java.nio.file.Files

import controllers.actions.{WithGenesisLoginAction, WithUserLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Serializable._
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.master.FileUpload

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddZoneController @Inject()(messagesControllerComponents: MessagesControllerComponents, fileResourceManager: utilities.FileResourceManager, withZoneLoginAction: WithZoneLoginAction, transaction: utilities.Transaction, utilitiesNotification: utilities.Notification, blockchainAccounts: blockchain.Accounts, masterZoneKYCs: master.ZoneKYCs, transactionsAddZone: transactions.AddZone, blockchainZones: models.blockchain.Zones, blockchainTransactionAddZones: blockchainTransaction.AddZones, masterAccounts: master.Accounts, masterZones: master.Zones, withUserLoginAction: WithUserLoginAction, withGenesisLoginAction: WithGenesisLoginAction, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ADD_ZONE

  def addZoneForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zone = masterZones.Service.getByAccountID(loginState.username)
      (for {
        zone <- zone
      } yield withUsernameToken.Ok(views.html.component.master.addZone(views.companion.master.AddZone.form.fill(views.companion.master.AddZone.Data(name = zone.name, currency = zone.currency, address = views.companion.master.AddZone.AddressData(addressLine1 = zone.address.addressLine1, addressLine2 = zone.address.addressLine2, landmark = zone.address.landmark, city = zone.address.city, country = zone.address.country, zipCode = zone.address.zipCode, phone = zone.address.phone)))))
        ).recover {
        case _: BaseException => withUsernameToken.Ok(views.html.component.master.addZone())
      }
  }


  def addZone(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddZone.form.bindFromRequest().fold(
        formWithErrors => {
          Future {
            BadRequest(views.html.component.master.addZone(formWithErrors))
          }
        },
        addZoneData => {

          val id = masterZones.Service.insertOrUpdate(accountID = loginState.username, name = addZoneData.name, currency = addZoneData.currency, address = Address(addressLine1 = addZoneData.address.addressLine1, addressLine2 = addZoneData.address.addressLine2, landmark = addZoneData.address.landmark, city = addZoneData.address.city, country = addZoneData.address.country, zipCode = addZoneData.address.zipCode, phone = addZoneData.address.phone))

          def zoneKYCs(id: String) = masterZoneKYCs.Service.getAllDocuments(id)

          (for {
            id <- id
            zoneKYCs <- zoneKYCs(id)
          } yield withUsernameToken.PartialContent(views.html.component.master.userUploadOrUpdateZoneKYC(zoneKYCs))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def userUploadOrUpdateZoneKYCView(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val zoneID = masterZones.Service.getID(loginState.username)

      def zoneKYCs(zoneID: String) = masterZoneKYCs.Service.getAllDocuments(zoneID)

      (for {
        zoneID <- zoneID
        zoneKYCs <- zoneKYCs(zoneID)
      } yield Ok(views.html.component.master.userUploadOrUpdateZoneKYC(zoneKYCs))

        ).recover {
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

      val id = masterZones.Service.getID(loginState.username)

      def storeFile(id: String) = fileResourceManager.storeFile[master.ZoneKYC](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getZoneKYCFilePath(documentType),
        document = master.ZoneKYC(id = id, documentType = documentType, status = None, fileName = name, file = None),
        masterCreate = masterZoneKYCs.Service.create
      )

      def zoneKYCs(id: String) = masterZoneKYCs.Service.getAllDocuments(id)

      (for {
        id <- id
        _ <- storeFile(id)
        zoneKYCs <- zoneKYCs(id)
      } yield withUsernameToken.PartialContent(views.html.component.master.userUploadOrUpdateZoneKYC(zoneKYCs))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userUpdateZoneKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.AddZoneController.userUploadZoneKYC), utilities.String.getJsRouteFunction(routes.javascript.AddZoneController.userUpdateZoneKYC), documentType))
  }


  def userUpdateZoneKYC(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val id = masterZones.Service.getID(loginState.username)

      def oldDocumentFileName(id: String) = masterZoneKYCs.Service.getFileName(id = id, documentType = documentType)

      def updateFile(oldDocumentFileName: String, id: String) = fileResourceManager.updateFile[master.ZoneKYC](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getZoneKYCFilePath(documentType),
        oldDocumentFileName = oldDocumentFileName,
        document = master.ZoneKYC(id = id, documentType = documentType, status = None, fileName = name, file = None),
        updateOldDocument = masterZoneKYCs.Service.updateOldDocument
      )

      def zoneKYCs(id: String) = masterZoneKYCs.Service.getAllDocuments(id)

      (for {
        id <- id
        oldDocumentFileName <- oldDocumentFileName(id)
        _ <- updateFile(oldDocumentFileName, id)
        zoneKYCs <- zoneKYCs(id)
      } yield withUsernameToken.PartialContent(views.html.component.master.userUploadOrUpdateZoneKYC(zoneKYCs))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userReviewAddZoneRequestForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val zone = masterZones.Service.getByAccountID(loginState.username)

      def zoneKYCs(zone: models.master.Zone) = masterZoneKYCs.Service.getAllDocuments(zone.id)

      (for {
        zone <- zone
        zoneKYCs <- zoneKYCs(zone)
      } yield withUsernameToken.Ok(views.html.component.master.userReviewAddZoneRequest(zone = zone, zoneKYCs = zoneKYCs))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userReviewAddZoneRequest(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ReviewAddZoneRequest.form.bindFromRequest().fold(
        formWithErrors => {

          val zone = masterZones.Service.getByAccountID(loginState.username)

          def zoneKYCs(zone: models.master.Zone) = masterZoneKYCs.Service.getAllDocuments(zone.id)

          (for {
            zone <- zone
            zoneKYCs <- zoneKYCs(zone)
          } yield BadRequest(views.html.component.master.userReviewAddZoneRequest(formWithErrors, zone = zone, zoneKYCs = zoneKYCs))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }

        },
        userReviewAddZoneRequestData => {
          val id = masterZones.Service.getID(loginState.username)

          def allKYCFileTypesExists(id: String) = masterZoneKYCs.Service.checkAllKYCFileTypesExists(id)

          def getResult(id: String, allKYCFileTypesExists: Boolean) = {
            if (userReviewAddZoneRequestData.completion && allKYCFileTypesExists) {
              val markZoneFormCompleted = masterZones.Service.markZoneFormCompleted(id)
              for {
                _ <- markZoneFormCompleted
              } yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ZONE_ADDED_FOR_VERIFICATION)))
            } else {
              val zone = masterZones.Service.getByAccountID(loginState.username)

              def zoneKYCs(zone: models.master.Zone) = masterZoneKYCs.Service.getAllDocuments(zone.id)

              for {
                zone <- zone
                zoneKYCs <- zoneKYCs(zone)
              } yield BadRequest(views.html.component.master.userReviewAddZoneRequest(zone = zone, zoneKYCs = zoneKYCs))
            }
          }

          (for {
            id <- id
            allKYCFileTypesExists <- allKYCFileTypesExists(id)
            result <- getResult(id, allKYCFileTypesExists)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }


  def verifyZoneForm(zoneID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.verifyZone(zoneID = zoneID))
  }

  def verifyZone: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyZone.form.bindFromRequest().fold(
        formWithErrors => {
          Future {
            BadRequest(views.html.component.master.verifyZone(formWithErrors, formWithErrors.data(constants.Form.ZONE_ID)))
          }
        },
        verifyZoneData => {
          val allKYCFilesVerified = masterZoneKYCs.Service.checkAllKYCFilesVerified(verifyZoneData.zoneID)

          def getResult(allKYCFilesVerified: Boolean) = {
            if (allKYCFilesVerified) {
              val accountID = masterZones.Service.getAccountId(verifyZoneData.zoneID)

              def zoneAccountAddress(accountID: String) = masterAccounts.Service.getAddress(accountID)

              def transactionProcess(zoneAccountAddress: String) = transaction.process[blockchainTransaction.AddZone, transactionsAddZone.Request](
                entity = blockchainTransaction.AddZone(from = loginState.address, to = zoneAccountAddress, zoneID = verifyZoneData.zoneID, gas = verifyZoneData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionAddZones.Service.create,
                request = transactionsAddZone.Request(transactionsAddZone.BaseReq(from = loginState.address, gas = verifyZoneData.gas.toString), to = zoneAccountAddress, zoneID = verifyZoneData.zoneID, password = verifyZoneData.password, mode = transactionMode),
                action = transactionsAddZone.Service.post,
                onSuccess = blockchainTransactionAddZones.Utility.onSuccess,
                onFailure = blockchainTransactionAddZones.Utility.onFailure,
                updateTransactionHash = blockchainTransactionAddZones.Service.updateTransactionHash
              )

              for {
                accountID <- accountID
                zoneAccountAddress <- zoneAccountAddress(accountID)
                _ <- transactionProcess(zoneAccountAddress)
              } yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ZONE_VERIFIED)))
            } else Future {
              PreconditionFailed(views.html.index(failures = Seq(constants.Response.ALL_KYC_FILES_NOT_VERIFIED)))
            }
          }

          (for {
            allKYCFilesVerified <- allKYCFilesVerified
            result <- getResult(allKYCFilesVerified)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def viewPendingVerifyZoneRequests: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val verifyZoneRequests = masterZones.Service.getVerifyZoneRequests
      (for {
        verifyZoneRequests <- verifyZoneRequests
      } yield withUsernameToken.Ok(views.html.component.master.viewPendingVerifyZoneRequests(verifyZoneRequests))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }


  def viewKYCDocuments(accountID: String): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val zoneKYCs = masterZoneKYCs.Service.getAllDocuments(accountID)
      (for {
        zoneKYCs <- zoneKYCs
      } yield withUsernameToken.Ok(views.html.component.master.viewVerificationZoneKYCDouments(zoneKYCs))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateZoneKYCDocumentStatusForm(zoneID: String, documentType: String): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val zoneKYC = masterZoneKYCs.Service.get(id = zoneID, documentType = documentType)
      for {
        zoneKYC <- zoneKYC
      } yield Ok(views.html.component.master.updateZoneKYCDocumentStatus(zoneKYC = zoneKYC))

  }

  def updateZoneKYCDocumentStatus(): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.UpdateZoneKYCDocumentStatus.form.bindFromRequest().fold(
        formWithErrors => {

          val zoneKYC = masterZoneKYCs.Service.get(id = formWithErrors(constants.FormField.ZONE_ID.name).value.get, documentType = formWithErrors(constants.FormField.DOCUMENT_TYPE.name).value.get)
          (for {
            zoneKYC <- zoneKYC
          } yield BadRequest(views.html.component.master.updateZoneKYCDocumentStatus(formWithErrors, zoneKYC))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }

        },
        updateZoneKYCDocumentStatusData => {

          val sendNotification = if (updateZoneKYCDocumentStatusData.status) {
            val verify = masterZoneKYCs.Service.verify(id = updateZoneKYCDocumentStatusData.zoneID, documentType = updateZoneKYCDocumentStatusData.documentType)
            val zoneId = masterZones.Service.getAccountId(updateZoneKYCDocumentStatusData.zoneID)
            for {
              _ <- verify
              zoneId <- zoneId
            } yield utilitiesNotification.send(zoneId, constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))

          } else {
            val reject = masterZoneKYCs.Service.reject(id = updateZoneKYCDocumentStatusData.zoneID, documentType = updateZoneKYCDocumentStatusData.documentType)
            val zoneId = masterZones.Service.getAccountId(updateZoneKYCDocumentStatusData.zoneID)
            for {
              _ <- reject
              zoneId <- zoneId
            } yield utilitiesNotification.send(zoneId, constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
          }

          def zoneKYC = masterZoneKYCs.Service.get(id = updateZoneKYCDocumentStatusData.zoneID, documentType = updateZoneKYCDocumentStatusData.documentType)

          (for {
            _ <- sendNotification
            zoneKYC <- zoneKYC
          } yield PartialContent(views.html.component.master.updateZoneKYCDocumentStatus(zoneKYC = zoneKYC))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }

      )
  }

  def rejectVerifyZoneRequestForm(zoneID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.rejectVerifyZoneRequest(zoneID = zoneID))
  }

  def rejectVerifyZoneRequest: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RejectVerifyZoneRequest.form.bindFromRequest().fold(
        formWithErrors => {
          Future {
            BadRequest(views.html.component.master.rejectVerifyZoneRequest(formWithErrors, formWithErrors.data(constants.Form.ZONE_ID)))
          }
        },
        rejectVerifyZoneRequestData => {
          val rejectZone = masterZones.Service.rejectZone(rejectVerifyZoneRequestData.zoneID)
          val accountID = masterZones.Service.getAccountId(rejectVerifyZoneRequestData.zoneID)

          def rejectAll(accountID: String) = masterZoneKYCs.Service.rejectAll(accountID)

          (for {
            _ <- rejectZone
            accountID <- accountID
            _ <- rejectAll(accountID)
          } yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.VERIFY_ZONE_REJECTED)))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }


  def viewZonesInGenesis: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val getAllVerified = masterZones.Service.getAllVerified
      (for {
        getAllVerified <- getAllVerified
      } yield withUsernameToken.Ok(views.html.component.master.viewZonesInGenesis(getAllVerified))
        ).recover {
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

      val id = masterZones.Service.getID(loginState.username)

      def storeFile(id: String) = fileResourceManager.storeFile[master.ZoneKYC](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getZoneKYCFilePath(documentType),
        document = master.ZoneKYC(id = id, documentType = documentType, status = None, fileName = name, file = None),
        masterCreate = masterZoneKYCs.Service.create
      )

      (for {
        id <- id
        _ <- storeFile(id)
      } yield withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateZoneKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.AddZoneController.uploadZoneKYC), utilities.String.getJsRouteFunction(routes.javascript.AddZoneController.updateZoneKYC), documentType))
  }

  def updateZoneKYC(name: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val id = masterZones.Service.getID(loginState.username)

      def oldDocumentFileName(id: String) = masterZoneKYCs.Service.getFileName(id = id, documentType = documentType)

      def updateFile(oldDocumentFileName: String, id: String) = fileResourceManager.updateFile[master.ZoneKYC](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getZoneKYCFilePath(documentType),
        oldDocumentFileName = oldDocumentFileName,
        document = master.ZoneKYC(id = id, documentType = documentType, status = None, fileName = name, file = None),
        updateOldDocument = masterZoneKYCs.Service.updateOldDocument
      )

      (for {
        id <- id
        oldDocumentFileName <- oldDocumentFileName(id)
        _ <- updateFile(oldDocumentFileName, id)
      } yield withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def blockchainAddZoneForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.addZone())
  }

  def blockchainAddZone: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.AddZone.form.bindFromRequest().fold(
      formWithErrors => {
        Future {
          BadRequest(views.html.component.blockchain.addZone(formWithErrors))
        }
      },
      addZoneData => {

        val postRequest = transactionsAddZone.Service.post(transactionsAddZone.Request(transactionsAddZone.BaseReq(from = addZoneData.from, gas = addZoneData.gas.toString), to = addZoneData.to, zoneID = addZoneData.zoneID, password = addZoneData.password, mode = transactionMode))
        (for {
          _ <- postRequest
        } yield Ok(views.html.index(successes = Seq(constants.Response.ZONE_ADDED)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
