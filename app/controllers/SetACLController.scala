package controllers

import java.nio.file.Files

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.master.FileUpload

import scala.concurrent.ExecutionContext

@Singleton
class SetACLController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterTransactionAddTraderRequests: masterTransaction.AddTraderRequests, withTraderLoginAction: WithTraderLoginAction, transaction: utilities.Transaction, fileResourceManager: utilities.FileResourceManager, blockchainAccounts: blockchain.Accounts, masterZones: master.Zones, masterOrganizations: master.Organizations, masterTraders: master.Traders, masterTraderKYCs: master.TraderKYCs, withZoneLoginAction: WithZoneLoginAction, withOrganizationLoginAction: WithOrganizationLoginAction, withUserLoginAction: WithUserLoginAction, withGenesisLoginAction: WithGenesisLoginAction, masterAccounts: master.Accounts, transactionsSetACL: transactions.SetACL, blockchainAclAccounts: blockchain.ACLAccounts, blockchainTransactionSetACLs: blockchainTransaction.SetACLs, blockchainAclHashes: blockchain.ACLHashes, utilitiesNotification: utilities.Notification, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_SET_ACL

  def traderInvitationForm(): Action[AnyContent] = Action {
    implicit request =>
      Ok(views.html.component.master.traderInvitation(views.companion.master.TraderInvitation.form))
  }

  def traderInvitation(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.TraderInvitation.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.traderInvitation(formWithErrors))
        },
        addTraderRequestData => {
          try {
            val requestID = masterTransactionAddTraderRequests.Service.create(accountID = addTraderRequestData.accountID, organizationID = masterOrganizations.Service.getID(loginState.username))
            utilitiesNotification.send(accountID = addTraderRequestData.accountID, notification = constants.Notification.TRADER_INVITATION, routes.SetACLController.addTraderRequestForm(requestID).url)
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.INVITATION_EMAIL_SENT)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def addTraderRequestForm(requestID: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val addTraderRequest = masterTransactionAddTraderRequests.Service.get(requestID)
        if (addTraderRequest.accountID == loginState.username) {
          Ok(views.html.component.master.addTrader(views.companion.master.AddTrader.form.fill(views.companion.master.AddTrader.Data(zoneID = masterOrganizations.Service.getZoneID(addTraderRequest.organizationID), organizationID = addTraderRequest.organizationID, name = ""))))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def addTraderRequest(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddTrader.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.addTrader(formWithErrors))
        },
        addTraderData => {
          try {
            if (masterOrganizations.Service.getVerificationStatus(addTraderData.organizationID)) {
              val id = masterTraders.Service.insertOrUpdateTrader(zoneID = addTraderData.zoneID, organizationID = addTraderData.organizationID, accountID = loginState.username, name = addTraderData.name)
              PartialContent(views.html.component.master.userUploadOrUpdateTraderKYC(masterTraderKYCs.Service.getAllDocuments(id)))
            } else {
              Unauthorized(views.html.index(failures = Seq(constants.Response.UNVERIFIED_ORGANIZATION)))
            }
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def addTraderForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val trader = masterTraders.Service.getByAccountID(loginState.username)
        Ok(views.html.component.master.addTrader(views.companion.master.AddTrader.form.fill(views.companion.master.AddTrader.Data(zoneID = trader.zoneID, organizationID = trader.organizationID, name = trader.name))))
      } catch {
        case _: BaseException => Ok(views.html.component.master.addTrader(views.companion.master.AddTrader.form))
      }
  }

  def addTrader(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddTrader.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.addTrader(formWithErrors))
        },
        addTraderData => {
          try {
            if (masterOrganizations.Service.getVerificationStatus(addTraderData.organizationID)) {
              val id = masterTraders.Service.insertOrUpdateTrader(zoneID = addTraderData.zoneID, organizationID = addTraderData.organizationID, accountID = loginState.username, name = addTraderData.name)
              PartialContent(views.html.component.master.userUploadOrUpdateTraderKYC(masterTraderKYCs.Service.getAllDocuments(id)))
            } else {
              Unauthorized(views.html.index(failures = Seq(constants.Response.UNVERIFIED_ORGANIZATION)))
            }
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def userUploadOrUpdateTraderKYCView(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.userUploadOrUpdateTraderKYC(masterTraderKYCs.Service.getAllDocuments(masterTraders.Service.getID(loginState.username))))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userUploadTraderKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFileForm(utilities.String.getJsRouteFunction(routes.javascript.SetACLController.userUploadTraderKYC), utilities.String.getJsRouteFunction(routes.javascript.SetACLController.userStoreTraderKYC), documentType))
  }

  def userUploadTraderKYC(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.index(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getTraderKycFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def userStoreTraderKYC(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.storeFile[master.TraderKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getTraderKycFilePath(documentType),
          document = master.TraderKYC(id = masterTraders.Service.getID(loginState.username), documentType = documentType, fileName = name, file = None, zoneStatus = None, organizationStatus = None),
          masterCreate = masterTraderKYCs.Service.create
        )
        PartialContent(views.html.component.master.userUploadOrUpdateTraderKYC(masterTraderKYCs.Service.getAllDocuments(masterTraders.Service.getID(loginState.username))))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userUpdateTraderKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFileForm(utilities.String.getJsRouteFunction(routes.javascript.SetACLController.userUploadTraderKYC), utilities.String.getJsRouteFunction(routes.javascript.SetACLController.userUpdateTraderKYC), documentType))
  }

  def userUpdateTraderKYC(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val id = masterTraders.Service.getID(loginState.username)
        fileResourceManager.updateFile[master.TraderKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getTraderKycFilePath(documentType),
          oldDocumentFileName = masterTraderKYCs.Service.getFileName(id = id, documentType = documentType),
          document = master.TraderKYC(id = id, documentType = documentType, fileName = name, file = None, zoneStatus = None, organizationStatus = None),
          updateOldDocument = masterTraderKYCs.Service.updateOldDocument
        )
        PartialContent(views.html.component.master.userUploadOrUpdateTraderKYC(masterTraderKYCs.Service.getAllDocuments(masterTraders.Service.getID(loginState.username))))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userAccessedTraderKYCFile(documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getOrganizationKycFilePath(documentType), fileName = masterTraderKYCs.Service.getFileName(id = masterTraders.Service.getID(loginState.username), documentType = documentType)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def reviewTraderCompletionForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val trader = masterTraders.Service.getByAccountID(loginState.username)
        Ok(views.html.component.master.reviewTraderCompletion(views.companion.master.TraderCompletion.form, trader = trader, organization = masterOrganizations.Service.get(trader.organizationID), zone = masterZones.Service.get(trader.zoneID), traderKYCs = masterTraderKYCs.Service.getAllDocuments(trader.id)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def reviewTraderCompletion(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.TraderCompletion.form.bindFromRequest().fold(
        formWithErrors => {
          try {
            val trader = masterTraders.Service.getByAccountID(loginState.username)
            BadRequest(views.html.component.master.reviewTraderCompletion(formWithErrors, trader = trader, organization = masterOrganizations.Service.get(trader.organizationID), zone = masterZones.Service.get(trader.zoneID), traderKYCs = masterTraderKYCs.Service.getAllDocuments(trader.id)))
          } catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        reviewTraderCompletionData => {
          try {
            val id = masterTraders.Service.getID(loginState.username)
            if (reviewTraderCompletionData.completion && masterTraderKYCs.Service.checkAllKYCFileTypesExists(id)) {
              masterTraders.Service.markTraderFormCompleted(id)
              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.TRADER_ADDED_FOR_VERIFICATION)))
            } else {
              val trader = masterTraders.Service.getByAccountID(loginState.username)
              BadRequest(views.html.component.master.reviewTraderCompletion(views.companion.master.TraderCompletion.form, trader = trader, organization = masterOrganizations.Service.get(trader.organizationID), zone = masterZones.Service.get(trader.zoneID), traderKYCs = masterTraderKYCs.Service.getAllDocuments(trader.id)))
            }
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def zoneVerifyTraderForm(accountID: String, organizationID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.zoneVerifyTrader(views.companion.master.VerifyTrader.form.fill(views.companion.master.VerifyTrader.Data(accountID = accountID, organizationID = organizationID, gas = constants.FormField.GAS.minimumValue, password = ""))))
  }

  def zoneVerifyTrader: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyTrader.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.zoneVerifyTrader(formWithErrors))
        },
        verifyTraderData => {
          try {
            if (masterOrganizations.Service.getVerificationStatusWithTry(verifyTraderData.organizationID) && masterTraderKYCs.Service.checkAllKYCFilesVerified(masterTraders.Service.getID(verifyTraderData.accountID))) {
              val zoneID = masterOrganizations.Service.getZoneID(verifyTraderData.organizationID)
              val aclAddress = masterAccounts.Service.getAddress(verifyTraderData.accountID)
              val acl = blockchain.ACL(issueAsset = verifyTraderData.issueAsset, issueFiat = verifyTraderData.issueFiat, sendAsset = verifyTraderData.sendAsset, sendFiat = verifyTraderData.sendFiat, redeemAsset = verifyTraderData.redeemAsset, redeemFiat = verifyTraderData.redeemFiat, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder, changeBuyerBid = verifyTraderData.changeBuyerBid, changeSellerBid = verifyTraderData.changeSellerBid, confirmBuyerBid = verifyTraderData.confirmBuyerBid, confirmSellerBid = verifyTraderData.changeSellerBid, negotiation = verifyTraderData.negotiation, releaseAsset = verifyTraderData.releaseAsset)
              blockchainAclHashes.Service.create(acl)
              transaction.process[blockchainTransaction.SetACL, transactionsSetACL.Request](
                entity = blockchainTransaction.SetACL(from = loginState.address, aclAddress = aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, aclHash = util.hashing.MurmurHash3.stringHash(acl.toString).toString, gas = verifyTraderData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionSetACLs.Service.create,
                request = transactionsSetACL.Request(transactionsSetACL.BaseReq(from = loginState.address, gas = verifyTraderData.gas.toString), password = verifyTraderData.password, aclAddress = aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, issueAsset = verifyTraderData.issueAsset.toString, issueFiat = verifyTraderData.issueFiat.toString, sendAsset = verifyTraderData.sendAsset.toString, sendFiat = verifyTraderData.sendFiat.toString, redeemAsset = verifyTraderData.redeemAsset.toString, redeemFiat = verifyTraderData.redeemFiat.toString, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder.toString, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder.toString, changeBuyerBid = verifyTraderData.changeBuyerBid.toString, changeSellerBid = verifyTraderData.changeSellerBid.toString, confirmBuyerBid = verifyTraderData.confirmBuyerBid.toString, confirmSellerBid = verifyTraderData.confirmSellerBid.toString, negotiation = verifyTraderData.negotiation.toString, releaseAsset = verifyTraderData.releaseAsset.toString, mode = transactionMode),
                action = transactionsSetACL.Service.post,
                onSuccess = blockchainTransactionSetACLs.Utility.onSuccess,
                onFailure = blockchainTransactionSetACLs.Utility.onFailure,
                updateTransactionHash = blockchainTransactionSetACLs.Service.updateTransactionHash
              )
              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ACL_SET)))
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

  def zoneViewKycDocuments(traderID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.zoneViewVerificationTraderKycDouments(masterTraderKYCs.Service.getAllDocuments(traderID)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneVerifyKycDocument(traderID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        masterTraderKYCs.Service.zoneVerify(id = traderID, documentType = documentType)
        utilitiesNotification.send(accountID = masterTraders.Service.getAccountId(traderID), notification = constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
        withUsernameToken.Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def zoneRejectKycDocument(traderID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        masterTraderKYCs.Service.zoneReject(id = traderID, documentType = documentType)
        utilitiesNotification.send(accountID = masterTraders.Service.getAccountId(traderID), notification = constants.Notification.FAILURE,  Messages(constants.Response.DOCUMENT_REJECTED.message))
        withUsernameToken.Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }


  def zoneRejectVerifyTraderRequestForm(traderID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.zoneRejectVerifyTraderRequest(views.companion.master.RejectVerifyTraderRequest.form, traderID))
  }

  def zoneRejectVerifyTraderRequest: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RejectVerifyTraderRequest.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.zoneRejectVerifyTraderRequest(formWithErrors, formWithErrors.data(constants.Form.TRADER_ID)))
        },
        rejectVerifyTraderRequestData => {
          try {
            masterTraders.Service.rejectTrader(rejectVerifyTraderRequestData.traderID)
            masterTraderKYCs.Service.zoneRejectAll(masterZones.Service.getAccountId(rejectVerifyTraderRequestData.traderID))
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.VERIFY_TRADER_REQUEST_REJECTED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def zoneViewPendingVerifyTraderRequests: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.zoneViewPendingVerifyTraderRequests(masterTraders.Service.getVerifyTraderRequestsForZone(masterZones.Service.getZoneId(loginState.username))))
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationVerifyTraderForm(accountID: String, organizationID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.organizationVerifyTrader(views.companion.master.VerifyTrader.form.fill(views.companion.master.VerifyTrader.Data(accountID = accountID, organizationID = organizationID, gas = constants.FormField.GAS.minimumValue, password = ""))))
  }

  def organizationVerifyTrader: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyTrader.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.organizationVerifyTrader(formWithErrors))
        },
        verifyTraderData => {
          try {
            if (masterTraderKYCs.Service.checkAllKYCFilesVerified(masterTraders.Service.getID(verifyTraderData.accountID))) {
              val zoneID = masterOrganizations.Service.getZoneID(verifyTraderData.organizationID)
              val aclAddress = masterAccounts.Service.getAddress(verifyTraderData.accountID)
              val acl = blockchain.ACL(issueAsset = verifyTraderData.issueAsset, issueFiat = verifyTraderData.issueFiat, sendAsset = verifyTraderData.sendAsset, sendFiat = verifyTraderData.sendFiat, redeemAsset = verifyTraderData.redeemAsset, redeemFiat = verifyTraderData.redeemFiat, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder, changeBuyerBid = verifyTraderData.changeBuyerBid, changeSellerBid = verifyTraderData.changeSellerBid, confirmBuyerBid = verifyTraderData.confirmBuyerBid, confirmSellerBid = verifyTraderData.changeSellerBid, negotiation = verifyTraderData.negotiation, releaseAsset = verifyTraderData.releaseAsset)
              blockchainAclHashes.Service.create(acl)
              transaction.process[blockchainTransaction.SetACL, transactionsSetACL.Request](
                entity = blockchainTransaction.SetACL(from = loginState.address, aclAddress = aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, aclHash = util.hashing.MurmurHash3.stringHash(acl.toString).toString, gas = verifyTraderData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionSetACLs.Service.create,
                request = transactionsSetACL.Request(transactionsSetACL.BaseReq(from = loginState.address, gas = verifyTraderData.gas.toString), password = verifyTraderData.password, aclAddress = aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, issueAsset = verifyTraderData.issueAsset.toString, issueFiat = verifyTraderData.issueFiat.toString, sendAsset = verifyTraderData.sendAsset.toString, sendFiat = verifyTraderData.sendFiat.toString, redeemAsset = verifyTraderData.redeemAsset.toString, redeemFiat = verifyTraderData.redeemFiat.toString, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder.toString, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder.toString, changeBuyerBid = verifyTraderData.changeBuyerBid.toString, changeSellerBid = verifyTraderData.changeSellerBid.toString, confirmBuyerBid = verifyTraderData.confirmBuyerBid.toString, confirmSellerBid = verifyTraderData.confirmSellerBid.toString, negotiation = verifyTraderData.negotiation.toString, releaseAsset = verifyTraderData.releaseAsset.toString, mode = transactionMode),
                action = transactionsSetACL.Service.post,
                onSuccess = blockchainTransactionSetACLs.Utility.onSuccess,
                onFailure = blockchainTransactionSetACLs.Utility.onFailure,
                updateTransactionHash = blockchainTransactionSetACLs.Service.updateTransactionHash
              )
              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ACL_SET)))
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

  def organizationModifyTraderForm(accountID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.organizationModifyTrader(views.companion.master.ModifyTrader.form.fill(views.companion.master.ModifyTrader.Data(accountID = accountID, gas = constants.FormField.GAS.minimumValue, password = ""))))
  }

  def organizationModifyTrader: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ModifyTrader.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.organizationModifyTrader(formWithErrors))
        },
        verifyTraderData => {
          try {
            if (masterTraderKYCs.Service.checkAllKYCFilesVerified(masterTraders.Service.getID(verifyTraderData.accountID))) {
              val zoneID = masterOrganizations.Service.getZoneIDByAccountID(loginState.username)
              val organizationID = masterOrganizations.Service.getID(loginState.username)
              val aclAddress = masterAccounts.Service.getAddress(verifyTraderData.accountID)
              val acl = blockchain.ACL(issueAsset = verifyTraderData.issueAsset, issueFiat = verifyTraderData.issueFiat, sendAsset = verifyTraderData.sendAsset, sendFiat = verifyTraderData.sendFiat, redeemAsset = verifyTraderData.redeemAsset, redeemFiat = verifyTraderData.redeemFiat, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder, changeBuyerBid = verifyTraderData.changeBuyerBid, changeSellerBid = verifyTraderData.changeSellerBid, confirmBuyerBid = verifyTraderData.confirmBuyerBid, confirmSellerBid = verifyTraderData.changeSellerBid, negotiation = verifyTraderData.negotiation, releaseAsset = verifyTraderData.releaseAsset)
              blockchainAclHashes.Service.create(acl)
              transaction.process[blockchainTransaction.SetACL, transactionsSetACL.Request](
                entity = blockchainTransaction.SetACL(from = loginState.address, aclAddress = aclAddress, organizationID = organizationID, zoneID = zoneID, aclHash = util.hashing.MurmurHash3.stringHash(acl.toString).toString, gas = verifyTraderData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionSetACLs.Service.create,
                request = transactionsSetACL.Request(transactionsSetACL.BaseReq(from = loginState.address, gas = verifyTraderData.gas.toString), password = verifyTraderData.password, aclAddress = aclAddress, organizationID = organizationID, zoneID = zoneID, issueAsset = verifyTraderData.issueAsset.toString, issueFiat = verifyTraderData.issueFiat.toString, sendAsset = verifyTraderData.sendAsset.toString, sendFiat = verifyTraderData.sendFiat.toString, redeemAsset = verifyTraderData.redeemAsset.toString, redeemFiat = verifyTraderData.redeemFiat.toString, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder.toString, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder.toString, changeBuyerBid = verifyTraderData.changeBuyerBid.toString, changeSellerBid = verifyTraderData.changeSellerBid.toString, confirmBuyerBid = verifyTraderData.confirmBuyerBid.toString, confirmSellerBid = verifyTraderData.confirmSellerBid.toString, negotiation = verifyTraderData.negotiation.toString, releaseAsset = verifyTraderData.releaseAsset.toString, mode = transactionMode),
                action = transactionsSetACL.Service.post,
                onSuccess = blockchainTransactionSetACLs.Utility.onSuccess,
                onFailure = blockchainTransactionSetACLs.Utility.onFailure,
                updateTransactionHash = blockchainTransactionSetACLs.Service.updateTransactionHash
              )
              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ACL_SET)))
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

  def organizationViewKycDocuments(traderID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.organizationViewVerificationTraderKycDouments(masterTraderKYCs.Service.getAllDocuments(traderID)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationVerifyKycDocument(traderID: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        masterTraderKYCs.Service.organizationVerify(id = traderID, documentType = documentType)
        utilitiesNotification.send(accountID = masterTraders.Service.getAccountId(traderID), notification = constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
        withUsernameToken.Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def organizationRejectKycDocument(traderID: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        masterTraderKYCs.Service.organizationReject(id = traderID, documentType = documentType)
        utilitiesNotification.send(accountID = masterTraders.Service.getAccountId(traderID), notification = constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
        withUsernameToken.Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def organizationRejectVerifyTraderRequestForm(traderID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.organizationRejectVerifyTraderRequest(views.companion.master.RejectVerifyTraderRequest.form, traderID))
  }

  def organizationRejectVerifyTraderRequest: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RejectVerifyTraderRequest.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.organizationRejectVerifyTraderRequest(formWithErrors, formWithErrors.data(constants.Form.TRADER_ID)))
        },
        rejectVerifyTraderRequestData => {
          try {
            masterTraders.Service.rejectTrader(rejectVerifyTraderRequestData.traderID)
            masterTraderKYCs.Service.organizationRejectAll(masterOrganizations.Service.getAccountId(rejectVerifyTraderRequestData.traderID))
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.VERIFY_TRADER_REQUEST_REJECTED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def organizationViewPendingVerifyTraderRequests: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.organizationViewPendingVerifyTraderRequests(masterTraders.Service.getVerifyTraderRequestsForOrganization(masterOrganizations.Service.getByAccountID(loginState.username).id)))
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def uploadTraderKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFileForm(utilities.String.getJsRouteFunction(routes.javascript.SetACLController.uploadTraderKyc), utilities.String.getJsRouteFunction(routes.javascript.SetACLController.storeTraderKyc), documentType))
  }

  def uploadTraderKyc(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.index(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getTraderKycFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeTraderKyc(name: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        fileResourceManager.storeFile[master.TraderKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getTraderKycFilePath(documentType),
          document = master.TraderKYC(id = masterTraders.Service.getID(loginState.username), documentType = documentType, fileName = name, file = None, zoneStatus = None, organizationStatus = None),
          masterCreate = masterTraderKYCs.Service.create
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateTraderKycForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFileForm(utilities.String.getJsRouteFunction(routes.javascript.SetACLController.uploadTraderKyc), utilities.String.getJsRouteFunction(routes.javascript.SetACLController.updateTraderKyc), documentType))
  }

  def updateTraderKyc(name: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val id = masterTraders.Service.getID(loginState.username)
        fileResourceManager.updateFile[master.TraderKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getTraderKycFilePath(documentType),
          oldDocumentFileName = masterTraderKYCs.Service.getFileName(id = id, documentType = documentType),
          document = master.TraderKYC(id = id, documentType = documentType, fileName = name, file = None, zoneStatus = None, organizationStatus = None),
          updateOldDocument = masterTraderKYCs.Service.updateOldDocument
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def viewTradersInOrganization: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.viewTradersInOrganizationForOrganization(masterTraders.Service.getVerifiedTradersForOrganization(masterOrganizations.Service.getByAccountID(loginState.username).id)))
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def viewTradersInOrganizationForZone(organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        if (masterOrganizations.Service.getZoneID(organizationID) == masterZones.Service.getZoneId(loginState.username)) {
          withUsernameToken.Ok(views.html.component.master.viewTradersInOrganization(masterTraders.Service.getVerifiedTradersForOrganization(organizationID)))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def viewTradersInOrganizationForGenesis(organizationID: String): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.viewTradersInOrganization(masterTraders.Service.getVerifiedTradersForOrganization(organizationID)))
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def blockchainSetACLForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.setACL(views.companion.blockchain.SetACL.form))
  }

  def blockchainSetACL: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.SetACL.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.setACL(formWithErrors))
      },
      setACLData => {
        try {
          transactionsSetACL.Service.post(transactionsSetACL.Request(transactionsSetACL.BaseReq(from = setACLData.from, gas = setACLData.gas.toString), password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAsset = setACLData.redeemAsset.toString, redeemFiat = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAsset = setACLData.releaseAsset.toString, mode = transactionMode))
          Ok(views.html.index(successes = Seq(constants.Response.ACL_SET)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}