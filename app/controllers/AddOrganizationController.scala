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
        Ok(views.html.component.master.addOrganization(views.companion.master.AddOrganization.form.fill(value = views.companion.master.AddOrganization.Data(zoneID = organization.zoneID, name = organization.name, abbreviation = organization.abbreviation, establishmentDate = utilities.Date.sqlDateToUtilDate(organization.establishmentDate), email = organization.email, registeredAddress = views.companion.master.AddOrganization.AddressData(addressLine1 = organization.registeredAddress.addressLine1, addressLine2 = organization.registeredAddress.addressLine2, Landmark = organization.registeredAddress.landmark, City = organization.registeredAddress.city, Country = organization.registeredAddress.country, ZipCode = organization.registeredAddress.zipCode, Phone = organization.registeredAddress.phone), postalAddress = views.companion.master.AddOrganization.AddressData(addressLine1 = organization.postalAddress.addressLine1, addressLine2 = organization.postalAddress.addressLine2, Landmark = organization.postalAddress.landmark, City = organization.postalAddress.city, Country = organization.postalAddress.country, ZipCode = organization.postalAddress.zipCode, Phone = organization.postalAddress.phone))), zones = masterZones.Service.getAll))
      } catch {
        case _: BaseException => Ok(views.html.component.master.addOrganization(views.companion.master.AddOrganization.form, zones = masterZones.Service.getAll))
      }
  }

  def addOrganization(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddOrganization.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.addOrganization(formWithErrors, zones = masterZones.Service.getAll))
        },
        addOrganizationData => {
          try {
            if (masterZones.Service.getVerificationStatus(addOrganizationData.zoneID)) {
              val id = masterOrganizations.Service.insertOrUpdateOrganizationWithoutUBOs(zoneID = addOrganizationData.zoneID, accountID = loginState.username, name = addOrganizationData.name, abbreviation = addOrganizationData.abbreviation, establishmentDate = utilities.Date.utilDateToSQLDate(addOrganizationData.establishmentDate), email = addOrganizationData.email, registeredAddress = Address(addressLine1 = addOrganizationData.registeredAddress.addressLine1, addressLine2 = addOrganizationData.registeredAddress.addressLine2, landmark = addOrganizationData.registeredAddress.Landmark, city = addOrganizationData.registeredAddress.City, country = addOrganizationData.registeredAddress.Country, zipCode = addOrganizationData.registeredAddress.ZipCode, phone = addOrganizationData.registeredAddress.Phone), postalAddress = Address(addressLine1 = addOrganizationData.postalAddress.addressLine1, addressLine2 = addOrganizationData.postalAddress.addressLine2, landmark = addOrganizationData.postalAddress.Landmark, city = addOrganizationData.postalAddress.City, country = addOrganizationData.postalAddress.Country, zipCode = addOrganizationData.postalAddress.ZipCode, phone = addOrganizationData.postalAddress.Phone))
              try {
                PartialContent(views.html.component.master.userUpdateUBOs(views.companion.master.AddUBOs.form.fill(views.companion.master.AddUBOs.Data(masterOrganizations.Service.getUBOs(id).data.map(ubo => Option(views.companion.master.AddUBOs.UBOData(personName = ubo.personName, sharePercentage = ubo.sharePercentage, relationship = ubo.relationship, title = ubo.title)))))))
              } catch {
                case _: BaseException => PartialContent(views.html.component.master.userUpdateUBOs(views.companion.master.AddUBOs.form))
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
        Ok(views.html.component.master.userUpdateUBOs(views.companion.master.AddUBOs.form.fill(views.companion.master.AddUBOs.Data(masterOrganizations.Service.getUBOs(masterOrganizations.Service.getID(loginState.username)).data.map(ubo => Option(views.companion.master.AddUBOs.UBOData(personName = ubo.personName, sharePercentage = ubo.sharePercentage, relationship = ubo.relationship, title = ubo.title)))))))
      } catch {
        case _: BaseException => Ok(views.html.component.master.userUpdateUBOs(views.companion.master.AddUBOs.form))
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
            PartialContent(views.html.component.master.userUpdateUBOs(views.companion.master.AddUBOs.form.fill(views.companion.master.AddUBOs.Data(masterOrganizations.Service.getUBOs(id).data.map(ubo => Option(views.companion.master.AddUBOs.UBOData(personName = ubo.personName, sharePercentage = ubo.sharePercentage, relationship = ubo.relationship, title = ubo.title)))))))
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
        Ok(views.html.component.master.organizationBankAccountDetail(views.companion.master.OrganizationBankAccountDetail.form.fill(views.companion.master.OrganizationBankAccountDetail.Data(accountHolder = organizationBankAccountDetail.accountHolder, nickName = organizationBankAccountDetail.nickName, accountNumber = organizationBankAccountDetail.accountNumber, bankName = organizationBankAccountDetail.bankName, swiftAddress = organizationBankAccountDetail.swiftAddress, address = organizationBankAccountDetail.address, country = organizationBankAccountDetail.country, zipCode = organizationBankAccountDetail.zipCode))))
      } catch {
        case _: BaseException => Ok(views.html.component.master.organizationBankAccountDetail(views.companion.master.OrganizationBankAccountDetail.form))
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
            PartialContent(views.html.component.master.userUploadOrUpdateOrganizationKYC(masterOrganizationKYCs.Service.getAllDocuments(id)))
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

  def userUploadOrganizationKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFileForm(utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.userUploadOrganizationKyc), utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.userStoreOrganizationKyc), documentType))
  }

  def userUploadOrganizationKyc(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.index(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getOrganizationKycFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def userStoreOrganizationKyc(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val id = masterOrganizations.Service.getID(loginState.username)
        fileResourceManager.storeFile[master.OrganizationKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getOrganizationKycFilePath(documentType),
          document = master.OrganizationKYC(id = id, documentType = documentType, status = None, fileName = name, file = None),
          masterCreate = masterOrganizationKYCs.Service.create
        )
        PartialContent(views.html.component.master.userUploadOrUpdateOrganizationKYC(masterOrganizationKYCs.Service.getAllDocuments(id)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userUpdateOrganizationKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFileForm(utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.userUploadOrganizationKyc), utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.userUpdateOrganizationKyc), documentType))
  }

  def userUpdateOrganizationKyc(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val id = masterOrganizations.Service.getID(loginState.username)
        fileResourceManager.updateFile[master.OrganizationKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getOrganizationKycFilePath(documentType),
          oldDocumentFileName = masterOrganizationKYCs.Service.getFileName(id = id, documentType = documentType),
          document = master.OrganizationKYC(id = id, documentType = documentType, status = None, fileName = name, file = None),
          updateOldDocument = masterOrganizationKYCs.Service.updateOldDocument
        )
        PartialContent(views.html.component.master.userUploadOrUpdateOrganizationKYC(masterOrganizationKYCs.Service.getAllDocuments(id)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userAccessedOrganizationKYCFile(documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getOrganizationKycFilePath(documentType), fileName = masterOrganizationKYCs.Service.getFileName(id = masterOrganizations.Service.getID(loginState.username), documentType = documentType)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def reviewOrganizationCompletionForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val organization = masterOrganizations.Service.getByAccountID(loginState.username)
        Ok(views.html.component.master.reviewOrganizationCompletion(views.companion.master.OrganizationCompletion.form, organization = organization, zone = masterZones.Service.get(organization.zoneID), organizationBankAccountDetail = masterOrganizationBankAccountDetails.Service.get(organization.id), organizationKYCs = masterOrganizationKYCs.Service.getAllDocuments(organization.id)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def reviewOrganizationCompletion(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.OrganizationCompletion.form.bindFromRequest().fold(
        formWithErrors => {
          try {
            val organization = masterOrganizations.Service.getByAccountID(loginState.username)
            BadRequest(views.html.component.master.reviewOrganizationCompletion(formWithErrors, organization = organization, zone = masterZones.Service.get(organization.zoneID), organizationBankAccountDetail = masterOrganizationBankAccountDetails.Service.get(organization.id), organizationKYCs = masterOrganizationKYCs.Service.getAllDocuments(organization.id)))
          } catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        reviewOrganizationCompletionData => {
          try {
            val id = masterOrganizations.Service.getID(loginState.username)
            if (reviewOrganizationCompletionData.completion && masterOrganizationKYCs.Service.checkAllKYCFileTypesExists(id)) {
              masterOrganizations.Service.markOrganizationFormCompleted(id)
              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ORGANIZATION_ADDED_FOR_VERIFICATION)))
            } else {
              val organization = masterOrganizations.Service.getByAccountID(loginState.username)
              BadRequest(views.html.component.master.reviewOrganizationCompletion(views.companion.master.OrganizationCompletion.form, organization = organization, zone = masterZones.Service.get(organization.zoneID), organizationBankAccountDetail = masterOrganizationBankAccountDetails.Service.get(organization.id), organizationKYCs = masterOrganizationKYCs.Service.getAllDocuments(organization.id)))
            }
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def verifyOrganizationForm(organizationID: String, zoneID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.verifyOrganization(views.companion.master.VerifyOrganization.form, organizationID, zoneID))
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

  def viewKycDocuments(organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.viewVerificationOrganizationKycDouments(masterOrganizationKYCs.Service.getAllDocuments(organizationID)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def verifyKycDocument(organizationID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        masterOrganizationKYCs.Service.verify(id = organizationID, documentType = documentType)
        utilitiesNotification.send(accountID = masterOrganizations.Service.getAccountId(organizationID), notification = constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
        withUsernameToken.Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def rejectKycDocument(organizationID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        masterOrganizationKYCs.Service.reject(id = organizationID, documentType = documentType)
        utilitiesNotification.send(accountID = masterOrganizations.Service.getAccountId(organizationID), notification = constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
        withUsernameToken.Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def rejectVerifyOrganizationRequestForm(organizationID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.rejectVerifyOrganizationRequest(views.companion.master.RejectVerifyOrganizationRequest.form, organizationID))
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
        withUsernameToken.Ok(views.html.component.master.viewVerificationOrganizationBankAccountDetail(masterOrganizationBankAccountDetails.Service.get(organizationID)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def viewPendingVerifyOrganizationRequests: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.viewPendingVerifyOrgnizationRequests(masterOrganizations.Service.getVerifyOrganizationRequests(masterZones.Service.getZoneId(loginState.username))))
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def uploadOrganizationKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFileForm(utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.uploadOrganizationKyc), utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.storeOrganizationKyc), documentType))
  }

  def updateOrganizationKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFileForm(utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.uploadOrganizationKyc), utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.updateOrganizationKyc), documentType))
  }

  def uploadOrganizationKyc(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.index(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getOrganizationKycFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeOrganizationKyc(name: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.storeFile[master.OrganizationKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getOrganizationKycFilePath(documentType),
          document = master.OrganizationKYC(id = masterOrganizations.Service.getID(loginState.username), documentType = documentType, status = None, fileName = name, file = None),
          masterCreate = masterOrganizationKYCs.Service.create
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateOrganizationKyc(name: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val id = masterOrganizations.Service.getID(loginState.username)
        fileResourceManager.updateFile[master.OrganizationKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getOrganizationKycFilePath(documentType),
          oldDocumentFileName = masterOrganizationKYCs.Service.getFileName(id = id, documentType = documentType),
          document = master.OrganizationKYC(id = id, documentType = documentType, status = None, fileName = name, file = None),
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
        Ok(views.html.component.master.viewOrganizationsInZone(masterOrganizations.Service.getOrganizationsInZone(masterZones.Service.getZoneId(loginState.username))))
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
    Ok(views.html.component.blockchain.addOrganization(views.companion.blockchain.AddOrganization.form))
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