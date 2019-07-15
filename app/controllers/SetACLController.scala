package controllers

import controllers.actions.{WithOrganizationLoginAction, WithUserLoginAction, WithZoneLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import utilities.PushNotification

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class SetACLController @Inject()(messagesControllerComponents: MessagesControllerComponents, blockchainAccounts: blockchain.Accounts, masterZones: master.Zones, masterOrganizations: master.Organizations, masterTraders: master.Traders, masterTraderKYCs: master.TraderKYCs,withZoneLoginAction: WithZoneLoginAction, withOrganizationLoginAction: WithOrganizationLoginAction, withUserLoginAction: WithUserLoginAction, masterAccounts: master.Accounts, transactionsSetACL: transactions.SetACL, blockchainAclAccounts: blockchain.ACLAccounts, blockchainTransactionSetACLs: blockchainTransaction.SetACLs, blockchainAclHashes: blockchain.ACLHashes, pushNotification: PushNotification)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def addTraderForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.addTrader(views.companion.master.AddTrader.form))
  }

  def addTrader: Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddTrader.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.addTrader(formWithErrors))
        },
        addTraderData => {

          try {
            if (masterOrganizations.Service.getStatus(addTraderData.organizationID) == Option(true)) {
              masterTraders.Service.create(zoneID = addTraderData.zoneID, organizationID = addTraderData.organizationID, accountID = loginState.username, name = addTraderData.name)
              Ok(views.html.index(successes = Seq(constants.Response.TRADER_ADDED)))
            } else {
              Ok(views.html.index(failures = Seq(constants.Response.UNVERIFIED_ORGANIZATION)))
            }
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def zoneVerifyTraderForm(accountID: String, organizationID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.zoneVerifyTrader(views.companion.master.VerifyTrader.form, masterAccounts.Service.getAddress(accountID), organizationID))
  }

  def zoneVerifyTrader: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyTrader.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.zoneVerifyTrader(formWithErrors, formWithErrors.data(constants.Form.ACL_ADDRESS), formWithErrors.data(constants.Form.ORGANIZATION_ID)))
        },
        verifyTraderData => {

          try {
            if (masterOrganizations.Service.getStatus(verifyTraderData.organizationID) == Option(true)) {
              val zoneID = masterZones.Service.getZoneId(loginState.username)
              val ticketID = if (kafkaEnabled) transactionsSetACL.Service.kafkaPost(transactionsSetACL.Request(from = loginState.username, password = verifyTraderData.password, aclAddress = verifyTraderData.aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, issueAsset = verifyTraderData.issueAsset.toString, issueFiat = verifyTraderData.issueFiat.toString, sendAsset = verifyTraderData.sendAsset.toString, sendFiat = verifyTraderData.sendFiat.toString, redeemAsset = verifyTraderData.redeemAsset.toString, redeemFiat = verifyTraderData.redeemFiat.toString, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder.toString, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder.toString, changeBuyerBid = verifyTraderData.changeBuyerBid.toString, changeSellerBid = verifyTraderData.changeSellerBid.toString, confirmBuyerBid = verifyTraderData.confirmBuyerBid.toString, confirmSellerBid = verifyTraderData.confirmSellerBid.toString, negotiation = verifyTraderData.negotiation.toString, releaseAsset = verifyTraderData.releaseAsset.toString)).ticketID else Random.nextString(32)
              val acl = blockchain.ACL(issueAsset = verifyTraderData.issueAsset, issueFiat = verifyTraderData.issueFiat, sendAsset = verifyTraderData.sendAsset, sendFiat = verifyTraderData.sendFiat, redeemAsset = verifyTraderData.redeemAsset, redeemFiat = verifyTraderData.redeemFiat, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder, changeBuyerBid = verifyTraderData.changeBuyerBid, changeSellerBid = verifyTraderData.changeSellerBid, confirmBuyerBid = verifyTraderData.confirmBuyerBid, confirmSellerBid = verifyTraderData.changeSellerBid, negotiation = verifyTraderData.negotiation, releaseAsset = verifyTraderData.releaseAsset)
              blockchainAclHashes.Service.create(acl)
              blockchainTransactionSetACLs.Service.create(from = loginState.username, aclAddress = verifyTraderData.aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, acl = acl, null, txHash = null, ticketID = ticketID, null)
              if (!kafkaEnabled) {
                Future {
                  try {
                    blockchainTransactionSetACLs.Utility.onSuccess(ticketID, transactionsSetACL.Service.post(transactionsSetACL.Request(from = loginState.username, password = verifyTraderData.password, aclAddress = verifyTraderData.aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, issueAsset = verifyTraderData.issueAsset.toString, issueFiat = verifyTraderData.issueFiat.toString, sendAsset = verifyTraderData.sendAsset.toString, sendFiat = verifyTraderData.sendFiat.toString, redeemAsset = verifyTraderData.redeemAsset.toString, redeemFiat = verifyTraderData.redeemFiat.toString, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder.toString, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder.toString, changeBuyerBid = verifyTraderData.changeBuyerBid.toString, changeSellerBid = verifyTraderData.changeSellerBid.toString, confirmBuyerBid = verifyTraderData.confirmBuyerBid.toString, confirmSellerBid = verifyTraderData.confirmSellerBid.toString, negotiation = verifyTraderData.negotiation.toString, releaseAsset = verifyTraderData.releaseAsset.toString)))
                  } catch {
                    case baseException: BaseException => logger.error(baseException.failure.message, baseException)
                    case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
                      blockchainTransactionSetACLs.Utility.onFailure(ticketID, blockChainException.failure.message)
                  }
                }
              }
              Ok(views.html.index(successes = Seq(constants.Response.ACL_SET)))
            } else {
              Ok(views.html.index(failures = Seq(constants.Response.UNVERIFIED_ORGANIZATION)))
            }
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
          }
        }
      )
  }

  def zoneViewKycDocuments(accountID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.zoneViewVerificationTraderKycDouments(masterTraderKYCs.Service.getAllDocuments(accountID)))
      } catch {
        case baseException: BaseException => BadRequest(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneVerifyKycDocument(accountID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        masterTraderKYCs.Service.zoneVerify(id = accountID, documentType = documentType)
        pushNotification.sendNotification(accountID, constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
        Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => BadRequest(Messages(baseException.failure.message))
      }
  }

  def zoneRejectKycDocument(accountID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        masterTraderKYCs.Service.zoneReject(id = accountID, documentType = documentType)
        pushNotification.sendNotification(accountID, constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
        Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => BadRequest(Messages(baseException.failure.message))
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
            masterTraders.Service.updateStatus(rejectVerifyTraderRequestData.traderID, status = false)
            masterTraderKYCs.Service.zoneRejectAll(masterZones.Service.getAccountId(rejectVerifyTraderRequestData.traderID))
            Ok(views.html.index(successes = Seq(constants.Response.VERIFY_TRADER_REQUEST_REJECTED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def zoneViewPendingVerifyTraderRequests: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.zoneViewPendingVerifyTraderRequests(masterTraders.Service.getVerifyTraderRequestsForZone(masterZones.Service.getZoneId(loginState.username))))
      }
      catch {
        case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationVerifyTraderForm(accountID:String ,organizationID:String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.organizationVerifyTrader(views.companion.master.VerifyTrader.form, masterAccounts.Service.getAddress(accountID), organizationID))
  }

  def organizationVerifyTrader: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyTrader.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.organizationVerifyTrader(formWithErrors, formWithErrors.data(constants.Form.ACL_ADDRESS), formWithErrors.data(constants.Form.ORGANIZATION_ID)))
        },
        verifyTraderData => {

          try {
            if (masterOrganizations.Service.getStatus(verifyTraderData.organizationID) == Option(true)) {
              val zoneID = masterOrganizations.Service.get(verifyTraderData.organizationID).zoneID
              val ticketID = if (kafkaEnabled) transactionsSetACL.Service.kafkaPost(transactionsSetACL.Request(from = loginState.username, password = verifyTraderData.password, aclAddress = verifyTraderData.aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, issueAsset = verifyTraderData.issueAsset.toString, issueFiat = verifyTraderData.issueFiat.toString, sendAsset = verifyTraderData.sendAsset.toString, sendFiat = verifyTraderData.sendFiat.toString, redeemAsset = verifyTraderData.redeemAsset.toString, redeemFiat = verifyTraderData.redeemFiat.toString, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder.toString, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder.toString, changeBuyerBid = verifyTraderData.changeBuyerBid.toString, changeSellerBid = verifyTraderData.changeSellerBid.toString, confirmBuyerBid = verifyTraderData.confirmBuyerBid.toString, confirmSellerBid = verifyTraderData.confirmSellerBid.toString, negotiation = verifyTraderData.negotiation.toString, releaseAsset = verifyTraderData.releaseAsset.toString)).ticketID else Random.nextString(32)
              val acl = blockchain.ACL(issueAsset = verifyTraderData.issueAsset, issueFiat = verifyTraderData.issueFiat, sendAsset = verifyTraderData.sendAsset, sendFiat = verifyTraderData.sendFiat, redeemAsset = verifyTraderData.redeemAsset, redeemFiat = verifyTraderData.redeemFiat, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder, changeBuyerBid = verifyTraderData.changeBuyerBid, changeSellerBid = verifyTraderData.changeSellerBid, confirmBuyerBid = verifyTraderData.confirmBuyerBid, confirmSellerBid = verifyTraderData.changeSellerBid, negotiation = verifyTraderData.negotiation, releaseAsset = verifyTraderData.releaseAsset)
              blockchainAclHashes.Service.create(acl)
              blockchainTransactionSetACLs.Service.create(from = loginState.username, aclAddress = verifyTraderData.aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, acl = acl, null, txHash = null, ticketID = ticketID, null)
              if (!kafkaEnabled) {
                Future {
                  try {
                    blockchainTransactionSetACLs.Utility.onSuccess(ticketID, transactionsSetACL.Service.post(transactionsSetACL.Request(from = loginState.username, password = verifyTraderData.password, aclAddress = verifyTraderData.aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, issueAsset = verifyTraderData.issueAsset.toString, issueFiat = verifyTraderData.issueFiat.toString, sendAsset = verifyTraderData.sendAsset.toString, sendFiat = verifyTraderData.sendFiat.toString, redeemAsset = verifyTraderData.redeemAsset.toString, redeemFiat = verifyTraderData.redeemFiat.toString, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder.toString, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder.toString, changeBuyerBid = verifyTraderData.changeBuyerBid.toString, changeSellerBid = verifyTraderData.changeSellerBid.toString, confirmBuyerBid = verifyTraderData.confirmBuyerBid.toString, confirmSellerBid = verifyTraderData.confirmSellerBid.toString, negotiation = verifyTraderData.negotiation.toString, releaseAsset = verifyTraderData.releaseAsset.toString)))
                  } catch {
                    case baseException: BaseException => logger.error(baseException.failure.message, baseException)
                    case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
                      blockchainTransactionSetACLs.Utility.onFailure(ticketID, blockChainException.failure.message)
                  }
                }
              }
              Ok(views.html.index(successes = Seq(constants.Response.ACL_SET)))
            } else {
              Ok(views.html.index(failures = Seq(constants.Response.UNVERIFIED_ORGANIZATION)))
            }
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
          }
        }
      )
  }

  def organizationViewKycDocuments(accountID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.organizationViewVerificationTraderKycDouments(masterTraderKYCs.Service.getAllDocuments(accountID)))
      } catch {
        case baseException: BaseException => BadRequest(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationVerifyKycDocument(accountID: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        masterTraderKYCs.Service.organizationVerify(id = accountID, documentType = documentType)
        pushNotification.sendNotification(accountID, constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
        Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => BadRequest(Messages(baseException.failure.message))
      }
  }

  def organizationRejectKycDocument(accountID: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        masterTraderKYCs.Service.organizationReject(id = accountID, documentType = documentType)
        pushNotification.sendNotification(accountID, constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
        Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => BadRequest(Messages(baseException.failure.message))
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
            masterTraders.Service.updateStatus(rejectVerifyTraderRequestData.traderID, false)
            masterTraderKYCs.Service.organizationRejectAll(masterOrganizations.Service.getAccountId(rejectVerifyTraderRequestData.traderID))
            Ok(views.html.index(successes = Seq(constants.Response.VERIFY_TRADER_REQUEST_REJECTED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def organizationViewPendingVerifyTraderRequests: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.organizationViewPendingVerifyTraderRequests(masterTraders.Service.getVerifyTraderRequestsForOrganization(masterOrganizations.Service.getByAccountID(loginState.username).id)))
      }
      catch {
        case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
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
          if (kafkaEnabled) {
            transactionsSetACL.Service.kafkaPost(transactionsSetACL.Request(from = setACLData.from, password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAsset = setACLData.redeemAsset.toString, redeemFiat = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAsset = setACLData.releaseAsset.toString))

          } else {
            transactionsSetACL.Service.post(transactionsSetACL.Request(from = setACLData.from, password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAsset = setACLData.redeemAsset.toString, redeemFiat = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAsset = setACLData.releaseAsset.toString))
          }
          Ok(views.html.index(successes = Seq(constants.Response.ACL_SET)))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }
}