package controllers

import java.nio.file.Files

import controllers.actions._
import controllers.logging.{WithActionAsyncLoggingFilter, WithActionLoggingFilter}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchainTransaction.AddOrganization
import models.common.Serializable._
import models.master._
import models.{blockchain, blockchainTransaction, master, memberCheck}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import play.api.{Configuration, Logger}
import views.companion.master.FileUpload

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddOrganizationController @Inject()(
                                           messagesControllerComponents: MessagesControllerComponents,
                                           masterAccounts: master.Accounts,
                                           withOrganizationLoginAction: WithOrganizationLoginAction,
                                           fileResourceManager: utilities.FileResourceManager,
                                           transaction: utilities.Transaction,
                                           masterOrganizationBankAccountDetails: master.OrganizationBankAccountDetails,
                                           utilitiesNotification: utilities.Notification,
                                           masterOrganizationKYCs: master.OrganizationKYCs,
                                           masterOrganizationUBOs: master.OrganizationUBOs,
                                           memberCheckCorporateScanResultDecisions: memberCheck.CorporateScanDecisions,
                                           transactionsAddOrganization: transactions.AddOrganization,
                                           masterZones: master.Zones,
                                           masterIdentifications: master.Identifications,
                                           masterEmails: master.Emails,
                                           masterMobiles: master.Mobiles,
                                           blockchainAccounts: blockchain.Accounts,
                                           blockchainTransactionAddOrganizations: blockchainTransaction.AddOrganizations,
                                           masterOrganizations: master.Organizations,
                                           withUserLoginAction: WithUserLoginAction,
                                           withZoneLoginAction: WithZoneLoginAction,
                                           withUsernameToken: WithUsernameToken,
                                           withoutLoginAction: WithoutLoginAction,
                                           withoutLoginActionAsync: WithoutLoginActionAsync,
                                         )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_ADD_ORGANIZATION

  private implicit val logger: Logger = Logger(this.getClass)

  def addOrganizationForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organization = masterOrganizations.Service.getByAccountID(loginState.username)
      val zones = masterZones.Service.getAllVerified

      def getResult(organization: Option[Organization], zones: Seq[Zone]): Future[Result] = {
        organization match {
          case Some(organization) => {
            withUsernameToken.Ok(views.html.component.master.addOrganization(views.companion.master.AddOrganization.form.fill(value = views.companion.master.AddOrganization.Data(zoneID = organization.zoneID, name = organization.name, abbreviation = organization.abbreviation, establishmentDate = utilities.Date.sqlDateToUtilDate(organization.establishmentDate), email = organization.email, registeredAddress = views.companion.master.AddOrganization.AddressData(addressLine1 = organization.registeredAddress.addressLine1, addressLine2 = organization.registeredAddress.addressLine2, landmark = organization.registeredAddress.landmark, city = organization.registeredAddress.city, country = organization.registeredAddress.country, zipCode = organization.registeredAddress.zipCode, phone = organization.registeredAddress.phone), postalAddress = views.companion.master.AddOrganization.AddressData(addressLine1 = organization.postalAddress.addressLine1, addressLine2 = organization.postalAddress.addressLine2, landmark = organization.postalAddress.landmark, city = organization.postalAddress.city, country = organization.postalAddress.country, zipCode = organization.postalAddress.zipCode, phone = organization.postalAddress.phone))), zones = zones))
          }
          case None => {
            withUsernameToken.Ok(views.html.component.master.addOrganization(views.companion.master.AddOrganization.form, zones = zones))
          }
        }
      }

      (for {
        organization <- organization
        zones <- zones
        result <- getResult(organization = organization, zones = zones)
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def addOrganization(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddOrganization.form.bindFromRequest().fold(
        formWithErrors => {
          val zones = masterZones.Service.getAllVerified
          for {
            zones <- zones
          } yield BadRequest(views.html.component.master.addOrganization(formWithErrors, zones = zones))
        },
        addOrganizationData => {
          val verificationStatus = masterZones.Service.getVerificationStatus(addOrganizationData.zoneID)
          val identification = masterIdentifications.Service.tryGet(loginState.username)
          val email = masterEmails.Service.tryGet(loginState.username)
          val mobile = masterMobiles.Service.tryGet(loginState.username)

          def insertOrUpdateOrganizationWithoutUBOsAndGetResult(verificationStatus: Boolean, identification: Identification, email: Email, mobile: Mobile): Future[Result] = {
            if (!verificationStatus) throw new BaseException(constants.Response.UNVERIFIED_ZONE)
            else if (!identification.verificationStatus.getOrElse(false)) throw new BaseException(constants.Response.ACCOUNT_KYC_PENDING)
            else if (!email.status || !mobile.status) throw new BaseException(constants.Response.CONTACT_VERIFICATION_PENDING)
            else {
              val id = masterOrganizations.Service.insertOrUpdate(zoneID = addOrganizationData.zoneID, accountID = loginState.username, name = addOrganizationData.name, abbreviation = addOrganizationData.abbreviation, establishmentDate = utilities.Date.utilDateToSQLDate(addOrganizationData.establishmentDate), email = addOrganizationData.email, registeredAddress = Address(addressLine1 = addOrganizationData.registeredAddress.addressLine1, addressLine2 = addOrganizationData.registeredAddress.addressLine2, landmark = addOrganizationData.registeredAddress.landmark, city = addOrganizationData.registeredAddress.city, country = addOrganizationData.registeredAddress.country, zipCode = addOrganizationData.registeredAddress.zipCode, phone = addOrganizationData.registeredAddress.phone), postalAddress = Address(addressLine1 = addOrganizationData.postalAddress.addressLine1, addressLine2 = addOrganizationData.postalAddress.addressLine2, landmark = addOrganizationData.postalAddress.landmark, city = addOrganizationData.postalAddress.city, country = addOrganizationData.postalAddress.country, zipCode = addOrganizationData.postalAddress.zipCode, phone = addOrganizationData.postalAddress.phone))

              def getOrganizationKYCs(id: String): Future[Seq[OrganizationKYC]] = masterOrganizationKYCs.Service.getAllDocuments(id)

              for {
                id <- id
                organizationKYCs <- getOrganizationKYCs(id)
                result <- withUsernameToken.PartialContent(views.html.component.master.userUploadOrUpdateOrganizationKYC(organizationKYCs))
              } yield result
            }
          }

          (for {
            verificationStatus <- verificationStatus
            identification <- identification
            email <- email
            mobile <- mobile
            result <- insertOrUpdateOrganizationWithoutUBOsAndGetResult(verificationStatus, identification, email, mobile)
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def userAddUBOForm(): Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.userAddUBO())
  }

  def userAddUBO(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddUBO.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.userAddUBO(formWithErrors)))
        },
        userAddUBOData => {
          val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

          def getOldUBOs(organizationID: String): Future[Seq[OrganizationUBO]] = masterOrganizationUBOs.Service.getUBOs(organizationID)

          def createUBO(organizationID: String, oldUBOs: Seq[OrganizationUBO]): Future[String] = {
            if (oldUBOs.map(_.sharePercentage).sum + userAddUBOData.sharePercentage > 100.0) throw new BaseException(constants.Response.UBO_TOTAL_SHARE_PERCENTAGE_EXCEEDS_MAXIMUM_VALUE)
            masterOrganizationUBOs.Service.create(organizationID, userAddUBOData.personFirstName, userAddUBOData.personLastName, userAddUBOData.sharePercentage, userAddUBOData.relationship, userAddUBOData.title)
          }

          (for {
            organizationID <- organizationID
            oldUBOs <- getOldUBOs(organizationID)
            _ <- createUBO(organizationID, oldUBOs)
            result <- withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.UBO_ADDED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def addUBOForm(): Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.addUBO())
  }

  def addUBO(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddUBO.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.addUBO(formWithErrors)))
        },
        addUBOData => {
          val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

          def getOldUBOs(organizationID: String): Future[Seq[OrganizationUBO]] = masterOrganizationUBOs.Service.getUBOs(organizationID)

          def createUBO(organizationID: String, oldUBOs: Seq[OrganizationUBO]): Future[String] = {
            if (oldUBOs.map(_.sharePercentage).sum + addUBOData.sharePercentage > 100.0) throw new BaseException(constants.Response.UBO_TOTAL_SHARE_PERCENTAGE_EXCEEDS_MAXIMUM_VALUE)
            masterOrganizationUBOs.Service.create(organizationID, addUBOData.personFirstName, addUBOData.personLastName, addUBOData.sharePercentage, addUBOData.relationship, addUBOData.title)
          }

          (for {
            organizationID <- organizationID
            oldUBOs <- getOldUBOs(organizationID)
            _ <- createUBO(organizationID, oldUBOs)
            result <- withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.UBO_ADDED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def userDeleteUBOForm(id: String): Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.userDeleteUBO(views.companion.master.DeleteUBO.form.fill(views.companion.master.DeleteUBO.Data(id = id))))
  }

  def userDeleteUBO(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.DeleteUBO.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.userDeleteUBO(formWithErrors)))
        },
        userDeleteUBOData => {
          val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

          def deleteUBO(organizationID: String): Future[Int] = masterOrganizationUBOs.Service.delete(id = userDeleteUBOData.id, organizationID)

          (for {
            organizationID <- organizationID
            _ <- deleteUBO(organizationID)
            result <- withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.UBO_DELETED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def deleteUBOForm(id: String): Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.deleteUBO(views.companion.master.DeleteUBO.form.fill(views.companion.master.DeleteUBO.Data(id = id))))
  }

  def deleteUBO(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.DeleteUBO.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.deleteUBO(formWithErrors)))
        },
        deleteUBOData => {
          val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

          def deleteUBO(organizationID: String): Future[Int] = masterOrganizationUBOs.Service.delete(id = deleteUBOData.id, organizationID)

          (for {
            organizationID <- organizationID
            _ <- deleteUBO(organizationID)
            result <- withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.UBO_DELETED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def addOrUpdateOrganizationBankAccountForm(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOrganizationBankAccountDetail(organizationID: String): Future[OrganizationBankAccountDetail] = masterOrganizationBankAccountDetails.Service.tryGet(organizationID)

      (for {
        organizationID <- organizationID
        organizationBankAccountDetail <- getOrganizationBankAccountDetail(organizationID)
        result <- withUsernameToken.Ok(views.html.component.master.addOrUpdateOrganizationBankAccount(views.companion.master.AddOrUpdateOrganizationBankAccount.form.fill(views.companion.master.AddOrUpdateOrganizationBankAccount.Data(accountHolder = organizationBankAccountDetail.accountHolder, nickName = organizationBankAccountDetail.nickName, accountNumber = organizationBankAccountDetail.accountNumber, bankName = organizationBankAccountDetail.bankName, swiftAddress = organizationBankAccountDetail.swiftAddress, streetAddress = organizationBankAccountDetail.address, country = organizationBankAccountDetail.country, zipCode = organizationBankAccountDetail.zipCode))))
      } yield result
        ).recoverWith {
        case _: BaseException => withUsernameToken.Ok(views.html.component.master.addOrUpdateOrganizationBankAccount())
      }
  }

  def addOrUpdateOrganizationBankAccount(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddOrUpdateOrganizationBankAccount.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.addOrUpdateOrganizationBankAccount(formWithErrors)))
        },
        organizationBankAccountDetailData => {
          val id = masterOrganizations.Service.tryGetID(loginState.username)

          def insertOrUpdate(id: String): Future[Int] = masterOrganizationBankAccountDetails.Service.insertOrUpdate(id = id, accountHolder = organizationBankAccountDetailData.accountHolder, nickName = organizationBankAccountDetailData.nickName, accountNumber = organizationBankAccountDetailData.accountNumber, bankName = organizationBankAccountDetailData.bankName, swiftAddress = organizationBankAccountDetailData.swiftAddress, address = organizationBankAccountDetailData.streetAddress, country = organizationBankAccountDetailData.country, zipCode = organizationBankAccountDetailData.zipCode)

          (for {
            id <- id
            _ <- insertOrUpdate(id)
            result <- withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.BANK_ACCOUNT_DETAILS_UPDATED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def userUploadOrUpdateOrganizationKYCView(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val id = masterOrganizations.Service.tryGetID(loginState.username)

      def getOrganizationKYCs(id: String): Future[Seq[OrganizationKYC]] = masterOrganizationKYCs.Service.getAllDocuments(id)

      (for {
        id <- id
        organizationKYCs <- getOrganizationKYCs(id)
      } yield Ok(views.html.component.master.userUploadOrUpdateOrganizationKYC(organizationKYCs))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def userUploadOrganizationKYCForm(documentType: String): Action[AnyContent] = withoutLoginAction { implicit request =>
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
            case None => BadRequest(views.html.profile(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getOrganizationKYCFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def userStoreOrganizationKYC(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def storeFile(organizationID: String): Future[Unit] = fileResourceManager.storeFile[OrganizationKYC](
        name = name,
        path = fileResourceManager.getOrganizationKYCFilePath(documentType),
        document = OrganizationKYC(id = organizationID, documentType = documentType, status = None, fileName = name, file = None),
        masterCreate = masterOrganizationKYCs.Service.create
      )

      def getOrganizationKYCs(organizationID: String): Future[Seq[OrganizationKYC]] = masterOrganizationKYCs.Service.getAllDocuments(organizationID)

      (for {
        organizationID <- organizationID
        _ <- storeFile(organizationID)
        organizationKYCs <- getOrganizationKYCs(organizationID)
        result <- withUsernameToken.PartialContent(views.html.component.master.userUploadOrUpdateOrganizationKYC(organizationKYCs))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def userUpdateOrganizationKYCForm(documentType: String): Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.userUploadOrganizationKYC), utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.userUpdateOrganizationKYC), documentType))
  }

  def userUpdateOrganizationKYC(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOldDocument(organizationID: String): Future[OrganizationKYC] = masterOrganizationKYCs.Service.tryGet(id = organizationID, documentType = documentType)

      def updateFile(oldDocument: OrganizationKYC): Future[Boolean] = fileResourceManager.updateFile[OrganizationKYC](
        name = name,
        path = fileResourceManager.getOrganizationKYCFilePath(documentType),
        oldDocument = oldDocument,
        updateOldDocument = masterOrganizationKYCs.Service.updateOldDocument
      )

      def getOrganizationKYCs(id: String): Future[Seq[OrganizationKYC]] = masterOrganizationKYCs.Service.getAllDocuments(id)

      (for {
        organizationID <- organizationID
        oldDocument <- getOldDocument(organizationID)
        _ <- updateFile(oldDocument)
        organizationKYCs <- getOrganizationKYCs(organizationID)
        result <- withUsernameToken.PartialContent(views.html.component.master.userUploadOrUpdateOrganizationKYC(organizationKYCs))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def userReviewAddOrganizationRequestForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organization = masterOrganizations.Service.tryGetByAccountID(loginState.username)

      def getZoneOrganizationDetails(organization: Organization): Future[(Zone, Seq[OrganizationKYC])] = {
        val zone = masterZones.Service.tryGet(organization.zoneID)
        val organizationKYCs = masterOrganizationKYCs.Service.getAllDocuments(organization.id)
        for {
          zone <- zone
          organizationKYCs <- organizationKYCs
        } yield (zone, organizationKYCs)
      }

      (for {
        organization <- organization
        (zone, organizationKYCs) <- getZoneOrganizationDetails(organization)
        result <- withUsernameToken.Ok(views.html.component.master.userReviewAddOrganizationRequest(organization = organization, zone = zone, organizationKYCs = organizationKYCs))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def userReviewAddOrganizationRequest(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.UserReviewAddOrganizationRequest.form.bindFromRequest().fold(
        formWithErrors => {
          val organization = masterOrganizations.Service.tryGetByAccountID(loginState.username)

          def getZoneOrganizationDetails(organization: Organization): Future[(Zone, Seq[OrganizationKYC])] = {
            val zone = masterZones.Service.tryGet(organization.zoneID)
            val organizationKYCs = masterOrganizationKYCs.Service.getAllDocuments(organization.id)
            for {
              zone <- zone
              organizationKYCs <- organizationKYCs
            } yield (zone, organizationKYCs)
          }

          (for {
            organization <- organization
            (zone, organizationKYCs) <- getZoneOrganizationDetails(organization)
          } yield BadRequest(views.html.component.master.userReviewAddOrganizationRequest(formWithErrors, organization = organization, zone = zone, organizationKYCs = organizationKYCs))).recover {
            case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
          }
        },
        userReviewAddOrganizationRequestData => {
          val id = masterOrganizations.Service.tryGetID(loginState.username)

          def checkAllKYCFileTypesExists(id: String): Future[Boolean] = masterOrganizationKYCs.Service.checkAllKYCFileTypesExists(id)

          def markOrganizationFormCompletedAndGetResult(id: String, allKYCFileTypesExists: Boolean): Future[Result] = {
            if (userReviewAddOrganizationRequestData.completion && allKYCFileTypesExists) {
              val markOrganizationFormCompleted = masterOrganizations.Service.markOrganizationFormCompleted(id)
              for {
                _ <- markOrganizationFormCompleted
                result <- withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.ORGANIZATION_ADDED_FOR_VERIFICATION)))
              } yield result
            } else {
              val organization = masterOrganizations.Service.tryGetByAccountID(loginState.username)

              def getZoneOrganizationDetails(organization: Organization): Future[(Zone, Seq[OrganizationKYC])] = {
                val zone = masterZones.Service.tryGet(organization.zoneID)
                val organizationKYCs = masterOrganizationKYCs.Service.getAllDocuments(organization.id)
                for {
                  zone <- zone
                  organizationKYCs <- organizationKYCs
                } yield (zone, organizationKYCs)
              }

              for {
                organization <- organization
                (zone, organizationKYCs) <- getZoneOrganizationDetails(organization)
              } yield BadRequest(views.html.component.master.userReviewAddOrganizationRequest(organization = organization, zone = zone, organizationKYCs = organizationKYCs))
            }
          }

          (for {
            id <- id
            allKYCFileTypesExists <- checkAllKYCFileTypesExists(id)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.ADD_ORGANIZATION_REQUESTED, loginState.username)
            result <- markOrganizationFormCompletedAndGetResult(id, allKYCFileTypesExists)
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def acceptRequestForm(organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organization = masterOrganizations.Service.tryGet(organizationID)
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      (for {
        organization <- organization
        zoneID <- zoneID
      } yield if (organization.zoneID == zoneID) {
        Ok(views.html.component.master.acceptOrganizationRequest(organization = organization))
      } else {
        Unauthorized(views.html.account(failures = Seq(constants.Response.UNAUTHORIZED)))
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def acceptRequest: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AcceptOrganizationRequest.form.bindFromRequest().fold(
        formWithErrors => {
          val organization = masterOrganizations.Service.tryGet(formWithErrors.data(constants.FormField.ORGANIZATION_ID.name))
          val zoneID = masterZones.Service.tryGetID(loginState.username)
          (for {
            organization <- organization
            zoneID <- zoneID
          } yield if (organization.zoneID == zoneID) {
            BadRequest(views.html.component.master.acceptOrganizationRequest(formWithErrors, organization = organization))
          } else {
            Unauthorized(views.html.account(failures = Seq(constants.Response.UNAUTHORIZED)))
          }
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        },
        acceptRequestData => {
          val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = acceptRequestData.password)
          val zoneID = masterZones.Service.tryGetID(loginState.username)
          val organization = masterOrganizations.Service.tryGet(acceptRequestData.organizationID)

          def checkAllKYCFilesVerified(zoneID: String, organization: Organization): Future[Boolean] = {
            if (zoneID != organization.zoneID) throw new BaseException(constants.Response.UNAUTHORIZED)
            masterOrganizationKYCs.Service.checkAllKYCFilesVerified(acceptRequestData.organizationID)
          }

          def checkMemberCheckVerified(organizationID: String): Future[Boolean] = memberCheckCorporateScanResultDecisions.Service.checkOrganizationApproved(organizationID)

          def processTransactionAndGetResult(checkAllKYCFilesVerified: Boolean, checkMemberCheckVerified: Boolean, zoneID: String, validateUsernamePassword: Boolean, organization: Organization): Future[Result] = {
            if (validateUsernamePassword) {
              if (!checkMemberCheckVerified) throw new BaseException(constants.Response.MEMBER_CHECK_NOT_VERIFIED)
              else if (checkAllKYCFilesVerified) {
                val organizationAccountID = masterOrganizations.Service.tryGetAccountID(acceptRequestData.organizationID)

                def getOrganizationAccountAddress(accountId: String): Future[String] = blockchainAccounts.Service.tryGetAddress(accountId)

                def getTicketID(organizationAccountAddress: String): Future[String] = transaction.process[AddOrganization, transactionsAddOrganization.Request](
                  entity = AddOrganization(from = loginState.address, to = organizationAccountAddress, organizationID = acceptRequestData.organizationID, zoneID = zoneID, gas = acceptRequestData.gas, ticketID = "", mode = transactionMode),
                  blockchainTransactionCreate = blockchainTransactionAddOrganizations.Service.create,
                  request = transactionsAddOrganization.Request(transactionsAddOrganization.BaseReq(from = loginState.address, gas = acceptRequestData.gas.toString), to = organizationAccountAddress, organizationID = acceptRequestData.organizationID, zoneID = zoneID, password = acceptRequestData.password, mode = transactionMode),
                  action = transactionsAddOrganization.Service.post,
                  onSuccess = blockchainTransactionAddOrganizations.Utility.onSuccess,
                  onFailure = blockchainTransactionAddOrganizations.Utility.onFailure,
                  updateTransactionHash = blockchainTransactionAddOrganizations.Service.updateTransactionHash
                )

                for {
                  organizationAccountID <- organizationAccountID
                  organizationAccountAddress <- getOrganizationAccountAddress(organizationAccountID)
                  ticketID <- getTicketID(organizationAccountAddress)
                  _ <- utilitiesNotification.send(organizationAccountID, constants.Notification.ORGANIZATION_REQUEST_ACCEPTED, ticketID)
                  _ <- utilitiesNotification.send(loginState.username, constants.Notification.ORGANIZATION_REQUEST_ACCEPTED, ticketID)
                  result <- withUsernameToken.Ok(views.html.account(successes = Seq(constants.Response.ORGANIZATION_REQUEST_ACCEPTED)))
                } yield result
              } else Future(PreconditionFailed(views.html.account(failures = Seq(constants.Response.ALL_KYC_FILES_NOT_VERIFIED))))
            } else Future(BadRequest(views.html.component.master.acceptOrganizationRequest(views.companion.master.AcceptOrganizationRequest.form.fill(acceptRequestData).withGlobalError(constants.Response.INCORRECT_PASSWORD.message), organization = organization)))
          }

          (for {
            validateUsernamePassword <- validateUsernamePassword
            zoneID <- zoneID
            organization <- organization
            checkAllKYCFilesVerified <- checkAllKYCFilesVerified(zoneID, organization)
            checkMemberCheckVerified <- checkMemberCheckVerified(organization.id)
            result <- processTransactionAndGetResult(checkAllKYCFilesVerified = checkAllKYCFilesVerified, checkMemberCheckVerified = checkMemberCheckVerified, zoneID = zoneID, validateUsernamePassword = validateUsernamePassword, organization = organization)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def updateOrganizationKYCDocumentStatusForm(organizationID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val userZoneID = masterZones.Service.tryGetID(loginState.username)
      val organizationZoneID = masterOrganizations.Service.tryGetZoneID(organizationID)

      def getResult(userZoneID: String, organizationZoneID: String): Future[Result] = {
        if (userZoneID == organizationZoneID) {
          val organizationKYC = masterOrganizationKYCs.Service.tryGet(id = organizationID, documentType = documentType)
          for {
            organizationKYC <- organizationKYC
            result <- withUsernameToken.Ok(views.html.component.master.updateOrganizationKYCDocumentStatus(organizationKYC = organizationKYC))
          } yield result
        } else Future(Unauthorized(views.html.account(failures = Seq(constants.Response.UNAUTHORIZED))))
      }

      (for {
        userZoneID <- userZoneID
        organizationZoneID <- organizationZoneID
        result <- getResult(userZoneID, organizationZoneID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def updateOrganizationKYCDocumentStatus(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.UpdateOrganizationKYCDocumentStatus.form.bindFromRequest().fold(
        formWithErrors => {
          val organizationKYC = masterOrganizationKYCs.Service.tryGet(id = formWithErrors(constants.FormField.ORGANIZATION_ID.name).value.get, documentType = formWithErrors(constants.FormField.DOCUMENT_TYPE.name).value.get)
          for {
            organizationKYC <- organizationKYC
          } yield BadRequest(views.html.component.master.updateOrganizationKYCDocumentStatus(formWithErrors, organizationKYC))
        },
        updateOrganizationKYCDocumentStatusData => {
          val userZoneID = masterZones.Service.tryGetID(loginState.username)
          val organizationZoneID = masterOrganizations.Service.tryGetZoneID(updateOrganizationKYCDocumentStatusData.organizationID)

          def verifyOrReject: Future[Unit] = {
            if (updateOrganizationKYCDocumentStatusData.status) {
              val verifyOrganizationKYCs = masterOrganizationKYCs.Service.verify(id = updateOrganizationKYCDocumentStatusData.organizationID, documentType = updateOrganizationKYCDocumentStatusData.documentType)
              val organizationID = masterOrganizations.Service.tryGetAccountID(updateOrganizationKYCDocumentStatusData.organizationID)
              for {
                _ <- verifyOrganizationKYCs
                organizationID <- organizationID
                _ <- utilitiesNotification.send(organizationID, constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
              } yield {}
            } else {
              val rejectOrganizationKYCs = masterOrganizationKYCs.Service.reject(id = updateOrganizationKYCDocumentStatusData.organizationID, documentType = updateOrganizationKYCDocumentStatusData.documentType)
              val organizationID = masterOrganizations.Service.tryGetAccountID(updateOrganizationKYCDocumentStatusData.organizationID)
              for {
                _ <- rejectOrganizationKYCs
                organizationID <- organizationID
                _ <- utilitiesNotification.send(organizationID, constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
              } yield {}
            }
          }

          def organizationKYC: Future[OrganizationKYC] = masterOrganizationKYCs.Service.tryGet(updateOrganizationKYCDocumentStatusData.organizationID, updateOrganizationKYCDocumentStatusData.documentType)

          def getResult(userZoneID: String, organizationZoneID: String): Future[Result] = {
            if (userZoneID == organizationZoneID) {
              for {
                _ <- verifyOrReject
                organizationKYC <- organizationKYC
                result <- withUsernameToken.PartialContent(views.html.component.master.updateOrganizationKYCDocumentStatus(organizationKYC = organizationKYC))
              } yield result
            } else {
              Future(Unauthorized(views.html.account(failures = Seq(constants.Response.UNAUTHORIZED))))
            }
          }

          (for {
            userZoneID <- userZoneID
            organizationZoneID <- organizationZoneID
            result <- getResult(userZoneID, organizationZoneID)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def rejectRequestForm(organizationID: String): Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.rejectOrganizationRequest(organizationID = organizationID))
  }

  def rejectRequest(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RejectOrganizationRequest.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.rejectOrganizationRequest(formWithErrors, organizationID = formWithErrors.data(constants.FormField.ORGANIZATION_ID.name))))
        },
        rejectRequestData => {
          val rejectOrganization = masterOrganizations.Service.markRejected(id = rejectRequestData.organizationID, comment = rejectRequestData.comment)
          val organizationAccountID = masterOrganizations.Service.tryGetAccountID(rejectRequestData.organizationID)

          (for {
            _ <- rejectOrganization
            organizationAccountID <- organizationAccountID
            _ <- utilitiesNotification.send(organizationAccountID, constants.Notification.ORGANIZATION_REQUEST_REJECTED, rejectRequestData.comment.getOrElse(constants.View.NO_COMMENTS))
            result <- withUsernameToken.Ok(views.html.account(successes = Seq(constants.Response.ORGANIZATION_REQUEST_REJECTED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def uploadOrganizationKYCForm(documentType: String): Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.uploadOrganizationKYC), utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.storeOrganizationKYC), documentType))
  }

  def updateOrganizationKYCForm(documentType: String): Action[AnyContent] = withoutLoginAction { implicit request =>
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
            case None => BadRequest(views.html.profile(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getOrganizationKYCFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeOrganizationKYC(name: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val id = masterOrganizations.Service.tryGetID(loginState.username)

      def storeFile(id: String): Future[Unit] = fileResourceManager.storeFile[OrganizationKYC](
        name = name,
        path = fileResourceManager.getOrganizationKYCFilePath(documentType),
        document = OrganizationKYC(id = id, documentType = documentType, status = None, fileName = name, file = None),
        masterCreate = masterOrganizationKYCs.Service.create
      )

      (for {
        id <- id
        _ <- storeFile(id)
        result <- withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def updateOrganizationKYC(name: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOldDocument(organizationID: String): Future[OrganizationKYC] = masterOrganizationKYCs.Service.tryGet(id = organizationID, documentType = documentType)

      def updateFile(oldDocument: OrganizationKYC): Future[Boolean] = fileResourceManager.updateFile[OrganizationKYC](
        name = name,
        path = fileResourceManager.getOrganizationKYCFilePath(documentType),
        oldDocument = oldDocument,
        updateOldDocument = masterOrganizationKYCs.Service.updateOldDocument
      )

      (for {
        organizationID <- organizationID
        oldDocument <- getOldDocument(organizationID)
        _ <- updateFile(oldDocument)
        result <- withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def blockchainAddOrganizationForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.addOrganization())
  }

  def blockchainAddOrganization: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.AddOrganization.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.addOrganization(formWithErrors)))
      },
      addOrganizationData => {
        val postRequest = transactionsAddOrganization.Service.post(transactionsAddOrganization.Request(transactionsAddOrganization.BaseReq(from = addOrganizationData.from, gas = addOrganizationData.gas.toString), to = addOrganizationData.to, organizationID = addOrganizationData.organizationID, zoneID = addOrganizationData.zoneID, password = addOrganizationData.password, mode = addOrganizationData.mode))
        (for {
          _ <- postRequest
        } yield Ok(views.html.index(successes = Seq(constants.Response.ORGANIZATION_ADDED)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}