package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchain.{ACL, ACLAccounts, ACLHashs}
import models.blockchainTransaction.SetACLs
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.SetACL
import views.companion.blockchain.SetACL

import scala.concurrent.ExecutionContext
import scala.util.Random

class SetACLController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionSetACL: SetACL, aclAccounts: ACLAccounts, setACLs: SetACLs, aclHashs: ACLHashs)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def setACLForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.setACL(SetACL.form))
  }

  def setACL: Action[AnyContent] = Action { implicit request =>
    SetACL.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.setACL(formWithErrors))
      },
      setACLData => {
        try {
          val acl = ACL(issueAssets = setACLData.issueAsset, issueFiats = setACLData.issueFiat, sendAssets = setACLData.sendAsset, sendFiats = setACLData.sendFiat, redeemAssets = setACLData.redeemAsset, redeemFiats = setACLData.redeemFiat, sellerExecuteOrder = setACLData.sellerExecuteOrder, buyerExecuteOrder = setACLData.buyerExecuteOrder, changeBuyerBid = setACLData.changeBuyerBid, changeSellerBid = setACLData.changeSellerBid, confirmBuyerBid = setACLData.confirmBuyerBid, confirmSellerBid = setACLData.changeSellerBid, negotiation = setACLData.negotiation, releaseAssets = setACLData.releaseAsset)
          aclHashs.Service.addACLHash(acl)
          aclAccounts.Service.addACLAccount(setACLData.from, setACLData.aclAddress, setACLData.zoneID, setACLData.organizationID, acl)
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionSetACL.Service.kafkaPost( transactionSetACL.Request(from = setACLData.from, password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, issueAssets = setACLData.issueAsset, issueFiats = setACLData.issueFiat, sendAssets = setACLData.sendAsset, sendFiats = setACLData.sendFiat, redeemAssets = setACLData.redeemAsset, redeemFiats = setACLData.redeemFiat, sellerExecuteOrder = setACLData.sellerExecuteOrder, buyerExecuteOrder = setACLData.buyerExecuteOrder, changeBuyerBid = setACLData.changeBuyerBid, changeSellerBid = setACLData.changeSellerBid, confirmBuyerBid = setACLData.confirmBuyerBid, confirmSellerBid = setACLData.confirmSellerBid, negotiation = setACLData.negotiation, releaseAssets = setACLData.releaseAsset))
            setACLs.Service.addSetACLKafka(from = setACLData.from, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, acl = acl, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionSetACL.Service.post( transactionSetACL.Request(from = setACLData.from, password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, issueAssets = setACLData.issueAsset, issueFiats = setACLData.issueFiat, sendAssets = setACLData.sendAsset, sendFiats = setACLData.sendFiat, redeemAssets = setACLData.redeemAsset, redeemFiats = setACLData.redeemFiat, sellerExecuteOrder = setACLData.sellerExecuteOrder, buyerExecuteOrder = setACLData.buyerExecuteOrder, changeBuyerBid = setACLData.changeBuyerBid, changeSellerBid = setACLData.changeSellerBid, confirmBuyerBid = setACLData.confirmBuyerBid, confirmSellerBid = setACLData.confirmSellerBid, negotiation = setACLData.negotiation, releaseAssets = setACLData.releaseAsset))
            setACLs.Service.addSetACL(from = setACLData.from, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, acl = acl, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
            Ok(views.html.index(success = response.TxHash))
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      })
  }
}