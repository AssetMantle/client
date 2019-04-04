package controllers

import controllers.actions.WithOrganizationLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.{blockchain, master}
import models.blockchainTransaction
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.SetACL

import scala.concurrent.ExecutionContext
import scala.util.Random

class SetACLController @Inject()(messagesControllerComponents: MessagesControllerComponents, withOrganizationLoginAction: WithOrganizationLoginAction, masterAccounts: master.Accounts, transactionSetACL: SetACL, blockchainAclAccounts: blockchain.ACLAccounts, blockchainTransactionSetACLs: blockchainTransaction.SetACLs, blockchainAclHashes: blockchain.ACLHashes)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def setACLForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.setACL(views.companion.master.SetACL.form))
  }

  def setACL: Action[AnyContent] = withOrganizationLoginAction { implicit request =>
    views.companion.master.SetACL.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.setACL(formWithErrors))
      },
      setACLData => {
        try {
          val acl = blockchain.ACL(issueAssets = setACLData.issueAsset, issueFiats = setACLData.issueFiat, sendAssets = setACLData.sendAsset, sendFiats = setACLData.sendFiat, redeemAssets = setACLData.redeemAsset, redeemFiats = setACLData.redeemFiat, sellerExecuteOrder = setACLData.sellerExecuteOrder, buyerExecuteOrder = setACLData.buyerExecuteOrder, changeBuyerBid = setACLData.changeBuyerBid, changeSellerBid = setACLData.changeSellerBid, confirmBuyerBid = setACLData.confirmBuyerBid, confirmSellerBid = setACLData.changeSellerBid, negotiation = setACLData.negotiation, releaseAssets = setACLData.releaseAsset)
          blockchainAclHashes.Service.addACLHash(acl)
          if (kafkaEnabled) {
            val response = transactionSetACL.Service.kafkaPost( transactionSetACL.Request(from = request.session.get(constants.Security.USERNAME).get, password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAsset = setACLData.redeemAsset.toString, redeemFiat = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAsset = setACLData.releaseAsset.toString))
            blockchainAclAccounts.Service.addACLAccount(request.session.get(constants.Security.USERNAME).get, setACLData.aclAddress, setACLData.zoneID, setACLData.organizationID, acl)
            blockchainTransactionSetACLs.Service.addSetACLKafka(from = request.session.get(constants.Security.USERNAME).get, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, acl = acl, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionSetACL.Service.post( transactionSetACL.Request(from = request.session.get(constants.Security.USERNAME).get, password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAsset = setACLData.redeemAsset.toString, redeemFiat = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAsset = setACLData.releaseAsset.toString))
            blockchainAclAccounts.Service.addOrUpdateACLAccount(request.session.get(constants.Security.USERNAME).get, setACLData.aclAddress, setACLData.zoneID, setACLData.organizationID, acl)
            blockchainTransactionSetACLs.Service.addSetACL(from = request.session.get(constants.Security.USERNAME).get, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, acl = acl, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
            masterAccounts.Service.updateUserTypeOnAddress(setACLData.aclAddress, constants.User.TRADER)
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
            val response = transactionSetACL.Service.kafkaPost( transactionSetACL.Request(from = setACLData.from, password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAsset = setACLData.redeemAsset.toString, redeemFiat = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAsset = setACLData.releaseAsset.toString))
            blockchainAclAccounts.Service.addACLAccount(setACLData.from, setACLData.aclAddress, setACLData.zoneID, setACLData.organizationID, acl)
            blockchainTransactionSetACLs.Service.addSetACLKafka(from = setACLData.from, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, acl = acl, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionSetACL.Service.post( transactionSetACL.Request(from = setACLData.from, password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAsset = setACLData.redeemAsset.toString, redeemFiat = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAsset = setACLData.releaseAsset.toString))
            blockchainAclAccounts.Service.addOrUpdateACLAccount(setACLData.from, setACLData.aclAddress, setACLData.zoneID, setACLData.organizationID, acl)
            blockchainTransactionSetACLs.Service.addSetACL(from = setACLData.from, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, acl = acl, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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