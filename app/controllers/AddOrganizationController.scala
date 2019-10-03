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
      masterOrganizations.Service.getByAccountID(loginState.username).map { organization =>
        Ok(views.html.component.master.addOrganization(views.companion.master.AddOrganization.form.fill(value = views.companion.master.AddOrganization.Data(zoneID = organization.zoneID, name = organization.name, abbreviation = organization.abbreviation, establishmentDate = utilities.Date.sqlDateToUtilDate(organization.establishmentDate), email = organization.email, registeredAddress = views.companion.master.AddOrganization.AddressData(addressLine1 = organization.registeredAddress.addressLine1, addressLine2 = organization.registeredAddress.addressLine2, Landmark = organization.registeredAddress.landmark, City = organization.registeredAddress.city, Country = organization.registeredAddress.country, ZipCode = organization.registeredAddress.zipCode, Phone = organization.registeredAddress.phone), postalAddress = views.companion.master.AddOrganization.AddressData(addressLine1 = organization.postalAddress.addressLine1, addressLine2 = organization.postalAddress.addressLine2, Landmark = organization.postalAddress.landmark, City = organization.postalAddress.city, Country = organization.postalAddress.country, ZipCode = organization.postalAddress.zipCode, Phone = organization.postalAddress.phone))), zones = masterZones.Service.getAll))
      }.recover {
        case _: BaseException => Ok(views.html.component.master.addOrganization(views.companion.master.AddOrganization.form, zones = masterZones.Service.getAll))
      }
  }

  def addOrganization(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddOrganization.form.bindFromRequest().fold(
        formWithErrors => {
          Future {
            BadRequest(views.html.component.master.addOrganization(formWithErrors, zones = masterZones.Service.getAll))
          }
        },
        addOrganizationData => {

          val verificationStatus = masterZones.Service.getVerificationStatus(addOrganizationData.zoneID)

          def insertOrUpdateOrganizationWithoutUBOsAndGetResult(verificationStatus: Boolean) = {
            if (verificationStatus) {
              val id = masterOrganizations.Service.insertOrUpdateOrganizationWithoutUBOs(zoneID = addOrganizationData.zoneID, accountID = loginState.username, name = addOrganizationData.name, abbreviation = addOrganizationData.abbreviation, establishmentDate = utilities.Date.utilDateToSQLDate(addOrganizationData.establishmentDate), email = addOrganizationData.email, registeredAddress = Address(addressLine1 = addOrganizationData.registeredAddress.addressLine1, addressLine2 = addOrganizationData.registeredAddress.addressLine2, landmark = addOrganizationData.registeredAddress.Landmark, city = addOrganizationData.registeredAddress.City, country = addOrganizationData.registeredAddress.Country, zipCode = addOrganizationData.registeredAddress.ZipCode, phone = addOrganizationData.registeredAddress.Phone), postalAddress = Address(addressLine1 = addOrganizationData.postalAddress.addressLine1, addressLine2 = addOrganizationData.postalAddress.addressLine2, landmark = addOrganizationData.postalAddress.Landmark, city = addOrganizationData.postalAddress.City, country = addOrganizationData.postalAddress.Country, zipCode = addOrganizationData.postalAddress.ZipCode, phone = addOrganizationData.postalAddress.Phone))

              def getUBOs(id: String) = masterOrganizations.Service.getUBOs(id)

              for {
                id <- id
                uBOs <- getUBOs(id)
              } yield PartialContent(views.html.component.master.userUpdateUBOs(views.companion.master.AddUBOs.form.fill(views.companion.master.AddUBOs.Data(uBOs.data.map(ubo => Option(views.companion.master.AddUBOs.UBOData(personName = ubo.personName, sharePercentage = ubo.sharePercentage, relationship = ubo.relationship, title = ubo.title)))))))
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

      for {
        id <- id
        uBOs <- getUBOs(id)
      } yield Ok(views.html.component.master.userUpdateUBOs(views.companion.master.AddUBOs.form.fill(views.companion.master.AddUBOs.Data(uBOs.data.map(ubo => Option(views.companion.master.AddUBOs.UBOData(personName = ubo.personName, sharePercentage = ubo.sharePercentage, relationship = ubo.relationship, title = ubo.title)))))))

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
            uBOs <- getUBOs(id)
          } yield PartialContent(views.html.component.master.userUpdateUBOs(views.companion.master.AddUBOs.form.fill(views.companion.master.AddUBOs.Data(uBOs.data.map(ubo => Option(views.companion.master.AddUBOs.UBOData(personName = ubo.personName, sharePercentage = ubo.sharePercentage, relationship = ubo.relationship, title = ubo.title)))))))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def organizationBankAccountDetailForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val id = masterOrganizations.Service.getID(loginState.username)

      def organizationBankAccountDetail(id: String) = masterOrganizationBankAccountDetails.Service.get(id)

      (for {
        id <- id
        organizationBankAccountDetail <- organizationBankAccountDetail(id)
      } yield Ok(views.html.component.master.organizationBankAccountDetail(views.companion.master.OrganizationBankAccountDetail.form.fill(views.companion.master.OrganizationBankAccountDetail.Data(accountHolder = organizationBankAccountDetail.accountHolder, nickName = organizationBankAccountDetail.nickName, accountNumber = organizationBankAccountDetail.accountNumber, bankName = organizationBankAccountDetail.bankName, swiftAddress = organizationBankAccountDetail.swiftAddress, address = organizationBankAccountDetail.address, country = organizationBankAccountDetail.country, zipCode = organizationBankAccountDetail.zipCode))))
        ).recover {
        case _: BaseException => Ok(views.html.component.master.organizationBankAccountDetail(views.companion.master.OrganizationBankAccountDetail.form))
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

          def insertOrUpdate(id: String) = masterOrganizationBankAccountDetails.Service.insertOrUpdate(id = id, accountHolder = organizationBankAccountDetailData.accountHolder, nickName = organizationBankAccountDetailData.nickName, accountNumber = organizationBankAccountDetailData.accountNumber, bankName = organizationBankAccountDetailData.bankName, swiftAddress = organizationBankAccountDetailData.swiftAddress, address = organizationBankAccountDetailData.address, country = organizationBankAccountDetailData.country, zipCode = organizationBankAccountDetailData.zipCode)

          def getAllDocuments(id: String) = masterOrganizationKYCs.Service.getAllDocuments(id)

          (for {
            id <- id
            _ <- insertOrUpdate(id)
            documents <- getAllDocuments(id)
          } yield PartialContent(views.html.component.master.userUploadOrUpdateOrganizationKYC(documents))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def userUploadOrUpdateOrganizationKYCView(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val id = masterOrganizations.Service.getID(loginState.username)

      def getAllDocuments(id: String) = masterOrganizationKYCs.Service.getAllDocuments(id)

      (for {
        id <- id
        documents <- getAllDocuments(id)
      } yield Ok(views.html.component.master.userUploadOrUpdateOrganizationKYC(documents))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userUploadOrganizationKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.userUploadOrganizationKyc), utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.userStoreOrganizationKyc), documentType))
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

        def storeFile(id: String) = fileResourceManager.storeFile[master.OrganizationKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getOrganizationKycFilePath(documentType),
          document = master.OrganizationKYC(id = id, documentType = documentType, status = None, fileName = name, file = None),
          masterCreate = masterOrganizationKYCs.Service.create
        )

        def getAllDocuments(id: String) = masterOrganizationKYCs.Service.getAllDocuments(id)

        (for {
          id <- id
          _ <- storeFile(id)
          documents <- getAllDocuments(id)
        } yield PartialContent(views.html.component.master.userUploadOrUpdateOrganizationKYC(documents))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }

      def userUpdateOrganizationKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
        Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.userUploadOrganizationKyc), utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.userUpdateOrganizationKyc), documentType))
      }

      def userUpdateOrganizationKyc(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
        implicit request =>

          val organizationID = masterOrganizations.Service.getID(loginState.username)
          def oldDocumentFileName(organizationID:String)=masterOrganizationKYCs.Service.getFileName(id = organizationID, documentType = documentType)
          def updateFile(organizationID: String,oldDocumentFileName:String) = fileResourceManager.updateFile[master.OrganizationKYC](
            name = name,
            documentType = documentType,
            path = fileResourceManager.getOrganizationKycFilePath(documentType),
            oldDocumentFileName = oldDocumentFileName,
            document = master.OrganizationKYC(id = organizationID, documentType = documentType, status = None, fileName = name, file = None),
            updateOldDocument = masterOrganizationKYCs.Service.updateOldDocument
          )

          def getAllDocuments(id: String) = masterOrganizationKYCs.Service.getAllDocuments(id)

          (for {
            organizationID <- organizationID
            oldDocumentFileName<-oldDocumentFileName(organizationID)
            _ <- updateFile(organizationID,oldDocumentFileName)
            documents <- getAllDocuments(organizationID)
          } yield PartialContent(views.html.component.master.userUploadOrUpdateOrganizationKYC(documents))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
      }


      def userAccessedOrganizationKYCFile(documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
        implicit request =>

          val id = masterOrganizations.Service.getID(loginState.username)

          def fileName(id: String) = masterOrganizationKYCs.Service.getFileName(id, documentType)

          for {
            id <- id
            fileName2 <- fileName(id)
          } yield Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getOrganizationKycFilePath(documentType), fileName = fileName2))
      }

      def reviewOrganizationCompletionForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
        implicit request =>
          val organization = masterOrganizations.Service.getByAccountID(loginState.username)

          def getResult(organization: Organization) = {
            val zone = masterZones.Service.get(organization.zoneID)
            val organizationBankAccountDetail = masterOrganizationBankAccountDetails.Service.get(organization.id)
            val organizationKYCs = masterOrganizationKYCs.Service.getAllDocuments(organization.id)
            for {
              zone <- zone
              organizationBankAccountDetail <- organizationBankAccountDetail
              organizationKYCs <- organizationKYCs
            } yield {
              Ok(views.html.component.master.reviewOrganizationCompletion(views.companion.master.OrganizationCompletion.form, organization = organization, zone = zone, organizationBankAccountDetail = organizationBankAccountDetail, organizationKYCs = organizationKYCs))
            }
          }

          (
            for {
              organization <- organization
              result <- getResult(organization)
            } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }

      }

      def reviewOrganizationCompletion(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
        implicit request =>
          views.companion.master.OrganizationCompletion.form.bindFromRequest().fold(
            formWithErrors => {
              val organization = masterOrganizations.Service.getByAccountID(loginState.username)

              def getResult(organization: Organization) = {
                val zone = masterZones.Service.get(organization.zoneID)
                val organizationBankAccountDetail = masterOrganizationBankAccountDetails.Service.get(organization.id)
                val organizationKYCs = masterOrganizationKYCs.Service.getAllDocuments(organization.id)
                for {
                  zone <- zone
                  organizationBankAccountDetail <- organizationBankAccountDetail
                  organizationKYCs <- organizationKYCs
                } yield {
                  BadRequest(views.html.component.master.reviewOrganizationCompletion(formWithErrors, organization = organization, zone = zone, organizationBankAccountDetail = organizationBankAccountDetail, organizationKYCs = organizationKYCs))
                }
              }

              (for {
                organization <- organization; result <- getResult(organization)
              } yield result).recover {
                case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
              }
            },
            reviewOrganizationCompletionData => {

              val id = masterOrganizations.Service.getID(loginState.username)

              def checkAllKYCFileTypesExists(id: String) = masterOrganizationKYCs.Service.checkAllKYCFileTypesExists(id)

              def getResult(id: String, checkAllKYCFileTypesExists: Boolean) = {
                if (reviewOrganizationCompletionData.completion && checkAllKYCFileTypesExists) {
                  masterOrganizations.Service.markOrganizationFormCompleted(id).map { _ =>
                    withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ORGANIZATION_ADDED_FOR_VERIFICATION)))
                  }
                } else {
                  masterOrganizations.Service.getByAccountID(loginState.username).flatMap { organization =>
                    val zone = masterZones.Service.get(organization.zoneID)
                    val organizationBankAccountDetail = masterOrganizationBankAccountDetails.Service.get(organization.id)
                    val organizationKYCs = masterOrganizationKYCs.Service.getAllDocuments(organization.id)
                    for {
                      zone <- zone
                      organizationBankAccountDetail <- organizationBankAccountDetail
                      organizationKYCs <- organizationKYCs
                    } yield BadRequest(views.html.component.master.reviewOrganizationCompletion(views.companion.master.OrganizationCompletion.form, organization = organization, zone = zone, organizationBankAccountDetail = organizationBankAccountDetail, organizationKYCs = organizationKYCs))
                  }
                }
              }

              (for {
                id <- id
                checkAllKYCFileTypesExists <- checkAllKYCFileTypesExists(id)
                result <- getResult(id, checkAllKYCFileTypesExists)
              } yield result).recover {
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
              Future {
                BadRequest(views.html.component.master.verifyOrganization(formWithErrors, formWithErrors.data(constants.Form.ORGANIZATION_ID), formWithErrors.data(constants.Form.ZONE_ID)))
              }
            },
            verifyOrganizationData => {

              masterOrganizationKYCs.Service.checkAllKYCFilesVerified(verifyOrganizationData.organizationID).flatMap { checkAllKYCFilesVerified =>
                if (checkAllKYCFilesVerified) {
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
            }
          )
      }

      def viewKycDocuments(organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
        implicit request =>
          masterOrganizationKYCs.Service.getAllDocuments(organizationID).map { documents =>
            withUsernameToken.Ok(views.html.component.master.viewVerificationOrganizationKycDouments(documents))
          }.recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
      }
  }

  def verifyKycDocument(organizationID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val verify = masterOrganizationKYCs.Service.verify(id = organizationID, documentType = documentType)

      def accountID = masterOrganizations.Service.getAccountId(organizationID)

      (for {
        _ <- verify
        accountID <- accountID
      } yield {
        utilitiesNotification.send(accountID = accountID, notification = constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
        withUsernameToken.Ok(Messages(constants.Response.SUCCESS.message))
      }).recover {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def rejectKycDocument(organizationID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val reject = masterOrganizationKYCs.Service.reject(id = organizationID, documentType = documentType)
      def accountID = masterOrganizations.Service.getAccountId(organizationID)
      (for {
        _ <- reject
        accountID <- accountID
      } yield {
        utilitiesNotification.send(accountID = accountID, notification = constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
        withUsernameToken.Ok(Messages(constants.Response.SUCCESS.message))
      }).recover {
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
          Future {
            BadRequest(views.html.component.master.rejectVerifyOrganizationRequest(formWithErrors, formWithErrors.data(constants.Form.ORGANIZATION_ID)))
          }
        },
        rejectVerifyOrganizationRequestData => {

          val rejectOrganization = masterOrganizations.Service.rejectOrganization(rejectVerifyOrganizationRequestData.organizationID)
          val accountID = masterOrganizations.Service.getAccountId(rejectVerifyOrganizationRequestData.organizationID)

          def rejectAll(accountId: String) = masterOrganizationKYCs.Service.rejectAll(accountId)

          (for {
            _ <- rejectOrganization
            accountID <- accountID
            _ <- rejectAll(accountID)
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
      } yield withUsernameToken.Ok(views.html.component.master.viewVerificationOrganizationBankAccountDetail(organizationBankAccountDetail))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def viewPendingVerifyOrganizationRequests: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID=masterZones.Service.getZoneId(loginState.username)
      def verifyOrganizationRequests(zoneID:String)= masterOrganizations.Service.getVerifyOrganizationRequests(zoneID)
      (for{
        zoneID<-zoneID
        verifyOrganizationRequests<-verifyOrganizationRequests(zoneID)
      }yield Ok(views.html.component.master.viewPendingVerifyOrgnizationRequests(verifyOrganizationRequests))
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def uploadOrganizationKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.uploadOrganizationKyc), utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.storeOrganizationKyc), documentType))
  }

  def updateOrganizationKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.uploadOrganizationKyc), utilities.String.getJsRouteFunction(routes.javascript.AddOrganizationController.updateOrganizationKyc), documentType))
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

     val id=masterOrganizations.Service.getID(loginState.username)
     def storeFile(id:String)=fileResourceManager.storeFile[master.OrganizationKYC](
       name = name,
       documentType = documentType,
       path = fileResourceManager.getOrganizationKycFilePath(documentType),
       document = master.OrganizationKYC(id = id, documentType = documentType, status = None, fileName = name, file = None),
       masterCreate = masterOrganizationKYCs.Service.create
     )
      (for{
      id<-id
      _<- storeFile(id)
      }yield withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }

  }

  def updateOrganizationKyc(name: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val organizationID = masterOrganizations.Service.getID(loginState.username)
      def oldDocumentFileName(organizationID:String)=masterOrganizationKYCs.Service.getFileName(id = organizationID, documentType = documentType)
      def updateFile(oldDocumentFileName:String,organizationID:String)=fileResourceManager.updateFile[master.OrganizationKYC](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getOrganizationKycFilePath(documentType),
        oldDocumentFileName = oldDocumentFileName,
        document = master.OrganizationKYC(id = organizationID, documentType = documentType, status = None, fileName = name, file = None),
        updateOldDocument = masterOrganizationKYCs.Service.updateOldDocument
      )

      (for{
      organizationID<- organizationID
      oldDocumentFileName<-oldDocumentFileName(organizationID)
      _<- updateFile(oldDocumentFileName,organizationID)
    }yield  withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def viewOrganizationsInZone: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>

        val zoneID=masterZones.Service.getZoneId(loginState.username)
        def organizationsInZone(zoneID:String)=masterOrganizations.Service.getOrganizationsInZone(zoneID)
      (for{
         zoneID<- zoneID
         organizationsInZone<- organizationsInZone(zoneID)
       }yield  Ok(views.html.component.master.viewOrganizationsInZone(organizationsInZone))
      ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def viewOrganizationsInZoneForGenesis(zoneID: String): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val organizationsInZone=masterOrganizations.Service.getOrganizationsInZone(zoneID)
      (for{
      organizationsInZone<-organizationsInZone
    }yield Ok(views.html.component.master.viewOrganizationsInZoneForGenesis(organizationsInZone))
    ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def blockchainAddOrganizationForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.addOrganization(views.companion.blockchain.AddOrganization.form))
  }

  def blockchainAddOrganization: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.AddOrganization.form.bindFromRequest().fold(
      formWithErrors => {
        Future{BadRequest(views.html.component.blockchain.addOrganization(formWithErrors))}
      },
      addOrganizationData => {
        val transactionsAddOrganizationPost=transactionsAddOrganization.Service.post(transactionsAddOrganization.Request(transactionsAddOrganization.BaseReq(from = addOrganizationData.from, gas = addOrganizationData.gas.toString), to = addOrganizationData.to, organizationID = addOrganizationData.organizationID, zoneID = addOrganizationData.zoneID, password = addOrganizationData.password, mode = transactionMode))
        (for{
          _<- transactionsAddOrganizationPost
        } yield Ok(views.html.index(successes = Seq(constants.Response.ORGANIZATION_ADDED)))
          ).recover{
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }

      }
    )
  }
}