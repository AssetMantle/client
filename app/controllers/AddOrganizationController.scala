package controllers

import java.nio.file.Files

import controllers.actions.{WithGenesisLoginAction, WithOrganizationLoginAction, WithUserLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchainTransaction.AddOrganization
import models.common.Serializable._
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.master.FileUpload

import scala.concurrent.ExecutionContext

@Singleton
class AddOrganizationController @Inject()(messagesControllerComponents: MessagesControllerComponents, withOrganizationLoginAction: WithOrganizationLoginAction, fileResourceManager: utilities.FileResourceManager, transaction: utilities.Transaction, masterOrganizationBankAccountDetails: master.OrganizationBankAccountDetails, utilitiesNotification: utilities.Notification, blockchainAccounts: blockchain.Accounts, masterOrganizationKYCs: master.OrganizationKYCs, masterTraders: master.Traders, transactionsAddOrganization: transactions.AddOrganization, blockchainOrganizations: blockchain.Organizations, masterZones: master.Zones, blockchainTransactionAddOrganizations: blockchainTransaction.AddOrganizations, masterOrganizations: master.Organizations, masterAccounts: master.Accounts, withUserLoginAction: WithUserLoginAction, withZoneLoginAction: WithZoneLoginAction, withGenesisLoginAction: WithGenesisLoginAction, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_ADD_ORGANIZATION

  private implicit val logger: Logger = Logger(this.getClass)

  def addOrganizationForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val organization = masterOrganizations.Service.getByAccountID(loginState.username)
        withUsernameToken.Ok(views.html.component.master.addOrganization(views.companion.master.AddOrganization.form.fill(value = views.companion.master.AddOrganization.Data(zoneID = organization.zoneID, name = organization.name, abbreviation = organization.abbreviation, establishmentDate = utilities.Date.sqlDateToUtilDate(organization.establishmentDate), email = organization.email, registeredAddress = views.companion.master.AddOrganization.AddressData(addressLine1 = organization.registeredAddress.addressLine1, addressLine2 = organization.registeredAddress.addressLine2, landmark = organization.registeredAddress.landmark, city = organization.registeredAddress.city, country = organization.registeredAddress.country, zipCode = organization.registeredAddress.zipCode, phone = organization.registeredAddress.phone), postalAddress = views.companion.master.AddOrganization.AddressData(addressLine1 = organization.postalAddress.addressLine1, addressLine2 = organization.postalAddress.addressLine2, landmark = organization.postalAddress.landmark, city = organization.postalAddress.city, country = organization.postalAddress.country, zipCode = organization.postalAddress.zipCode, phone = organization.postalAddress.phone))), zones = masterZones.Service.getAllVerified))
      } catch {
        case _: BaseException => withUsernameToken.Ok(views.html.component.master.addOrganization(zones = masterZones.Service.getAllVerified))
      }
  }

  def addOrganization(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddOrganization.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.addOrganization(formWithErrors, zones = masterZones.Service.getAllVerified))
        },
        addOrganizationData => {
          try {
            if (masterZones.Service.getVerificationStatus(addOrganizationData.zoneID)) {
              val id = masterOrganizations.Service.insertOrUpdateWithoutUBOs(zoneID = addOrganizationData.zoneID, accountID = loginState.username, name = addOrganizationData.name, abbreviation = addOrganizationData.abbreviation, establishmentDate = utilities.Date.utilDateToSQLDate(addOrganizationData.establishmentDate), email = addOrganizationData.email, registeredAddress = Address(addressLine1 = addOrganizationData.registeredAddress.addressLine1, addressLine2 = addOrganizationData.registeredAddress.addressLine2, landmark = addOrganizationData.registeredAddress.landmark, city = addOrganizationData.registeredAddress.city, country = addOrganizationData.registeredAddress.country, zipCode = addOrganizationData.registeredAddress.zipCode, phone = addOrganizationData.registeredAddress.phone), postalAddress = Address(addressLine1 = addOrganizationData.postalAddress.addressLine1, addressLine2 = addOrganizationData.postalAddress.addressLine2, landmark = addOrganizationData.postalAddress.landmark, city = addOrganizationData.postalAddress.city, country = addOrganizationData.postalAddress.country, zipCode = addOrganizationData.postalAddress.zipCode, phone = addOrganizationData.postalAddress.phone))
              try {
                withUsernameToken.PartialContent(views.html.component.master.userUpdateUBOs(views.companion.master.AddUBOs.form.fill(views.companion.master.AddUBOs.Data(masterOrganizations.Service.getUBOs(id).data.map(ubo => Option(views.companion.master.AddUBOs.UBOData(personName = ubo.personName, sharePercentage = ubo.sharePercentage, relationship = ubo.relationship, title = ubo.title)))))))
              } catch {
                case _: BaseException => withUsernameToken.PartialContent(views.html.component.master.userUpdateUBOs())
              }
            } else {
              Unauthorized(views.html.index(failures = Seq(constants.Response.UNVERIFIED_ZONE)))
            }
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def userUpdateUBOsForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.userUpdateUBOs(views.companion.master.AddUBOs.form.fill(views.companion.master.AddUBOs.Data(masterOrganizations.Service.getUBOs(masterOrganizations.Service.getID(loginState.username)).data.map(ubo => Option(views.companion.master.AddUBOs.UBOData(personName = ubo.personName, sharePercentage = ubo.sharePercentage, relationship = ubo.relationship, title = ubo.title)))))))
      } catch {
        case _: BaseException => withUsernameToken.Ok(views.html.component.master.userUpdateUBOs())
      }
  }

  def userUpdateUBOs(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddUBOs.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.userUpdateUBOs(formWithErrors))
        },
        updateUBOsData => {
          try {
            val id = masterOrganizations.Service.getID(loginState.username)
            masterOrganizations.Service.updateUBOs(id = id, ubos = updateUBOsData.ubos.filter(_.isDefined).map(uboData => UBO(personName = uboData.get.personName, sharePercentage = uboData.get.sharePercentage, relationship = uboData.get.relationship, title = uboData.get.title)))
            withUsernameToken.PartialContent(views.html.component.master.userUpdateUBOs(views.companion.master.AddUBOs.form.fill(views.companion.master.AddUBOs.Data(masterOrganizations.Service.getUBOs(id).data.map(ubo => Option(views.companion.master.AddUBOs.UBOData(personName = ubo.personName, sharePercentage = ubo.sharePercentage, relationship = ubo.relationship, title = ubo.title)))))))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def organizationBankAccountDetailForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val organizationBankAccountDetail = masterOrganizationBankAccountDetails.Service.get(masterOrganizations.Service.getID(loginState.username))
        withUsernameToken.Ok(views.html.component.master.organizationBankAccountDetail(views.companion.master.OrganizationBankAccountDetail.form.fill(views.companion.master.OrganizationBankAccountDetail.Data(accountHolder = organizationBankAccountDetail.accountHolder, nickName = organizationBankAccountDetail.nickName, accountNumber = organizationBankAccountDetail.accountNumber, bankName = organizationBankAccountDetail.bankName, swiftAddress = organizationBankAccountDetail.swiftAddress, address = organizationBankAccountDetail.address, country = organizationBankAccountDetail.country, zipCode = organizationBankAccountDetail.zipCode))))
      } catch {
        case _: BaseException => withUsernameToken.Ok(views.html.component.master.organizationBankAccountDetail())
      }
  }

  def organizationBankAccountDetail(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.OrganizationBankAccountDetail.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.organizationBankAccountDetail(formWithErrors))
        },
        organizationBankAccountDetailData => {
          try {
            val id = masterOrganizations.Service.getID(loginState.username)
            masterOrganizationBankAccountDetails.Service.insertOrUpdate(id = id, accountHolder = organizationBankAccountDetailData.accountHolder, nickName = organizationBankAccountDetailData.nickName, accountNumber = organizationBankAccountDetailData.accountNumber, bankName = organizationBankAccountDetailData.bankName, swiftAddress = organizationBankAccountDetailData.swiftAddress, address = organizationBankAccountDetailData.address, country = organizationBankAccountDetailData.country, zipCode = organizationBankAccountDetailData.zipCode)
            withUsernameToken.PartialContent(views.html.component.master.userUploadOrUpdateOrganizationKYC(masterOrganizationKYCs.Service.getAllDocuments(id)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def userUploadOrUpdateOrganizationKYCView(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.userUploadOrUpdateOrganizationKYC(masterOrganizationKYCs.Service.getAllDocuments(masterOrganizations.Service.getID(loginState.username))))
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userUploadOrganizationKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.userUploadOrganizationKYC), utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.userStoreOrganizationKYC), documentType))
  }

  def userUploadOrganizationKYC(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.index(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getOrganizationKYCFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def userStoreOrganizationKYC(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val id = masterOrganizations.Service.getID(loginState.username)
        fileResourceManager.storeFile[master.OrganizationKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getOrganizationKYCFilePath(documentType),
          document = master.OrganizationKYC(id = id, documentType = documentType, status = None, fileName = name, file = None),
          masterCreate = masterOrganizationKYCs.Service.create
        )
        withUsernameToken.PartialContent(views.html.component.master.userUploadOrUpdateOrganizationKYC(masterOrganizationKYCs.Service.getAllDocuments(id)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userUpdateOrganizationKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.userUploadOrganizationKYC), utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.userUpdateOrganizationKYC), documentType))
  }

  def userUpdateOrganizationKYC(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val organizationID = masterOrganizations.Service.getID(loginState.username)
        fileResourceManager.updateFile[master.OrganizationKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getOrganizationKYCFilePath(documentType),
          oldDocumentFileName = masterOrganizationKYCs.Service.getFileName(id = organizationID, documentType = documentType),
          document = master.OrganizationKYC(id = organizationID, documentType = documentType, status = None, fileName = name, file = None),
          updateOldDocument = masterOrganizationKYCs.Service.updateOldDocument
        )
        withUsernameToken.PartialContent(views.html.component.master.userUploadOrUpdateOrganizationKYC(masterOrganizationKYCs.Service.getAllDocuments(organizationID)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def reviewAddOrganizationOnCompletionForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val organization = masterOrganizations.Service.getByAccountID(loginState.username)
        withUsernameToken.Ok(views.html.component.master.reviewAddOrganizationOnCompletion(organization = organization, zone = masterZones.Service.get(organization.zoneID), organizationBankAccountDetail = masterOrganizationBankAccountDetails.Service.get(organization.id), organizationKYCs = masterOrganizationKYCs.Service.getAllDocuments(organization.id)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def reviewAddOrganizationOnCompletion(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ReviewAddOrganizationOnCompletion.form.bindFromRequest().fold(
        formWithErrors => {
          try {
            val organization = masterOrganizations.Service.getByAccountID(loginState.username)
            BadRequest(views.html.component.master.reviewAddOrganizationOnCompletion(formWithErrors, organization = organization, zone = masterZones.Service.get(organization.zoneID), organizationBankAccountDetail = masterOrganizationBankAccountDetails.Service.get(organization.id), organizationKYCs = masterOrganizationKYCs.Service.getAllDocuments(organization.id)))
          } catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        reviewAddOrganizationOnCompletionData => {
          try {
            val id = masterOrganizations.Service.getID(loginState.username)
            if (reviewAddOrganizationOnCompletionData.completion && masterOrganizationKYCs.Service.checkAllKYCFileTypesExists(id)) {
              masterOrganizations.Service.markOrganizationFormCompleted(id)
              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ORGANIZATION_ADDED_FOR_VERIFICATION)))
            } else {
              val organization = masterOrganizations.Service.getByAccountID(loginState.username)
              BadRequest(views.html.component.master.reviewAddOrganizationOnCompletion(organization = organization, zone = masterZones.Service.get(organization.zoneID), organizationBankAccountDetail = masterOrganizationBankAccountDetails.Service.get(organization.id), organizationKYCs = masterOrganizationKYCs.Service.getAllDocuments(organization.id)))
            }
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def verifyOrganizationForm(organizationID: String, zoneID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.verifyOrganization(organizationID = organizationID, zoneID = zoneID))
  }

  def verifyOrganization: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyOrganization.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.verifyOrganization(formWithErrors, formWithErrors.data(constants.Form.ORGANIZATION_ID), formWithErrors.data(constants.Form.ZONE_ID)))
        },
        verifyOrganizationData => {
          try {
            if (masterOrganizationKYCs.Service.checkAllKYCFilesVerified(verifyOrganizationData.organizationID)) {
              val organizationAccountAddress = masterAccounts.Service.getAddress(masterOrganizations.Service.getAccountId(verifyOrganizationData.organizationID))
              transaction.process[AddOrganization, transactionsAddOrganization.Request](
                entity = AddOrganization(from = loginState.address, to = organizationAccountAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, gas = verifyOrganizationData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionAddOrganizations.Service.create,
                request = transactionsAddOrganization.Request(transactionsAddOrganization.BaseReq(from = loginState.address, gas = verifyOrganizationData.gas.toString), to = organizationAccountAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, password = verifyOrganizationData.password, mode = transactionMode),
                action = transactionsAddOrganization.Service.post,
                onSuccess = blockchainTransactionAddOrganizations.Utility.onSuccess,
                onFailure = blockchainTransactionAddOrganizations.Utility.onFailure,
                updateTransactionHash = blockchainTransactionAddOrganizations.Service.updateTransactionHash
              )
              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ORGANIZATION_VERIFIED)))
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

  def viewKYCDocuments(organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.viewVerificationOrganizationKYCDouments(masterOrganizationKYCs.Service.getAllDocuments(organizationID)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateOrganizationKYCDocumentStatusForm(organizationID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        if (masterZones.Service.getID(loginState.username) == masterOrganizations.Service.getZoneID(organizationID)) {
          withUsernameToken.Ok(views.html.component.master.updateOrganizationKYCDocumentStatus(organizationKYC = masterOrganizationKYCs.Service.get(id = organizationID, documentType = documentType)))
        }
        else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateOrganizationKYCDocumentStatus(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.UpdateOrganizationKYCDocumentStatus.form.bindFromRequest().fold(
        formWithErrors => {
          try {
            BadRequest(views.html.component.master.updateOrganizationKYCDocumentStatus(formWithErrors, masterOrganizationKYCs.Service.get(id = formWithErrors(constants.FormField.ORGANIZATION_ID.name).value.get, documentType = formWithErrors(constants.FormField.DOCUMENT_TYPE.name).value.get)))
          } catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        updateOrganizationKYCDocumentStatusData => {
          try {
            if (masterZones.Service.getID(loginState.username) == masterOrganizations.Service.getZoneID(updateOrganizationKYCDocumentStatusData.organizationID)) {
              if (updateOrganizationKYCDocumentStatusData.status) {
                masterOrganizationKYCs.Service.verify(id = updateOrganizationKYCDocumentStatusData.organizationID, documentType = updateOrganizationKYCDocumentStatusData.documentType)
                utilitiesNotification.send(masterOrganizations.Service.getAccountId(updateOrganizationKYCDocumentStatusData.organizationID), constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
              } else {
                masterOrganizationKYCs.Service.reject(id = updateOrganizationKYCDocumentStatusData.organizationID, documentType = updateOrganizationKYCDocumentStatusData.documentType)
                utilitiesNotification.send(masterOrganizations.Service.getAccountId(updateOrganizationKYCDocumentStatusData.organizationID), constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
              }
              withUsernameToken.PartialContent(views.html.component.master.updateOrganizationKYCDocumentStatus(organizationKYC = masterOrganizationKYCs.Service.get(updateOrganizationKYCDocumentStatusData.organizationID, updateOrganizationKYCDocumentStatusData.documentType)))
            } else {
              Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
            }
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def rejectVerifyOrganizationRequestForm(organizationID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.rejectVerifyOrganizationRequest(organizationID = organizationID))
  }

  def rejectVerifyOrganizationRequest(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RejectVerifyOrganizationRequest.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.rejectVerifyOrganizationRequest(formWithErrors, formWithErrors.data(constants.Form.ORGANIZATION_ID)))
        },
        rejectVerifyOrganizationRequestData => {
          try {
            masterOrganizations.Service.rejectOrganization(rejectVerifyOrganizationRequestData.organizationID)
            masterOrganizationKYCs.Service.rejectAll(masterOrganizations.Service.getAccountId(rejectVerifyOrganizationRequestData.organizationID))
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.VERIFY_ORGANIZATION_REQUEST_REJECTED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def viewOrganizationVerificationBankAccountDetail(organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.viewOrganizationBankAccountDetail(masterOrganizationBankAccountDetails.Service.get(organizationID)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def viewPendingVerifyOrganizationRequests: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.viewPendingVerifyOrganizationRequests(masterOrganizations.Service.getVerifyOrganizationRequests(masterZones.Service.getID(loginState.username))))
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def uploadOrganizationKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.uploadOrganizationKYC), utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.storeOrganizationKYC), documentType))
  }

  def updateOrganizationKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.uploadOrganizationKYC), utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.updateOrganizationKYC), documentType))
  }

  def uploadOrganizationKYC(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.index(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getOrganizationKYCFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeOrganizationKYC(name: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.storeFile[master.OrganizationKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getOrganizationKYCFilePath(documentType),
          document = master.OrganizationKYC(id = masterOrganizations.Service.getID(loginState.username), documentType = documentType, status = None, fileName = name, file = None),
          masterCreate = masterOrganizationKYCs.Service.create
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateOrganizationKYC(name: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val organizationID = masterOrganizations.Service.getID(loginState.username)
        fileResourceManager.updateFile[master.OrganizationKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getOrganizationKYCFilePath(documentType),
          oldDocumentFileName = masterOrganizationKYCs.Service.getFileName(id = organizationID, documentType = documentType),
          document = master.OrganizationKYC(id = organizationID, documentType = documentType, status = None, fileName = name, file = None),
          updateOldDocument = masterOrganizationKYCs.Service.updateOldDocument
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def viewOrganizationsInZone: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.viewOrganizationsInZone(masterOrganizations.Service.getOrganizationsInZone(masterZones.Service.getID(loginState.username))))
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def viewOrganizationsInZoneForGenesis(zoneID: String): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.viewOrganizationsInZoneForGenesis(masterOrganizations.Service.getOrganizationsInZone(zoneID)))
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def blockchainAddOrganizationForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.addOrganization())
  }

  def blockchainAddOrganization: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.AddOrganization.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.addOrganization(formWithErrors))
      },
      addOrganizationData => {
        try {
          transactionsAddOrganization.Service.post(transactionsAddOrganization.Request(transactionsAddOrganization.BaseReq(from = addOrganizationData.from, gas = addOrganizationData.gas.toString), to = addOrganizationData.to, organizationID = addOrganizationData.organizationID, zoneID = addOrganizationData.zoneID, password = addOrganizationData.password, mode = transactionMode))
          Ok(views.html.index(successes = Seq(constants.Response.ORGANIZATION_ADDED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}