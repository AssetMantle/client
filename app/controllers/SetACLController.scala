package controllers

import java.nio.file.Files

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.{Contact, Identification, Organization, Trader, TraderKYC}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import play.api.{Configuration, Logger}
import views.companion.master.FileUpload
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SetACLController @Inject()(
                                  messagesControllerComponents: MessagesControllerComponents,
                                  withTraderLoginAction: WithTraderLoginAction,
                                  transaction: utilities.Transaction,
                                  masterTransactionTraderInvitations: masterTransaction.TraderInvitations,
                                  masterContacts: master.Contacts,
                                  fileResourceManager: utilities.FileResourceManager,
                                  masterZones: master.Zones,
                                  masterOrganizations: master.Organizations,
                                  masterIdentifications: master.Identifications,
                                  masterTraders: master.Traders,
                                  masterTraderKYCs: master.TraderKYCs,
                                  withZoneLoginAction: WithZoneLoginAction,
                                  withOrganizationLoginAction: WithOrganizationLoginAction,
                                  withUserLoginAction: WithUserLoginAction,
                                  withLoginAction: WithLoginAction,
                                  withGenesisLoginAction: WithGenesisLoginAction,
                                  masterAccounts: master.Accounts,
                                  transactionsSetACL: transactions.SetACL,
                                  blockchainAclAccounts: blockchain.ACLAccounts,
                                  blockchainTransactionSetACLs: blockchainTransaction.SetACLs,
                                  blockchainAclHashes: blockchain.ACLHashes,
                                  utilitiesNotification: utilities.Notification,
                                  withUsernameToken: WithUsernameToken,
                                  masterTraderBackgroundChecks: master.TraderBackgroundChecks
                                )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_SET_ACL

  private val comdexURL: String = configuration.get[String]("comdex.url")

  def inviteTraderForm(): Action[AnyContent] = Action {
    implicit request =>
      Ok(views.html.component.master.inviteTrader())
  }

  def inviteTrader(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.InviteTrader.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.inviteTrader(formWithErrors)))
        },
        inviteTraderData => {

          val contact: Future[Option[Contact]] = masterContacts.Service.getContactByEmail(inviteTraderData.emailAddress)

          def inviteeUserType(contact: Option[Contact]): Future[String] = if (contact.isDefined) {
            masterAccounts.Service.getUserType(contact.get.id)
          } else {
            Future(constants.User.USER)
          }

          def createSendInvitationAndGetResult(inviteeUserType: String): Future[Result] = {
            if (inviteeUserType != constants.User.USER) {
              Future(BadRequest(views.html.account(failures = Seq(constants.Response.EMAIL_ADDRESS_ALREADY_IN_USE))))
            } else {

              val organization = masterOrganizations.Service.tryGetByAccountID(loginState.username)

              def createInvitation(organization: Organization): Future[String] = masterTransactionTraderInvitations.Service.create(organizationID = organization.id, inviteeEmailAddress = inviteTraderData.emailAddress)

              def sendEmailAndGetResult(organization: Organization): Future[Result] = {
                utilitiesNotification.sendEmailToEmailAddress(fromAccountID = loginState.username, toEmailAddress = inviteTraderData.emailAddress, email = constants.Notification.TRADER_INVITATION, inviteTraderData.name, organization.name, organization.id, comdexURL)
                withUsernameToken.Ok(views.html.account(successes = Seq(constants.Response.INVITATION_EMAIL_SENT)))
              }

              for {
                organization <- organization
                _ <- createInvitation(organization)
                _ <- utilitiesNotification.send(accountID = organization.accountID, notification = constants.Notification.ORGANIZATION_TRADER_INVITATION)
                result <- sendEmailAndGetResult(organization)
              } yield result
            }

          }

          (for {
            contact <- contact
            inviteeUserType <- inviteeUserType(contact)
            result <- createSendInvitationAndGetResult(inviteeUserType)
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def addTraderForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)
      (for {
        trader <- trader
      } yield Ok(views.html.component.master.addTrader(views.companion.master.AddTrader.form.fill(views.companion.master.AddTrader.Data(organizationID = trader.organizationID))))
        ).recover {
        case _: BaseException => Ok(views.html.component.master.addTrader())
      }
  }

  def addTrader(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddTrader.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.addTrader(formWithErrors)))
        },
        addTraderData => {
          val status = masterOrganizations.Service.getVerificationStatus(addTraderData.organizationID)

          def insertOrUpdateAndGetResult(status: Boolean): Future[Result] = {
            if (status) {
              val name = masterIdentifications.Service.tryGetName(loginState.username)
              val organization: Future[Organization] = masterOrganizations.Service.tryGet(addTraderData.organizationID)

              def addTrader(name: String, zoneID: String): Future[String] = masterTraders.Service.insertOrUpdate(zoneID, addTraderData.organizationID, loginState.username, name)

              val emailAddress: Future[Option[String]] = masterContacts.Service.getVerifiedEmailAddress(loginState.username)

              def updateInvitationStatus(emailAddress: Option[String]): Future[Int] = if (emailAddress.isDefined) {
                masterTransactionTraderInvitations.Service.updateStatusByEmailAddress(organizationID = addTraderData.organizationID, emailAddress = emailAddress.get, status = constants.Status.TraderInvitation.IDENTIFICATION_COMPLETE_DOCUMENT_UPLOAD_PENDING)
              } else {
                Future(0)
              }

              def getTraderKYCs(id: String): Future[Seq[TraderKYC]] = masterTraderKYCs.Service.getAllDocuments(id)

              for {
                name <- name
                organization <- organization
                id <- addTrader(name = name, zoneID = organization.zoneID)
                emailAddress <- emailAddress
                _ <- updateInvitationStatus(emailAddress)
                traderKYCs <- getTraderKYCs(id)
                result <- withUsernameToken.PartialContent(views.html.component.master.userUploadOrUpdateTraderKYC(traderKYCs))
              } yield result
            } else {
              Future(Unauthorized(views.html.profile(failures = Seq(constants.Response.UNVERIFIED_ORGANIZATION))))
            }
          }

          (for {
            status <- status
            result <- insertOrUpdateAndGetResult(status)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def userUploadOrUpdateTraderKYCView(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val id = masterTraders.Service.tryGetID(loginState.username)

      def getTraderKYCs(id: String): Future[Seq[TraderKYC]] = masterTraderKYCs.Service.getAllDocuments(id)

      (for {
        id <- id
        traderKYCs <- getTraderKYCs(id)
      } yield Ok(views.html.component.master.userUploadOrUpdateTraderKYC(traderKYCs))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def userUploadTraderKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.SetACLController.userUploadTraderKYC), utilities.String.getJsRouteFunction(routes.javascript.SetACLController.userStoreTraderKYC), documentType))
  }

  def userUploadTraderKYC(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.profile(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getTraderKYCFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def userStoreTraderKYC(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val id = masterTraders.Service.tryGetID(loginState.username)

      def storeFile(id: String): Future[Boolean] = fileResourceManager.storeFile[master.TraderKYC](
        name = name,
        path = fileResourceManager.getTraderKYCFilePath(documentType),
        document = master.TraderKYC(id = id, documentType = documentType, fileName = name, file = None, zoneStatus = None, organizationStatus = None),
        masterCreate = masterTraderKYCs.Service.create
      )

      def allDocuments(id: String): Future[Seq[TraderKYC]] = masterTraderKYCs.Service.getAllDocuments(id)

      (for {
        id <- id
        _ <- storeFile(id)
        allDocuments <- allDocuments(id)
        result <- withUsernameToken.PartialContent(views.html.component.master.userUploadOrUpdateTraderKYC(allDocuments))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def userUpdateTraderKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.SetACLController.userUploadTraderKYC), utilities.String.getJsRouteFunction(routes.javascript.SetACLController.userUpdateTraderKYC), documentType))
  }

  def userUpdateTraderKYC(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val id = masterTraders.Service.tryGetID(loginState.username)

      def getOldDocumentFileName(id: String): Future[String] = masterTraderKYCs.Service.getFileName(id = id, documentType = documentType)

      def updateFile(id: String, oldDocumentFileName: String): Future[Boolean] = fileResourceManager.updateFile[master.TraderKYC](
        name = name,
        path = fileResourceManager.getTraderKYCFilePath(documentType),
        oldDocumentFileName = oldDocumentFileName,
        document = master.TraderKYC(id = id, documentType = documentType, fileName = name, file = None, zoneStatus = None, organizationStatus = None),
        updateOldDocument = masterTraderKYCs.Service.updateOldDocument
      )

      def allDocuments(id: String): Future[Seq[TraderKYC]] = masterTraderKYCs.Service.getAllDocuments(id)

      (for {
        id <- id
        oldDocumentFileName <- getOldDocumentFileName(id)
        _ <- updateFile(id, oldDocumentFileName)
        allDocuments <- allDocuments(id)
        result <- withUsernameToken.PartialContent(views.html.component.master.userUploadOrUpdateTraderKYC(allDocuments))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def userReviewAddTraderRequestForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)

      def getResult(trader: Trader): Future[Result] = {
        val organization = masterOrganizations.Service.tryGet(trader.organizationID)
        val zone = masterZones.Service.get(trader.zoneID)
        val traderKYCs = masterTraderKYCs.Service.getAllDocuments(trader.id)
        for {
          organization <- organization
          zone <- zone
          traderKYCs <- traderKYCs
          result <- withUsernameToken.Ok(views.html.component.master.userReviewAddTraderRequest(trader = trader, organization = organization, zone = zone, traderKYCs = traderKYCs))
        } yield result
      }

      (for {
        trader <- trader
        result <- getResult(trader)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def userReviewAddTraderRequest(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ReviewAddTraderRequest.form.bindFromRequest().fold(
        formWithErrors => {
          val trader = masterTraders.Service.tryGetByAccountID(loginState.username)

          def getResult(trader: Trader): Future[Result] = {
            val organization = masterOrganizations.Service.tryGet(trader.organizationID)
            val zone = masterZones.Service.get(trader.zoneID)
            val traderKYCs = masterTraderKYCs.Service.getAllDocuments(trader.id)
            for {
              organization <- organization
              zone <- zone
              traderKYCs <- traderKYCs
            } yield BadRequest(views.html.component.master.userReviewAddTraderRequest(formWithErrors, trader = trader, organization = organization, zone = zone, traderKYCs = traderKYCs))
          }

          (for {
            trader <- trader
            result <- getResult(trader)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
          }
        },
        userReviewAddTraderRequestData => {
          val trader = masterTraders.Service.tryGetByAccountID(loginState.username)

          def allKYCFileTypesExists(id: String): Future[Boolean] = masterTraderKYCs.Service.checkAllKYCFileTypesExists(id)

          def organization(organizationID: String): Future[Organization] = masterOrganizations.Service.tryGet(organizationID)

          def getResult(trader: Trader, allKYCFileTypesExists: Boolean, traderOrganization: Organization): Future[Result] = {
            if (userReviewAddTraderRequestData.completion && allKYCFileTypesExists) {
              val markTraderFormCompleted = masterTraders.Service.markTraderFormCompleted(trader.id)

              val emailAddress: Future[Option[String]] = masterContacts.Service.getVerifiedEmailAddress(loginState.username)

              def updateInvitationStatus(emailAddress: Option[String]): Future[Int] = if (emailAddress.isDefined) {
                masterTransactionTraderInvitations.Service.updateStatusByEmailAddress(organizationID = traderOrganization.id, emailAddress = emailAddress.get, status = constants.Status.TraderInvitation.TRADER_ADDED_FOR_VERIFICATION)
              } else {
                Future(0)
              }

              def getResult(traderOrganization: Organization, trader: Trader): Future[Result] = {
                withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.TRADER_ADDED_FOR_VERIFICATION)))
              }

              for {
                _ <- markTraderFormCompleted
                emailAddress <- emailAddress
                _ <- updateInvitationStatus(emailAddress)
                _ <- utilitiesNotification.send(traderOrganization.accountID, constants.Notification.ORGANIZATION_USER_ADDED_OR_UPDATED_TRADER_REQUEST, trader.name)
                _ <- utilitiesNotification.send(loginState.username, constants.Notification.USER_ADDED_OR_UPDATED_TRADER_REQUEST, traderOrganization.name, traderOrganization.id)
                result <- getResult(traderOrganization, trader)
              } yield result
            } else {

              val zone = masterZones.Service.get(trader.zoneID)
              val traderKYCs = masterTraderKYCs.Service.getAllDocuments(trader.id)

              for {
                zone <- zone
                traderKYCs <- traderKYCs
              } yield BadRequest(views.html.component.master.userReviewAddTraderRequest(trader = trader, organization = traderOrganization, zone = zone, traderKYCs = traderKYCs))
            }
          }

          (for {
            trader <- trader
            allKYCFileTypesExists <- allKYCFileTypesExists(trader.id)
            traderOrganization <- organization(trader.organizationID)
            result <- getResult(trader, allKYCFileTypesExists, traderOrganization)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def zoneVerifyTraderForm(traderID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGet(traderID)
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      (for {
        trader <- trader
        zoneID <- zoneID
      } yield if (trader.zoneID == zoneID) {
        Ok(views.html.component.master.zoneVerifyTrader(views.companion.master.VerifyTrader.form, trader))
      } else {
        Unauthorized(views.html.account(failures = Seq(constants.Response.UNAUTHORIZED)))
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def zoneVerifyTrader: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyTrader.form.bindFromRequest().fold(
        formWithErrors => {
          val trader = masterTraders.Service.tryGetByAccountID(formWithErrors.data(constants.FormField.ACCOUNT_ID.name))
          (for {
            trader <- trader
          } yield if (trader.organizationID == formWithErrors.data(constants.FormField.ORGANIZATION_ID.name)) {
            BadRequest(views.html.component.master.zoneVerifyTrader(formWithErrors, trader))
          } else {
            Unauthorized(views.html.account(failures = Seq(constants.Response.UNAUTHORIZED)))
          }
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        },
        verifyTraderData => {
          val organizationVerificationStatus = masterOrganizations.Service.tryGetVerificationStatus(verifyTraderData.organizationID)
          val zoneID = masterZones.Service.tryGetID(loginState.username)
          val trader = masterTraders.Service.tryGetByAccountID(verifyTraderData.accountID)

          def checkAllKYCFilesVerified(trader: Trader, zoneID: String): Future[Boolean] = {
            if (trader.zoneID != zoneID) throw new BaseException(constants.Response.UNAUTHORIZED)
            if (trader.organizationID != verifyTraderData.organizationID) throw new BaseException(constants.Response.ORGANIZATION_ID_MISMATCH)
            masterTraderKYCs.Service.checkAllKYCFilesVerified(trader.id)
          }

          def checkAllBackgroundFilesVerified(id: String): Future[Boolean] = masterTraderBackgroundChecks.Service.checkAllBackgroundFilesVerified(id)

          def processTransactionAndGetResult(trader: Trader, organizationVerificationStatus: Boolean, checkAllKYCFilesVerified: Boolean, checkAllBackgroundFilesVerified: Boolean): Future[Result] = {
            if (!checkAllBackgroundFilesVerified) throw new BaseException(constants.Response.ALL_TRADER_BACKGROUND_CHECK_FILES_NOT_VERFIED)
            if (organizationVerificationStatus && checkAllKYCFilesVerified) {
              val aclAddress = masterAccounts.Service.getAddress(verifyTraderData.accountID)
              val acl = blockchain.ACL(issueAsset = verifyTraderData.issueAsset, issueFiat = verifyTraderData.issueFiat, sendAsset = verifyTraderData.sendAsset, sendFiat = verifyTraderData.sendFiat, redeemAsset = verifyTraderData.redeemAsset, redeemFiat = verifyTraderData.redeemFiat, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder, changeBuyerBid = verifyTraderData.changeBuyerBid, changeSellerBid = verifyTraderData.changeSellerBid, confirmBuyerBid = verifyTraderData.confirmBuyerBid, confirmSellerBid = verifyTraderData.changeSellerBid, negotiation = verifyTraderData.negotiation, releaseAsset = verifyTraderData.releaseAsset)
              val createACL = blockchainAclHashes.Service.create(acl)

              def transactionProcess(aclAddress: String, zoneID: String): Future[String] = transaction.process[blockchainTransaction.SetACL, transactionsSetACL.Request](
                entity = blockchainTransaction.SetACL(from = loginState.address, aclAddress = aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, aclHash = util.hashing.MurmurHash3.stringHash(acl.toString).toString, gas = verifyTraderData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionSetACLs.Service.create,
                request = transactionsSetACL.Request(transactionsSetACL.BaseReq(from = loginState.address, gas = verifyTraderData.gas.toString), password = verifyTraderData.password, aclAddress = aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, issueAsset = verifyTraderData.issueAsset.toString, issueFiat = verifyTraderData.issueFiat.toString, sendAsset = verifyTraderData.sendAsset.toString, sendFiat = verifyTraderData.sendFiat.toString, redeemAsset = verifyTraderData.redeemAsset.toString, redeemFiat = verifyTraderData.redeemFiat.toString, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder.toString, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder.toString, changeBuyerBid = verifyTraderData.changeBuyerBid.toString, changeSellerBid = verifyTraderData.changeSellerBid.toString, confirmBuyerBid = verifyTraderData.confirmBuyerBid.toString, confirmSellerBid = verifyTraderData.confirmSellerBid.toString, negotiation = verifyTraderData.negotiation.toString, releaseAsset = verifyTraderData.releaseAsset.toString, mode = transactionMode),
                action = transactionsSetACL.Service.post,
                onSuccess = blockchainTransactionSetACLs.Utility.onSuccess,
                onFailure = blockchainTransactionSetACLs.Utility.onFailure,
                updateTransactionHash = blockchainTransactionSetACLs.Service.updateTransactionHash
              )

              for {
                aclAddress <- aclAddress
                _ <- createACL
                _ <- transactionProcess(aclAddress = aclAddress, zoneID = trader.zoneID)
                result <- withUsernameToken.Ok(views.html.account(successes = Seq(constants.Response.ACL_SET)))
              } yield result
            } else {
              Future(PreconditionFailed(views.html.account(failures = Seq(constants.Response.ALL_KYC_FILES_NOT_VERIFIED))))
            }
          }

          (for {
            organizationVerificationStatus <- organizationVerificationStatus
            zoneID <- zoneID
            trader <- trader
            checkAllKYCFilesVerified <- checkAllKYCFilesVerified(trader, zoneID)
            checkAllBackgroundFilesVerified <- checkAllBackgroundFilesVerified(trader.id)
            result <- processTransactionAndGetResult(trader = trader, organizationVerificationStatus = organizationVerificationStatus, checkAllKYCFilesVerified = checkAllKYCFilesVerified, checkAllBackgroundFilesVerified = checkAllBackgroundFilesVerified)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def zoneViewPendingVerifyTraderRequests: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def getVerifyTraderRequestsForZone(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneVerifyTraderRequestList(zoneID)

      for {
        zoneID <- zoneID
        verifyTraderRequestsForZone <- getVerifyTraderRequestsForZone(zoneID)
      } yield Ok(views.html.component.master.zoneViewPendingVerifyTraderRequests(verifyTraderRequestsForZone))
  }

  def zoneViewKYCDocuments(traderID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderKYCs = masterTraderKYCs.Service.getAllDocuments(traderID)
      (for {
        traderKYCs <- traderKYCs
      } yield Ok(views.html.component.master.zoneViewVerificationTraderKYCDouments(traderKYCs))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def zoneAcceptOrRejectTraderKYCDocumentForm(traderID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val userZoneID = masterZones.Service.tryGetID(loginState.username)
      val traderZoneID = masterTraders.Service.tryGetZoneID(traderID)

      def getResult(userZoneID: String, traderZoneID: String): Future[Result] = {
        if (userZoneID == traderZoneID) {
          val traderKYC = masterTraderKYCs.Service.get(id = traderID, documentType = documentType)
          for {
            traderKYC <- traderKYC
          } yield Ok(views.html.component.master.zoneAcceptOrRejectTraderKYCDocument(traderKYC = traderKYC))
        } else Future(Unauthorized(views.html.account(failures = Seq(constants.Response.UNAUTHORIZED))))
      }

      (for {
        userZoneID <- userZoneID
        traderZoneID <- traderZoneID
        result <- getResult(userZoneID, traderZoneID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def zoneAcceptOrRejectTraderKYCDocument(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ZoneAcceptOrRejectTraderKYCDocument.form.bindFromRequest().fold(
        formWithErrors => {
          val traderKYC = masterTraderKYCs.Service.get(id = formWithErrors(constants.FormField.TRADER_ID.name).value.get, documentType = formWithErrors(constants.FormField.DOCUMENT_TYPE.name).value.get)
          (for {
            traderKYC <- traderKYC
          } yield BadRequest(views.html.component.master.zoneAcceptOrRejectTraderKYCDocument(formWithErrors, traderKYC))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        },
        zoneAcceptOrRejectTraderKYCDocumentData => {
          val userZoneID = masterZones.Service.tryGetID(loginState.username)
          val traderZoneID = masterTraders.Service.tryGetZoneID(zoneAcceptOrRejectTraderKYCDocumentData.traderID)

          def getResult(userZoneID: String, traderZoneID: String): Future[Result] = {
            if (userZoneID == traderZoneID) {
              val verifyOrReject = if (zoneAcceptOrRejectTraderKYCDocumentData.zoneStatus) {
                val zoneVerify = masterTraderKYCs.Service.zoneVerify(id = zoneAcceptOrRejectTraderKYCDocumentData.traderID, documentType = zoneAcceptOrRejectTraderKYCDocumentData.documentType)
                val traderID = masterTraders.Service.tryGetAccountId(zoneAcceptOrRejectTraderKYCDocumentData.traderID)
                for {
                  _ <- zoneVerify
                  traderID <- traderID
                  _ <- utilitiesNotification.send(traderID, constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
                } yield {}
              } else {
                val zoneReject = masterTraderKYCs.Service.zoneReject(id = zoneAcceptOrRejectTraderKYCDocumentData.traderID, documentType = zoneAcceptOrRejectTraderKYCDocumentData.documentType)
                val traderID = masterTraders.Service.tryGetAccountId(zoneAcceptOrRejectTraderKYCDocumentData.traderID)
                for {
                  _ <- zoneReject
                  traderID <- traderID
                  _ <- utilitiesNotification.send(traderID, constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
                } yield {}
              }

              def traderKYC: Future[TraderKYC] = masterTraderKYCs.Service.get(id = zoneAcceptOrRejectTraderKYCDocumentData.traderID, documentType = zoneAcceptOrRejectTraderKYCDocumentData.documentType)

              for {
                _ <- verifyOrReject
                traderKYC <- traderKYC
                result <- withUsernameToken.PartialContent(views.html.component.master.zoneAcceptOrRejectTraderKYCDocument(traderKYC = traderKYC))
              } yield result
            } else {
              Future(Unauthorized(views.html.account(failures = Seq(constants.Response.UNAUTHORIZED))))
            }
          }

          (for {
            userZoneID <- userZoneID
            traderZoneID <- traderZoneID
            result <- getResult(userZoneID, traderZoneID)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def organizationVerifyTraderForm(traderID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGet(traderID)
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      (for {
        trader <- trader
        organizationID <- organizationID
      } yield if (trader.organizationID == organizationID) {
        Ok(views.html.component.master.organizationVerifyTrader(views.companion.master.VerifyTrader.form, trader))
      } else {
        Unauthorized(views.html.account(failures = Seq(constants.Response.UNAUTHORIZED)))
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def organizationVerifyTrader: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyTrader.form.bindFromRequest().fold(
        formWithErrors => {
          val trader = masterTraders.Service.tryGetByAccountID(formWithErrors.data(constants.FormField.ACCOUNT_ID.name))

          (for {
            trader <- trader
          } yield if (trader.organizationID == formWithErrors.data(constants.FormField.ORGANIZATION_ID.name)) {
            BadRequest(views.html.component.master.organizationVerifyTrader(formWithErrors, trader))
          } else {
            Unauthorized(views.html.account(failures = Seq(constants.Response.UNAUTHORIZED)))
          }).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        },
        verifyTraderData => {
          val trader = masterTraders.Service.tryGetByAccountID(verifyTraderData.accountID)
          val traderOrganization = masterOrganizations.Service.tryGet(verifyTraderData.organizationID)
          val organization = masterOrganizations.Service.tryGetByAccountID(loginState.username)

          def checkAllKYCFilesVerified(trader: Trader, traderOrganization: Organization, organization: Organization): Future[Boolean] = {
            if (trader.organizationID != verifyTraderData.organizationID || traderOrganization.id != organization.id) throw new BaseException(constants.Response.UNAUTHORIZED)
            if (trader.zoneID != traderOrganization.zoneID) throw new BaseException(constants.Response.ZONE_ID_MISMATCH)
            masterTraderKYCs.Service.checkAllKYCFilesVerified(trader.id)
          }

          def checkAllBackgroundFilesVerified(id: String): Future[Boolean] = masterTraderBackgroundChecks.Service.checkAllBackgroundFilesVerified(id)

          def getResult(checkAllKYCFilesVerified: Boolean, checkAllBackgroundFilesVerified: Boolean, trader: Trader): Future[Result] = {
            if (!checkAllBackgroundFilesVerified) throw new BaseException(constants.Response.ALL_TRADER_BACKGROUND_CHECK_FILES_NOT_VERFIED)
            if (checkAllKYCFilesVerified) {
              val aclAddress = masterAccounts.Service.getAddress(verifyTraderData.accountID)
              val acl = blockchain.ACL(issueAsset = verifyTraderData.issueAsset, issueFiat = verifyTraderData.issueFiat, sendAsset = verifyTraderData.sendAsset, sendFiat = verifyTraderData.sendFiat, redeemAsset = verifyTraderData.redeemAsset, redeemFiat = verifyTraderData.redeemFiat, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder, changeBuyerBid = verifyTraderData.changeBuyerBid, changeSellerBid = verifyTraderData.changeSellerBid, confirmBuyerBid = verifyTraderData.confirmBuyerBid, confirmSellerBid = verifyTraderData.changeSellerBid, negotiation = verifyTraderData.negotiation, releaseAsset = verifyTraderData.releaseAsset)

              def createACL: Future[String] = blockchainAclHashes.Service.create(acl)

              def transactionProcess(aclAddress: String, zoneID: String): Future[String] = transaction.process[blockchainTransaction.SetACL, transactionsSetACL.Request](
                entity = blockchainTransaction.SetACL(from = loginState.address, aclAddress = aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, aclHash = util.hashing.MurmurHash3.stringHash(acl.toString).toString, gas = verifyTraderData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionSetACLs.Service.create,
                request = transactionsSetACL.Request(transactionsSetACL.BaseReq(from = loginState.address, gas = verifyTraderData.gas.toString), password = verifyTraderData.password, aclAddress = aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, issueAsset = verifyTraderData.issueAsset.toString, issueFiat = verifyTraderData.issueFiat.toString, sendAsset = verifyTraderData.sendAsset.toString, sendFiat = verifyTraderData.sendFiat.toString, redeemAsset = verifyTraderData.redeemAsset.toString, redeemFiat = verifyTraderData.redeemFiat.toString, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder.toString, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder.toString, changeBuyerBid = verifyTraderData.changeBuyerBid.toString, changeSellerBid = verifyTraderData.changeSellerBid.toString, confirmBuyerBid = verifyTraderData.confirmBuyerBid.toString, confirmSellerBid = verifyTraderData.confirmSellerBid.toString, negotiation = verifyTraderData.negotiation.toString, releaseAsset = verifyTraderData.releaseAsset.toString, mode = transactionMode),
                action = transactionsSetACL.Service.post,
                onSuccess = blockchainTransactionSetACLs.Utility.onSuccess,
                onFailure = blockchainTransactionSetACLs.Utility.onFailure,
                updateTransactionHash = blockchainTransactionSetACLs.Service.updateTransactionHash
              )

              for {
                aclAddress <- aclAddress
                _ <- createACL
                _ <- transactionProcess(aclAddress, trader.zoneID)
                result <- withUsernameToken.Ok(views.html.account(successes = Seq(constants.Response.ACL_SET)))
              } yield result
            } else {
              Future(PreconditionFailed(views.html.account(failures = Seq(constants.Response.ALL_KYC_FILES_NOT_VERIFIED))))
            }
          }

          (for {
            trader <- trader
            traderOrganization <- traderOrganization
            organization <- organization
            checkAllKYCFilesVerified <- checkAllKYCFilesVerified(trader, traderOrganization, organization)
            checkAllBackgroundFilesVerified <- checkAllBackgroundFilesVerified(trader.id)
            result <- getResult(checkAllKYCFilesVerified = checkAllKYCFilesVerified, checkAllBackgroundFilesVerified = checkAllBackgroundFilesVerified, trader = trader)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def organizationAcceptOrRejectTraderKYCDocumentForm(traderID: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val userOrganizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val traderOrganizationID = masterTraders.Service.tryGetOrganizationID(traderID)

      def getResult(userOrganizationID: String, traderOrganizationID: String): Future[Result] = {
        if (userOrganizationID == traderOrganizationID) {
          val traderKYC = masterTraderKYCs.Service.get(id = traderID, documentType = documentType)
          for {
            traderKYC <- traderKYC
          } yield Ok(views.html.component.master.organizationAcceptOrRejectTraderKYCDocument(traderKYC = traderKYC))
        } else Future(Unauthorized(views.html.account(failures = Seq(constants.Response.UNAUTHORIZED))))
      }

      (for {
        userOrganizationID <- userOrganizationID
        traderOrganizationID <- traderOrganizationID
        result <- getResult(userOrganizationID, traderOrganizationID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def organizationAcceptOrRejectTraderKYCDocument(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.OrganizationAcceptOrRejectTraderKYCDocument.form.bindFromRequest().fold(
        formWithErrors => {
          val traderKYC = masterTraderKYCs.Service.get(id = formWithErrors(constants.FormField.TRADER_ID.name).value.get, documentType = formWithErrors(constants.FormField.DOCUMENT_TYPE.name).value.get)
          (for {
            traderKYC <- traderKYC
          } yield BadRequest(views.html.component.master.organizationAcceptOrRejectTraderKYCDocument(formWithErrors, traderKYC))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        },
        organizationAcceptOrRejectTraderKYCDocumentData => {
          val userOrganizationID = masterOrganizations.Service.tryGetID(loginState.username)
          val traderOrganizationID = masterTraders.Service.tryGetOrganizationID(organizationAcceptOrRejectTraderKYCDocumentData.traderID)

          def verifyOrRejectAndGetResult(userOrganizationID: String, traderOrganizationID: String): Future[Result] = {
            if (userOrganizationID == traderOrganizationID) {
              val verifyOrReject = if (organizationAcceptOrRejectTraderKYCDocumentData.organizationStatus) {
                val organizationVerify = masterTraderKYCs.Service.organizationVerify(id = organizationAcceptOrRejectTraderKYCDocumentData.traderID, documentType = organizationAcceptOrRejectTraderKYCDocumentData.documentType)
                val traderID = masterTraders.Service.tryGetAccountId(organizationAcceptOrRejectTraderKYCDocumentData.traderID)
                for {
                  _ <- organizationVerify
                  traderID <- traderID
                  _ <- utilitiesNotification.send(traderID, constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
                } yield {}
              } else {
                val organizationReject = masterTraderKYCs.Service.organizationReject(id = organizationAcceptOrRejectTraderKYCDocumentData.traderID, documentType = organizationAcceptOrRejectTraderKYCDocumentData.documentType)
                val traderID = masterTraders.Service.tryGetAccountId(organizationAcceptOrRejectTraderKYCDocumentData.traderID)
                for {
                  _ <- organizationReject
                  traderID <- traderID
                  _ <- utilitiesNotification.send(traderID, constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
                } yield {}
              }

              def traderKYC: Future[TraderKYC] = masterTraderKYCs.Service.get(id = organizationAcceptOrRejectTraderKYCDocumentData.traderID, documentType = organizationAcceptOrRejectTraderKYCDocumentData.documentType)

              for {
                _ <- verifyOrReject
                traderKYC <- traderKYC
                result <- withUsernameToken.PartialContent(views.html.component.master.organizationAcceptOrRejectTraderKYCDocument(traderKYC = traderKYC))
              } yield result
            } else {
              Future(Unauthorized(views.html.account(failures = Seq(constants.Response.UNAUTHORIZED))))
            }
          }

          (for {
            userOrganizationID <- userOrganizationID
            traderOrganizationID <- traderOrganizationID
            result <- verifyOrRejectAndGetResult(userOrganizationID, traderOrganizationID)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def uploadTraderKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.SetACLController.uploadTraderKYC), utilities.String.getJsRouteFunction(routes.javascript.SetACLController.storeTraderKYC), documentType))
  }

  def uploadTraderKYC(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.account(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getTraderKYCFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeTraderKYC(name: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val id = masterTraders.Service.tryGetID(loginState.username)

      def storeFile(id: String): Future[Boolean] = fileResourceManager.storeFile[master.TraderKYC](
        name = name,
        path = fileResourceManager.getTraderKYCFilePath(documentType),
        document = master.TraderKYC(id = id, documentType = documentType, fileName = name, file = None, zoneStatus = None, organizationStatus = None),
        masterCreate = masterTraderKYCs.Service.create
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

  def updateTraderKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.SetACLController.uploadTraderKYC), utilities.String.getJsRouteFunction(routes.javascript.SetACLController.updateTraderKYC), documentType))
  }

  def updateTraderKYC(name: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val id = masterTraders.Service.tryGetID(loginState.username)

      def getOldDocumentFileName(id: String): Future[String] = masterTraderKYCs.Service.getFileName(id = id, documentType = documentType)

      def updateFile(id: String, oldDocumentFileName: String): Future[Boolean] = fileResourceManager.updateFile[master.TraderKYC](
        name = name,
        path = fileResourceManager.getTraderKYCFilePath(documentType),
        oldDocumentFileName = oldDocumentFileName,
        document = master.TraderKYC(id = id, documentType = documentType, fileName = name, file = None, zoneStatus = None, organizationStatus = None),
        updateOldDocument = masterTraderKYCs.Service.updateOldDocument
      )

      (for {
        id <- id
        oldDocumentFileName <- getOldDocumentFileName(id)
        _ <- updateFile(id, oldDocumentFileName)
        result <- withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def viewTradersInOrganizationForZone(organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationZoneID = masterOrganizations.Service.tryGetZoneID(organizationID)
      val userZoneID = masterZones.Service.tryGetID(loginState.username)

      def getResult(organizationZoneID: String, userZoneID: String): Future[Result] = {
        if (organizationZoneID == userZoneID) {
          val verifiedTradersForOrganization = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)
          for {
            verifiedTradersForOrganization <- verifiedTradersForOrganization
          } yield Ok(views.html.component.master.viewTradersInOrganization(verifiedTradersForOrganization))
        } else {
          Future(Unauthorized(views.html.account(failures = Seq(constants.Response.UNAUTHORIZED)))
          )
        }
      }

      (for {
        organizationZoneID <- organizationZoneID
        userZoneID <- userZoneID
        result <- getResult(organizationZoneID, userZoneID)
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def viewTradersInOrganizationForGenesis(organizationID: String): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val verifiedTradersForOrganization = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)
      for {
        verifiedTradersForOrganization <- verifiedTradersForOrganization
      } yield Ok(views.html.component.master.viewTradersInOrganization(verifiedTradersForOrganization))
  }

  def blockchainSetACLForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.setACL())
  }

  def blockchainSetACL: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.SetACL.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.setACL(formWithErrors)))
      },
      setACLData => {
        val postRequest = transactionsSetACL.Service.post(transactionsSetACL.Request(transactionsSetACL.BaseReq(from = setACLData.from, gas = setACLData.gas.toString), password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAsset = setACLData.redeemAsset.toString, redeemFiat = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAsset = setACLData.releaseAsset.toString, mode = transactionMode))
        (for {
          _ <- postRequest
        } yield Ok(views.html.index(successes = Seq(constants.Response.ACL_SET)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}