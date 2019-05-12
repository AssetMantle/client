package controllers

import controllers.actions.WithZoneLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext
import scala.util.Random

@Singleton
class SetACLController @Inject()(messagesControllerComponents: MessagesControllerComponents, blockchainFeedbacks: blockchain.Feedbacks, blockchainAccounts: blockchain.Accounts, masterZones: master.Zones, masterOrganizations: master.Organizations, withZoneLoginAction: WithZoneLoginAction, masterAccounts: master.Accounts, transactionsSetACL: transactions.SetACL, blockchainAclAccounts: blockchain.ACLAccounts, blockchainTransactionSetACLs: blockchainTransaction.SetACLs, blockchainAclHashes: blockchain.ACLHashes)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def setACLForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.setACL(views.companion.master.SetACL.form))
  }

  def setACL: Action[AnyContent] = withZoneLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.SetACL.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.setACL(formWithErrors))
        },
        setACLData => {
          try {
            if (masterOrganizations.Service.getStatus(setACLData.organizationID) == Option(true)) {
              val acl = blockchain.ACL(issueAssets = setACLData.issueAsset, issueFiats = setACLData.issueFiat, sendAssets = setACLData.sendAsset, sendFiats = setACLData.sendFiat, redeemAssets = setACLData.redeemAsset, redeemFiats = setACLData.redeemFiat, sellerExecuteOrder = setACLData.sellerExecuteOrder, buyerExecuteOrder = setACLData.buyerExecuteOrder, changeBuyerBid = setACLData.changeBuyerBid, changeSellerBid = setACLData.changeSellerBid, confirmBuyerBid = setACLData.confirmBuyerBid, confirmSellerBid = setACLData.changeSellerBid, negotiation = setACLData.negotiation, releaseAssets = setACLData.releaseAsset)
              blockchainAclHashes.Service.addACLHash(acl)
              val zoneID = masterZones.Service.getZoneId(username)
              if (kafkaEnabled) {
                val response = transactionsSetACL.Service.kafkaPost(transactionsSetACL.Request(from = username, password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAsset = setACLData.redeemAsset.toString, redeemFiat = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAsset = setACLData.releaseAsset.toString))
                blockchainAclAccounts.Service.addACLAccount(username, setACLData.aclAddress, zoneID, setACLData.organizationID, acl)
                blockchainTransactionSetACLs.Service.addSetACLKafka(from = username, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = zoneID, acl = acl, null, null, ticketID = response.ticketID, null)
                blockchainFeedbacks.Service.addFeedback(setACLData.aclAddress, 0, false)
                Ok(views.html.index(success = response.ticketID))
              } else {
                val response = transactionsSetACL.Service.post(transactionsSetACL.Request(from = username, password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAsset = setACLData.redeemAsset.toString, redeemFiat = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAsset = setACLData.releaseAsset.toString))
                val fromAddress = masterAccounts.Service.getAddress(username)
                blockchainAclAccounts.Service.addOrUpdateACLAccount(username, setACLData.aclAddress, zoneID, setACLData.organizationID, acl)
                blockchainTransactionSetACLs.Service.addSetACL(from = username, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = zoneID, acl = acl, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
                masterAccounts.Service.updateUserTypeOnAddress(setACLData.aclAddress, constants.User.TRADER)
                blockchainAccounts.Service.updateSequence(fromAddress, blockchainAccounts.Service.getSequence(fromAddress) + 1)
                Ok(views.html.index(success = response.TxHash))
              }
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
          val acl = blockchain.ACL(issueAssets = setACLData.issueAsset, issueFiats = setACLData.issueFiat, sendAssets = setACLData.sendAsset, sendFiats = setACLData.sendFiat, redeemAssets = setACLData.redeemAsset, redeemFiats = setACLData.redeemFiat, sellerExecuteOrder = setACLData.sellerExecuteOrder, buyerExecuteOrder = setACLData.buyerExecuteOrder, changeBuyerBid = setACLData.changeBuyerBid, changeSellerBid = setACLData.changeSellerBid, confirmBuyerBid = setACLData.confirmBuyerBid, confirmSellerBid = setACLData.changeSellerBid, negotiation = setACLData.negotiation, releaseAssets = setACLData.releaseAsset)
          blockchainAclHashes.Service.addACLHash(acl)
          if (kafkaEnabled) {
            val response = transactionsSetACL.Service.kafkaPost(transactionsSetACL.Request(from = setACLData.from, password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAsset = setACLData.redeemAsset.toString, redeemFiat = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAsset = setACLData.releaseAsset.toString))
            blockchainAclAccounts.Service.addACLAccount(setACLData.from, setACLData.aclAddress, setACLData.zoneID, setACLData.organizationID, acl)
            blockchainTransactionSetACLs.Service.addSetACLKafka(from = setACLData.from, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, acl = acl, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsSetACL.Service.post(transactionsSetACL.Request(from = setACLData.from, password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAsset = setACLData.redeemAsset.toString, redeemFiat = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAsset = setACLData.releaseAsset.toString))
            blockchainAclAccounts.Service.addOrUpdateACLAccount(setACLData.from, setACLData.aclAddress, setACLData.zoneID, setACLData.organizationID, acl)
            blockchainTransactionSetACLs.Service.addSetACL(from = setACLData.from, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, acl = acl, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
            Ok(views.html.index(success = response.TxHash))
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