package controllers

import controllers.actions.WithZoneLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class SetACLController @Inject()(messagesControllerComponents: MessagesControllerComponents, blockchainAccounts: blockchain.Accounts, masterZones: master.Zones, masterOrganizations: master.Organizations, withZoneLoginAction: WithZoneLoginAction, masterAccounts: master.Accounts, transactionsSetACL: transactions.SetACL, blockchainAclAccounts: blockchain.ACLAccounts, blockchainTransactionSetACLs: blockchainTransaction.SetACLs, blockchainAclHashes: blockchain.ACLHashes)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def setACLForm(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.setACL(views.companion.master.SetACL.form))
  }

  def setACL(): Action[AnyContent] = withZoneLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.SetACL.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.setACL(formWithErrors))
        },
        setACLData => {
          try {
            if (masterOrganizations.Service.getStatus(setACLData.organizationID) == Option(true)) {
              val zoneID = masterZones.Service.getZoneId(username)
              val ticketID = if (kafkaEnabled) transactionsSetACL.Service.kafkaPost(transactionsSetACL.Request(from = username, password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAsset = setACLData.redeemAsset.toString, redeemFiat = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAsset = setACLData.releaseAsset.toString)).ticketID else Random.nextString(32)
              val acl = blockchain.ACL(issueAsset = setACLData.issueAsset, issueFiat = setACLData.issueFiat, sendAsset = setACLData.sendAsset, sendFiat = setACLData.sendFiat, redeemAsset = setACLData.redeemAsset, redeemFiat = setACLData.redeemFiat, sellerExecuteOrder = setACLData.sellerExecuteOrder, buyerExecuteOrder = setACLData.buyerExecuteOrder, changeBuyerBid = setACLData.changeBuyerBid, changeSellerBid = setACLData.changeSellerBid, confirmBuyerBid = setACLData.confirmBuyerBid, confirmSellerBid = setACLData.changeSellerBid, negotiation = setACLData.negotiation, releaseAsset = setACLData.releaseAsset)
              blockchainAclHashes.Service.addEntity(acl)
              blockchainTransactionSetACLs.Service.addTransaction(from = username, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = zoneID, acl = acl, null, txHash = null, ticketID = ticketID, null)
              if (!kafkaEnabled) {
                Future {
                  try {
                    blockchainTransactionSetACLs.Utility.onSuccess(ticketID, transactionsSetACL.Service.post(transactionsSetACL.Request(from = username, password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAsset = setACLData.redeemAsset.toString, redeemFiat = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAsset = setACLData.releaseAsset.toString)))
                  } catch {
                    case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
                    case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
                      blockchainTransactionSetACLs.Utility.onFailure(ticketID, blockChainException.message)
                  }
                }
              }
              Ok(views.html.index(success = ticketID))
            } else {
              Ok(views.html.index(failure = Messages(constants.Error.UNVERIFIED_ORGANIZATION)))
            }
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
            case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
          }
        }
      )
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
            Ok(views.html.index(success = transactionsSetACL.Service.kafkaPost(transactionsSetACL.Request(from = setACLData.from, password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAsset = setACLData.redeemAsset.toString, redeemFiat = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAsset = setACLData.releaseAsset.toString)).ticketID))
          } else {
            Ok(views.html.index(success = transactionsSetACL.Service.post(transactionsSetACL.Request(from = setACLData.from, password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAsset = setACLData.redeemAsset.toString, redeemFiat = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAsset = setACLData.releaseAsset.toString)).TxHash))
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      }
    )
  }
}