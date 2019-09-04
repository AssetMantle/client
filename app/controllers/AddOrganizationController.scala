package controllers

import java.nio.file.Files
import java.sql.Date

import controllers.actions.{WithGenesisLoginAction, WithUserLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.blockchainTransaction.AddOrganization
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json._
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import utilities.PushNotification
import views.companion.master.FileUpload

import scala.concurrent.ExecutionContext

@Singleton
class AddOrganizationController @Inject()(messagesControllerComponents: MessagesControllerComponents, fileResourceManager: utilities.FileResourceManager, transaction: utilities.Transaction, masterOrganizationBankAccountDetails: master.OrganizationBankAccountDetails, pushNotification: PushNotification, blockchainAccounts: blockchain.Accounts, masterOrganizationKYCs: master.OrganizationKYCs, masterTraders: master.Traders, transactionsAddOrganization: transactions.AddOrganization, blockchainOrganizations: blockchain.Organizations, masterZones: master.Zones, blockchainTransactionAddOrganizations: blockchainTransaction.AddOrganizations, masterOrganizations: master.Organizations, masterAccounts: master.Accounts, withUserLoginAction: WithUserLoginAction, withZoneLoginAction: WithZoneLoginAction, withGenesisLoginAction: WithGenesisLoginAction, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_ADD_ORGANIZATION

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val addressWrites: OWrites[master.Address] = Json.writes[master.Address]

  private implicit val addressReads: Reads[master.Address] = Json.reads[master.Address]

  def addOrganizationForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val organization = masterOrganizations.Service.getByAccountID(loginState.username)
        val registeredAddress = utilities.JSON.getInstance(organization.registeredAddress)
        val postalAddress = utilities.JSON.getInstance(organization.postalAddress)
        Ok(views.html.component.master.addOrganization(views.companion.master.AddOrganization.form, zoneID = organization.zoneID, name = organization.name, abbreviation = organization.abbreviation.getOrElse(""), registeredAddressLine1 = registeredAddress.addressLine1, registeredAddressLine2 = registeredAddress.addressLine2, registeredLandmark = registeredAddress.landmark.getOrElse(""), registeredCity = registeredAddress.city, registeredCountry = registeredAddress.country, registeredZipCode = registeredAddress.zipCode, registeredPhone = registeredAddress.phone, postalAddressLine1 = postalAddress.addressLine1, postalAddressLine2 = postalAddress.addressLine2, postalLandmark = postalAddress.landmark.getOrElse(""), postalCity = postalAddress.city, postalCountry = postalAddress.country, postalZipCode = postalAddress.zipCode, postalPhone = postalAddress.phone, email = organization.email))
      } catch {
        case _: BaseException => Ok(views.html.component.master.addOrganization(views.companion.master.AddOrganization.form, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""))
      }
  }

  def addOrganization(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddOrganization.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.addOrganization(formWithErrors, zoneID = formWithErrors.data(constants.FormField.ZONE_ID.name), name = formWithErrors.data(constants.FormField.NAME.name), abbreviation = formWithErrors.data(constants.FormField.ABBREVIATION.name), registeredAddressLine1 = formWithErrors.data(constants.FormField.REGISTERED_ADDRESS_LINE_1.name), registeredAddressLine2 = formWithErrors.data(constants.FormField.REGISTERED_ADDRESS_LINE_2.name), registeredLandmark = formWithErrors.data(constants.FormField.REGISTERED_LANDMARK.name), registeredCity = formWithErrors.data(constants.FormField.REGISTERED_CITY.name), registeredCountry = formWithErrors.data(constants.FormField.REGISTERED_COUNTRY.name), registeredZipCode = formWithErrors.data(constants.FormField.REGISTERED_ZIP_CODE.name), registeredPhone = formWithErrors.data(constants.FormField.REGISTERED_PHONE.name), postalAddressLine1 = formWithErrors.data(constants.FormField.POSTAL_ADDRESS_LINE_1.name), postalAddressLine2 = formWithErrors.data(constants.FormField.POSTAL_ADDRESS_LINE_2.name), postalLandmark = formWithErrors.data(constants.FormField.POSTAL_LANDMARK.name), postalCity = formWithErrors.data(constants.FormField.POSTAL_CITY.name), postalCountry = formWithErrors.data(constants.FormField.POSTAL_COUNTRY.name), postalZipCode = formWithErrors.data(constants.FormField.POSTAL_ZIP_CODE.name), postalPhone = formWithErrors.data(constants.FormField.POSTAL_PHONE.name), email = formWithErrors.data(constants.FormField.EMAIL_ADDRESS.name)))
        },
        addOrganizationData => {
          try {
            if (masterZones.Service.getStatus(addOrganizationData.zoneID) == Option(true)) {
              val id = masterOrganizations.Service.insertOrUpdate(zoneID = addOrganizationData.zoneID, accountID = loginState.username, name = addOrganizationData.name, abbreviation = Option(addOrganizationData.abbreviation), establishmentDate = new Date(addOrganizationData.establishmentDate.getTime), registeredAddress = Json.toJson(master.Address(addressLine1 = addOrganizationData.registeredAddressLine1, addressLine2 = addOrganizationData.registeredAddressLine2, landmark = Option(addOrganizationData.registeredAddressLandmark), city = addOrganizationData.registeredAddressCity, country = addOrganizationData.registeredAddressCountry, zipCode = addOrganizationData.registeredAddressZipCode, phone = addOrganizationData.registeredAddressPhone)).toString(), postalAddress = Json.toJson(master.Address(addressLine1 = addOrganizationData.postalAddressLine1, addressLine2 = addOrganizationData.postalAddressLine2, landmark = Option(addOrganizationData.postalAddressLandmark), city = addOrganizationData.postalAddressCity, country = addOrganizationData.postalAddressCountry, zipCode = addOrganizationData.postalAddressZipCode, phone = addOrganizationData.postalAddressPhone)).toString(), email = addOrganizationData.email, ubo = None)
              try {
                val organizationBankAccountDetail = masterOrganizationBankAccountDetails.Service.get(id)
                PartialContent(views.html.component.master.organizationBankAccountDetail(views.companion.master.OrganizationBankAccountDetail.form, accountHolderName = organizationBankAccountDetail.accountHolder, nickName = organizationBankAccountDetail.nickName, accountNumber = organizationBankAccountDetail.accountNumber, bankName = organizationBankAccountDetail.bankName, swiftAddress = organizationBankAccountDetail.swiftAddress, address = organizationBankAccountDetail.address, country = organizationBankAccountDetail.country, zipCode = organizationBankAccountDetail.zipCode))
              } catch {
                case _: BaseException => PartialContent(views.html.component.master.organizationBankAccountDetail(views.companion.master.OrganizationBankAccountDetail.form, "", "", "", "", "", "", "", ""))
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

  def organizationBankAccountDetail(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.OrganizationBankAccountDetail.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.organizationBankAccountDetail(formWithErrors, accountHolderName = formWithErrors.data(constants.FormField.ACCOUNT_HOLDER_NAME.name), nickName = formWithErrors.data(constants.FormField.NICK_NAME.name), accountNumber = formWithErrors.data(constants.FormField.ACCOUNT_NUMBER.name), bankName = formWithErrors.data(constants.FormField.BANK_NAME.name), swiftAddress = formWithErrors.data(constants.FormField.SWIFT_ADDRESS.name), address = formWithErrors.data(constants.FormField.ADDRESS.name), country = formWithErrors.data(constants.FormField.COUNTRY.name), zipCode = formWithErrors.data(constants.FormField.ZIP_CODE.name)))
        },
        organizationBankAccountDetailData => {
          try {
            val id = masterOrganizations.Service.getID(loginState.username)
            masterOrganizationBankAccountDetails.Service.insertOrUpdate(id = id, accountHolder = organizationBankAccountDetailData.accountHolder, nickName = organizationBankAccountDetailData.nickName, accountNumber = organizationBankAccountDetailData.bankAccountNumber, bankName = organizationBankAccountDetailData.bankName, swiftAddress = organizationBankAccountDetailData.swiftAddress, address = organizationBankAccountDetailData.address, country = organizationBankAccountDetailData.country, zipCode = organizationBankAccountDetailData.zipCode)
            PartialContent(views.html.component.master.organizationKYC(masterOrganizationKYCs.Service.getAllDocuments(id)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def reviewOrganizationKYC(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.OrganizationAgreement.form.bindFromRequest().fold(
        formWithErrors => {
          try {
            val organization = masterOrganizations.Service.getByAccountID(loginState.username)
            BadRequest(views.html.component.master.reveiewOrganizationKYC(formWithErrors, organization = organization, registeredAddress = utilities.JSON.getInstance[master.Address](organization.registeredAddress), postalAddress = utilities.JSON.getInstance[master.Address](organization.postalAddress), zone = masterZones.Service.get(organization.zoneID), organizationBankAccountDetail = masterOrganizationBankAccountDetails.Service.get(organization.id), organizationKYCs = masterOrganizationKYCs.Service.getAllDocuments(loginState.username)))
          } catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        reveiewOrganizationKYCData => {
          try {
            Ok
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def uploadUserOrganizationKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFileForm(utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.uploadUserOrganizationKyc), utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.storeUserOrganizationKyc), documentType))
  }

  def uploadUserOrganizationKyc(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errors.mkString("\n"))
      },
      fileUploadInfo => {
        try {
          request.body.file("file") match {
            case None => BadRequest(Messages(constants.Response.NO_FILE.message))
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

  def storeUserOrganizationKyc(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
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
        PartialContent(views.html.component.master.organizationKYC(masterOrganizationKYCs.Service.getAllDocuments(id)))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def updateUserOrganizationKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFileForm(utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.uploadUserOrganizationKyc), utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.updateUserOrganizationKyc), documentType))
  }

  def updateUserOrganizationKyc(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
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
        PartialContent(views.html.component.master.organizationKYC(masterOrganizationKYCs.Service.getAllDocuments(id)))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
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
            val organizationAccountAddress = masterAccounts.Service.getAddress(masterOrganizations.Service.getAccountId(verifyOrganizationData.organizationID))
            transaction.process[AddOrganization, transactionsAddOrganization.Request](
              entity = AddOrganization(from = loginState.address, to = organizationAccountAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, gas = verifyOrganizationData.gas, ticketID = "", mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionAddOrganizations.Service.create,
              request = transactionsAddOrganization.Request(transactionsAddOrganization.BaseRequest(from = loginState.address, gas = verifyOrganizationData.gas.toString), to = organizationAccountAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, password = verifyOrganizationData.password, mode = transactionMode),
              action = transactionsAddOrganization.Service.post,
              onSuccess = blockchainTransactionAddOrganizations.Utility.onSuccess,
              onFailure = blockchainTransactionAddOrganizations.Utility.onFailure,
              updateTransactionHash = blockchainTransactionAddOrganizations.Service.updateTransactionHash
            )
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ORGANIZATION_VERIFIED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
          }
        }
      )
  }

  def viewKycDocuments(accountID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.viewVerificationOrganizationKycDouments(masterOrganizationKYCs.Service.getAllDocuments(accountID)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def verifyKycDocument(accountID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        masterOrganizationKYCs.Service.verify(id = accountID, documentType = documentType)
        pushNotification.sendNotification(accountID, constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
        withUsernameToken.Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def rejectKycDocument(accountID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        masterOrganizationKYCs.Service.reject(id = accountID, documentType = documentType)
        pushNotification.sendNotification(accountID, constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
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
            masterOrganizations.Service.updateStatus(rejectVerifyOrganizationRequestData.organizationID, status = false)
            masterOrganizationKYCs.Service.rejectAll(masterOrganizations.Service.getAccountId(rejectVerifyOrganizationRequestData.organizationID))
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.VERIFY_ORGANIZATION_REQUEST_REJECTED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
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
          transactionsAddOrganization.Service.post(transactionsAddOrganization.Request(transactionsAddOrganization.BaseRequest(from = addOrganizationData.from, gas = addOrganizationData.gas.toString), to = addOrganizationData.to, organizationID = addOrganizationData.organizationID, zoneID = addOrganizationData.zoneID, password = addOrganizationData.password, mode = transactionMode))
          Ok(views.html.index(successes = Seq(constants.Response.ORGANIZATION_ADDED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }
}