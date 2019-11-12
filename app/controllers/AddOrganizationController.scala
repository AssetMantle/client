package controllers

import java.nio.file.Files

import controllers.actions.{WithGenesisLoginAction, WithOrganizationLoginAction, WithUserLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchainTransaction.AddOrganization
import models.common.Serializable._
import models.master.Organization
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.master.FileUpload

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddOrganizationController @Inject()(messagesControllerComponents: MessagesControllerComponents, withOrganizationLoginAction: WithOrganizationLoginAction, fileResourceManager: utilities.FileResourceManager, transaction: utilities.Transaction, masterOrganizationBankAccountDetails: master.OrganizationBankAccountDetails, utilitiesNotification: utilities.Notification, blockchainAccounts: blockchain.Accounts, masterOrganizationKYCs: master.OrganizationKYCs, masterTraders: master.Traders, transactionsAddOrganization: transactions.AddOrganization, blockchainOrganizations: blockchain.Organizations, masterZones: master.Zones, blockchainTransactionAddOrganizations: blockchainTransaction.AddOrganizations, masterOrganizations: master.Organizations, masterAccounts: master.Accounts, withUserLoginAction: WithUserLoginAction, withZoneLoginAction: WithZoneLoginAction, withGenesisLoginAction: WithGenesisLoginAction, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_ADD_ORGANIZATION

  private implicit val logger: Logger = Logger(this.getClass)

  def addOrganizationForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organization = masterOrganizations.Service.getByAccountID(loginState.username)
      val zones = masterZones.Service.getAllVerified
      (for {
        organization <- organization
        zones <- zones
      } yield withUsernameToken.Ok(views.html.component.master.addOrganization(views.companion.master.AddOrganization.form.fill(value = views.companion.master.AddOrganization.Data(zoneID = organization.zoneID, name = organization.name, abbreviation = organization.abbreviation, establishmentDate = utilities.Date.sqlDateToUtilDate(organization.establishmentDate), email = organization.email, registeredAddress = views.companion.master.AddOrganization.AddressData(addressLine1 = organization.registeredAddress.addressLine1, addressLine2 = organization.registeredAddress.addressLine2, landmark = organization.registeredAddress.landmark, city = organization.registeredAddress.city, country = organization.registeredAddress.country, zipCode = organization.registeredAddress.zipCode, phone = organization.registeredAddress.phone), postalAddress = views.companion.master.AddOrganization.AddressData(addressLine1 = organization.postalAddress.addressLine1, addressLine2 = organization.postalAddress.addressLine2, landmark = organization.postalAddress.landmark, city = organization.postalAddress.city, country = organization.postalAddress.country, zipCode = organization.postalAddress.zipCode, phone = organization.postalAddress.phone))), zones = zones))
        ).recoverWith {
        case _: BaseException =>
          val zones = masterZones.Service.getAllVerified
          for {
            zones <- zones
          } yield Ok(views.html.component.master.addOrganization(views.companion.master.AddOrganization.form, zones = zones))
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

          def insertOrUpdateOrganizationWithoutUBOsAndGetResult(verificationStatus: Boolean) = {
            if (verificationStatus) {
              val id = masterOrganizations.Service.insertOrUpdateWithoutUBOs(zoneID = addOrganizationData.zoneID, accountID = loginState.username, name = addOrganizationData.name, abbreviation = addOrganizationData.abbreviation, establishmentDate = utilities.Date.utilDateToSQLDate(addOrganizationData.establishmentDate), email = addOrganizationData.email, registeredAddress = Address(addressLine1 = addOrganizationData.registeredAddress.addressLine1, addressLine2 = addOrganizationData.registeredAddress.addressLine2, landmark = addOrganizationData.registeredAddress.landmark, city = addOrganizationData.registeredAddress.city, country = addOrganizationData.registeredAddress.country, zipCode = addOrganizationData.registeredAddress.zipCode, phone = addOrganizationData.registeredAddress.phone), postalAddress = Address(addressLine1 = addOrganizationData.postalAddress.addressLine1, addressLine2 = addOrganizationData.postalAddress.addressLine2, landmark = addOrganizationData.postalAddress.landmark, city = addOrganizationData.postalAddress.city, country = addOrganizationData.postalAddress.country, zipCode = addOrganizationData.postalAddress.zipCode, phone = addOrganizationData.postalAddress.phone))

              def getUBOs(id: String) = masterOrganizations.Service.getUBOs(id)

              for {
                id <- id
                ubos <- getUBOs(id)
              } yield withUsernameToken.PartialContent(views.html.component.master.userUpdateUBOs(views.companion.master.AddUBOs.form.fill(views.companion.master.AddUBOs.Data(ubos.data.map(ubo => Option(views.companion.master.AddUBOs.UBOData(personName = ubo.personName, sharePercentage = ubo.sharePercentage, relationship = ubo.relationship, title = ubo.title)))))))
            }
            else {
              Future {
                Unauthorized(views.html.index(failures = Seq(constants.Response.UNVERIFIED_ZONE)))
              }
            }
          }

          (for {
            verificationStatus <- verificationStatus
            result <- insertOrUpdateOrganizationWithoutUBOsAndGetResult(verificationStatus)
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def userUpdateUBOsForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val id = masterOrganizations.Service.getID(loginState.username)

      def getUBOs(id: String) = masterOrganizations.Service.getUBOs(id)

      (for {
        id <- id
        ubos <- getUBOs(id)
      } yield withUsernameToken.Ok(views.html.component.master.userUpdateUBOs(views.companion.master.AddUBOs.form.fill(views.companion.master.AddUBOs.Data(ubos.data.map(ubo => Option(views.companion.master.AddUBOs.UBOData(personName = ubo.personName, sharePercentage = ubo.sharePercentage, relationship = ubo.relationship, title = ubo.title)))))))
        ).recover {
        case _: BaseException => withUsernameToken.Ok(views.html.component.master.userUpdateUBOs())
      }
  }

  def userUpdateUBOs(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddUBOs.form.bindFromRequest().fold(
        formWithErrors => {
          Future {
            BadRequest(views.html.component.master.userUpdateUBOs(formWithErrors))
          }
        },
        updateUBOsData => {
          val id = masterOrganizations.Service.getID(loginState.username)

          def updateUBOs(id: String) = masterOrganizations.Service.updateUBOs(id = id, ubos = updateUBOsData.ubos.filter(_.isDefined).map(uboData => UBO(personName = uboData.get.personName, sharePercentage = uboData.get.sharePercentage, relationship = uboData.get.relationship, title = uboData.get.title)))

          def getUBOs(id: String) = masterOrganizations.Service.getUBOs(id)

          (for {
            id <- id
            _ <- updateUBOs(id)
            ubos <- getUBOs(id)
          } yield withUsernameToken.PartialContent(views.html.component.master.userUpdateUBOs(views.companion.master.AddUBOs.form.fill(views.companion.master.AddUBOs.Data(ubos.data.map(ubo => Option(views.companion.master.AddUBOs.UBOData(personName = ubo.personName, sharePercentage = ubo.sharePercentage, relationship = ubo.relationship, title = ubo.title)))))))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def organizationBankAccountDetailForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val id = masterOrganizations.Service.getID(loginState.username)

      def getOrganizationBankAccountDetail(id: String) = masterOrganizationBankAccountDetails.Service.get(id)

      (for {
        id <- id
        organizationBankAccountDetail <- getOrganizationBankAccountDetail(id)
      } yield withUsernameToken.Ok(views.html.component.master.organizationBankAccountDetail(views.companion.master.OrganizationBankAccountDetail.form.fill(views.companion.master.OrganizationBankAccountDetail.Data(accountHolder = organizationBankAccountDetail.accountHolder, nickName = organizationBankAccountDetail.nickName, accountNumber = organizationBankAccountDetail.accountNumber, bankName = organizationBankAccountDetail.bankName, swiftAddress = organizationBankAccountDetail.swiftAddress, streetAddress = organizationBankAccountDetail.address, country = organizationBankAccountDetail.country, zipCode = organizationBankAccountDetail.zipCode))))
        ).recover {
        case _: BaseException => withUsernameToken.Ok(views.html.component.master.organizationBankAccountDetail())
      }
  }

  def organizationBankAccountDetail(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.OrganizationBankAccountDetail.form.bindFromRequest().fold(
        formWithErrors => {
          Future {
            BadRequest(views.html.component.master.organizationBankAccountDetail(formWithErrors))
          }
        },
        organizationBankAccountDetailData => {
          val id = masterOrganizations.Service.getID(loginState.username)

          def insertOrUpdate(id: String) = masterOrganizationBankAccountDetails.Service.insertOrUpdate(id = id, accountHolder = organizationBankAccountDetailData.accountHolder, nickName = organizationBankAccountDetailData.nickName, accountNumber = organizationBankAccountDetailData.accountNumber, bankName = organizationBankAccountDetailData.bankName, swiftAddress = organizationBankAccountDetailData.swiftAddress, address = organizationBankAccountDetailData.streetAddress, country = organizationBankAccountDetailData.country, zipCode = organizationBankAccountDetailData.zipCode)

          def getOrganizationKYCs(id: String) = masterOrganizationKYCs.Service.getAllDocuments(id)

          (for {
            id <- id
            _ <- insertOrUpdate(id)
            organizationKYCs <- getOrganizationKYCs(id)
          } yield withUsernameToken.PartialContent(views.html.component.master.userUploadOrUpdateOrganizationKYC(organizationKYCs))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def userUploadOrUpdateOrganizationKYCView(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val id = masterOrganizations.Service.getID(loginState.username)

      def getOrganizationKYCs(id: String) = masterOrganizationKYCs.Service.getAllDocuments(id)

      (for {
        id <- id
        organizationKYCs <- getOrganizationKYCs(id)
      } yield Ok(views.html.component.master.userUploadOrUpdateOrganizationKYC(organizationKYCs))
        ).recover {
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
      val organizationID = masterOrganizations.Service.getID(loginState.username)

      def storeFile(organizationID: String) = fileResourceManager.storeFile[master.OrganizationKYC](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getOrganizationKYCFilePath(documentType),
        document = master.OrganizationKYC(id = organizationID, documentType = documentType, status = None, fileName = name, file = None),
        masterCreate = masterOrganizationKYCs.Service.create
      )

      def getOrganizationKYCs(organizationID: String) = masterOrganizationKYCs.Service.getAllDocuments(organizationID)

      (for {
        organizationID <- organizationID
        _ <- storeFile(organizationID)
        organizationKYCs <- getOrganizationKYCs(organizationID)
      } yield withUsernameToken.PartialContent(views.html.component.master.userUploadOrUpdateOrganizationKYC(organizationKYCs))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userUpdateOrganizationKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.userUploadOrganizationKYC), utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.userUpdateOrganizationKYC), documentType))
  }

  def userUpdateOrganizationKYC(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val organizationID = masterOrganizations.Service.getID(loginState.username)

      def oldDocumentFileName(organizationID: String) = masterOrganizationKYCs.Service.getFileName(id = organizationID, documentType = documentType)

      def updateFile(organizationID: String, oldDocumentFileName: String) = fileResourceManager.updateFile[master.OrganizationKYC](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getOrganizationKYCFilePath(documentType),
        oldDocumentFileName = oldDocumentFileName,
        document = master.OrganizationKYC(id = organizationID, documentType = documentType, status = None, fileName = name, file = None),
        updateOldDocument = masterOrganizationKYCs.Service.updateOldDocument
      )

      def getOrganizationKYCs(id: String) = masterOrganizationKYCs.Service.getAllDocuments(id)

      (for {
        organizationID <- organizationID
        oldDocumentFileName <- oldDocumentFileName(organizationID)
        _ <- updateFile(organizationID, oldDocumentFileName)
        organizationKYCs <- getOrganizationKYCs(organizationID)
      } yield withUsernameToken.PartialContent(views.html.component.master.userUploadOrUpdateOrganizationKYC(organizationKYCs))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userReviewAddOrganizationRequestForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organization = masterOrganizations.Service.getByAccountID(loginState.username)

      (for {
        organization <- organization
        (zone, organizationBankAccountDetail, organizationKYCs) <- getZoneOrganizationDetails(organization)
      } yield withUsernameToken.Ok(views.html.component.master.userReviewAddOrganizationRequest(organization = organization, zone = zone, organizationBankAccountDetail = organizationBankAccountDetail, organizationKYCs = organizationKYCs))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def getZoneOrganizationDetails(organization: Organization) = {
    val zone = masterZones.Service.get(organization.zoneID)
    val organizationBankAccountDetail = masterOrganizationBankAccountDetails.Service.get(organization.id)
    val organizationKYCs = masterOrganizationKYCs.Service.getAllDocuments(organization.id)
    for {
      zone <- zone
      organizationBankAccountDetail <- organizationBankAccountDetail
      organizationKYCs <- organizationKYCs
    } yield (zone, organizationBankAccountDetail, organizationKYCs)
  }

  def userReviewAddOrganizationRequest(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.UserReviewAddOrganizationRequest.form.bindFromRequest().fold(
        formWithErrors => {
          val organization = masterOrganizations.Service.getByAccountID(loginState.username)
          (for {
            organization <- organization
            (zone, organizationBankAccountDetail, organizationKYCs) <- getZoneOrganizationDetails(organization)
          } yield BadRequest(views.html.component.master.userReviewAddOrganizationRequest(formWithErrors, organization = organization, zone = zone, organizationBankAccountDetail = organizationBankAccountDetail, organizationKYCs = organizationKYCs))).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        userReviewAddOrganizationRequestData => {
          val id = masterOrganizations.Service.getID(loginState.username)

          def checkAllKYCFileTypesExists(id: String) = masterOrganizationKYCs.Service.checkAllKYCFileTypesExists(id)

          def markOrganizationFormCompletedAndGetResult(id: String, allKYCFileTypesExists: Boolean) = {
            if (userReviewAddOrganizationRequestData.completion && allKYCFileTypesExists) {
              val markOrganizationFormCompleted = masterOrganizations.Service.markOrganizationFormCompleted(id)
              for {
                _ <- markOrganizationFormCompleted
              } yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ORGANIZATION_ADDED_FOR_VERIFICATION)))
            } else {
              val organization = masterOrganizations.Service.getByAccountID(loginState.username)
              for {
                organization <- organization
                (zone, organizationBankAccountDetail, organizationKYCs) <- getZoneOrganizationDetails(organization)
              } yield BadRequest(views.html.component.master.userReviewAddOrganizationRequest(organization = organization, zone = zone, organizationBankAccountDetail = organizationBankAccountDetail, organizationKYCs = organizationKYCs))
            }
          }

          (for {
            id <- id
            allKYCFileTypesExists <- checkAllKYCFileTypesExists(id)
            result <- markOrganizationFormCompletedAndGetResult(id, allKYCFileTypesExists)
          } yield result).recover {
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
          Future {
            BadRequest(views.html.component.master.verifyOrganization(formWithErrors, organizationID = formWithErrors.data(constants.FormField.ORGANIZATION_ID.name), zoneID = formWithErrors.data(constants.FormField.ZONE_ID.name)))
          }
        },
        verifyOrganizationData => {
          val allKYCFilesVerified = masterOrganizationKYCs.Service.checkAllKYCFilesVerified(verifyOrganizationData.organizationID)

          def processTransactionAndGetResult(allKYCFilesVerified: Boolean) = {
            if (allKYCFilesVerified) {
              val accountId = masterOrganizations.Service.getAccountId(verifyOrganizationData.organizationID)

              def organizationAccountAddress(accountId: String) = masterAccounts.Service.getAddress(accountId)

              def transactionProcess(organizationAccountAddress: String) = transaction.process[AddOrganization, transactionsAddOrganization.Request](
                entity = AddOrganization(from = loginState.address, to = organizationAccountAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, gas = verifyOrganizationData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionAddOrganizations.Service.create,
                request = transactionsAddOrganization.Request(transactionsAddOrganization.BaseReq(from = loginState.address, gas = verifyOrganizationData.gas.toString), to = organizationAccountAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, password = verifyOrganizationData.password, mode = transactionMode),
                action = transactionsAddOrganization.Service.post,
                onSuccess = blockchainTransactionAddOrganizations.Utility.onSuccess,
                onFailure = blockchainTransactionAddOrganizations.Utility.onFailure,
                updateTransactionHash = blockchainTransactionAddOrganizations.Service.updateTransactionHash
              )

              for {
                accountId <- accountId
                organizationAccountAddress <- organizationAccountAddress(accountId)
                _ <- transactionProcess(organizationAccountAddress)
              } yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ORGANIZATION_VERIFIED)))
            } else {
              Future {
                PreconditionFailed(views.html.index(failures = Seq(constants.Response.ALL_KYC_FILES_NOT_VERIFIED)))
              }
            }
          }

          (for {
            allKYCFilesVerified <- allKYCFilesVerified
            result <- processTransactionAndGetResult(allKYCFilesVerified)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def viewKYCDocuments(organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationKYCs = masterOrganizationKYCs.Service.getAllDocuments(organizationID)
      (for {
        organizationKYCs <- organizationKYCs
      } yield Ok(views.html.component.master.viewVerificationOrganizationKYCDouments(organizationKYCs))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateOrganizationKYCDocumentStatusForm(organizationID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val userZoneID = masterZones.Service.getID(loginState.username)
      val organizationZoneID = masterOrganizations.Service.getZoneID(organizationID)

      def getResult(userZoneID: String, organizationZoneID: String) = {
        if (userZoneID == organizationZoneID) {
          val organizationKYC = masterOrganizationKYCs.Service.get(id = organizationID, documentType = documentType)
          for {
            organizationKYC <- organizationKYC
          } yield withUsernameToken.Ok(views.html.component.master.updateOrganizationKYCDocumentStatus(organizationKYC = organizationKYC))
        } else Future {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      }

      (for {
        userZoneID <- userZoneID
        organizationZoneID <- organizationZoneID
        result <- getResult(userZoneID, organizationZoneID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateOrganizationKYCDocumentStatus(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.UpdateOrganizationKYCDocumentStatus.form.bindFromRequest().fold(
        formWithErrors => {
          val organizationKYC = masterOrganizationKYCs.Service.get(id = formWithErrors(constants.FormField.ORGANIZATION_ID.name).value.get, documentType = formWithErrors(constants.FormField.DOCUMENT_TYPE.name).value.get)
          for {
            organizationKYC <- organizationKYC
          } yield BadRequest(views.html.component.master.updateOrganizationKYCDocumentStatus(formWithErrors, organizationKYC))
        },
        updateOrganizationKYCDocumentStatusData => {
          val userZoneID = masterZones.Service.getID(loginState.username)
          val organizationZoneID = masterOrganizations.Service.getZoneID(updateOrganizationKYCDocumentStatusData.organizationID)

          def verifyOrReject = {
            if (updateOrganizationKYCDocumentStatusData.status) {
              val verifyOrganizationKYCs = masterOrganizationKYCs.Service.verify(id = updateOrganizationKYCDocumentStatusData.organizationID, documentType = updateOrganizationKYCDocumentStatusData.documentType)
              val organizationID = masterOrganizations.Service.getAccountId(updateOrganizationKYCDocumentStatusData.organizationID)
              for {
                _ <- verifyOrganizationKYCs
                organizationID <- organizationID
              } yield utilitiesNotification.send(organizationID, constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
            } else {
              val rejectOrganizationKYCs = masterOrganizationKYCs.Service.verify(id = updateOrganizationKYCDocumentStatusData.organizationID, documentType = updateOrganizationKYCDocumentStatusData.documentType)
              val organizationID = masterOrganizations.Service.getAccountId(updateOrganizationKYCDocumentStatusData.organizationID)
              for {
                _ <- rejectOrganizationKYCs
                organizationID <- organizationID
              } yield utilitiesNotification.send(organizationID, constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
            }
          }

          def organizationKYC = masterOrganizationKYCs.Service.get(updateOrganizationKYCDocumentStatusData.organizationID, updateOrganizationKYCDocumentStatusData.documentType)

          def getResult(userZoneID: String, organizationZoneID: String) = {
            if (userZoneID == organizationZoneID) {
              for {
                _ <- verifyOrReject
                organizationKYC <- organizationKYC
              } yield withUsernameToken.PartialContent(views.html.component.master.updateOrganizationKYCDocumentStatus(organizationKYC = organizationKYC))
            } else {
              Future {
                Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
              }
            }
          }

          (for {
            userZoneID <- userZoneID
            organizationZoneID <- organizationZoneID
            result <- getResult(userZoneID, organizationZoneID)
          } yield result
            ).recover {
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
          Future {
            BadRequest(views.html.component.master.rejectVerifyOrganizationRequest(formWithErrors, organizationID = formWithErrors.data(constants.FormField.ORGANIZATION_ID.name)))
          }
        },
        rejectVerifyOrganizationRequestData => {
          val rejectOrganization = masterOrganizations.Service.rejectOrganization(rejectVerifyOrganizationRequestData.organizationID)
          val organizationAccountID = masterOrganizations.Service.getAccountId(rejectVerifyOrganizationRequestData.organizationID)

          def rejectAll(organizationAccountID: String) = masterOrganizationKYCs.Service.rejectAll(organizationAccountID)

          (for {
            _ <- rejectOrganization
            organizationAccountID <- organizationAccountID
            _ <- rejectAll(organizationAccountID)
          } yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.VERIFY_ORGANIZATION_REQUEST_REJECTED)))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def viewOrganizationVerificationBankAccountDetail(organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationBankAccountDetail = masterOrganizationBankAccountDetails.Service.get(organizationID)
      (for {
        organizationBankAccountDetail <- organizationBankAccountDetail
      } yield Ok(views.html.component.master.viewOrganizationBankAccountDetail(organizationBankAccountDetail))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def viewPendingVerifyOrganizationRequests: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.getID(loginState.username)

      def verifyOrganizationRequests(zoneID: String) = masterOrganizations.Service.getVerifyOrganizationRequests(zoneID)

      (for {
        zoneID <- zoneID
        verifyOrganizationRequests <- verifyOrganizationRequests(zoneID)
      } yield Ok(views.html.component.master.viewPendingVerifyOrganizationRequests(verifyOrganizationRequests))
        ).recover {
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
      val id = masterOrganizations.Service.getID(loginState.username)

      def storeFile(id: String) = fileResourceManager.storeFile[master.OrganizationKYC](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getOrganizationKYCFilePath(documentType),
        document = master.OrganizationKYC(id = id, documentType = documentType, status = None, fileName = name, file = None),
        masterCreate = masterOrganizationKYCs.Service.create
      )

      (for {
        id <- id
        _ <- storeFile(id)
      } yield withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateOrganizationKYC(name: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.getID(loginState.username)

      def oldDocumentFileName(organizationID: String) = masterOrganizationKYCs.Service.getFileName(id = organizationID, documentType = documentType)

      def updateFile(oldDocumentFileName: String, organizationID: String) = fileResourceManager.updateFile[master.OrganizationKYC](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getOrganizationKYCFilePath(documentType),
        oldDocumentFileName = oldDocumentFileName,
        document = master.OrganizationKYC(id = organizationID, documentType = documentType, status = None, fileName = name, file = None),
        updateOldDocument = masterOrganizationKYCs.Service.updateOldDocument
      )

      (for {
        organizationID <- organizationID
        oldDocumentFileName <- oldDocumentFileName(organizationID)
        _ <- updateFile(oldDocumentFileName, organizationID)
      } yield withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def viewOrganizationsInZone: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.getID(loginState.username)

      def organizationsInZone(zoneID: String) = masterOrganizations.Service.getOrganizationsInZone(zoneID)

      (for {
        zoneID <- zoneID
        organizationsInZone <- organizationsInZone(zoneID)
      } yield Ok(views.html.component.master.viewOrganizationsInZone(organizationsInZone))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def viewOrganizationsInZoneForGenesis(zoneID: String): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationsInZone = masterOrganizations.Service.getOrganizationsInZone(zoneID)
      (for {
        organizationsInZone <- organizationsInZone
      } yield Ok(views.html.component.master.viewOrganizationsInZoneForGenesis(organizationsInZone))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def blockchainAddOrganizationForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.addOrganization())
  }

  def blockchainAddOrganization: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.AddOrganization.form.bindFromRequest().fold(
      formWithErrors => {
        Future {
          BadRequest(views.html.component.blockchain.addOrganization(formWithErrors))
        }
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