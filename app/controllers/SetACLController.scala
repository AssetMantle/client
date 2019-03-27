package controllers

import controllers.actions.WithLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchain.{ACL, ACLAccounts, ACLHashs}
import models.blockchainTransaction.SetACLs
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.SetACL
import views.companion.blockchain.SetACL
import views.companion.master

import scala.concurrent.ExecutionContext
import scala.util.Random

class SetACLController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, transactionSetACL: SetACL, aclAccounts: ACLAccounts, setACLs: SetACLs, aclHashs: ACLHashs)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def setACLForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.setACL(master.SetACL.form))
  }

  def setACL: Action[AnyContent] = withLoginAction { implicit request =>
    master.SetACL.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.setACL(formWithErrors))
      },
      setACLData => {
        try {
          val acl = ACL(issueAssets = setACLData.issueAsset, issueFiats = setACLData.issueFiat, sendAssets = setACLData.sendAsset, sendFiats = setACLData.sendFiat, redeemAssets = setACLData.redeemAsset, redeemFiats = setACLData.redeemFiat, sellerExecuteOrder = setACLData.sellerExecuteOrder, buyerExecuteOrder = setACLData.buyerExecuteOrder, changeBuyerBid = setACLData.changeBuyerBid, changeSellerBid = setACLData.changeSellerBid, confirmBuyerBid = setACLData.confirmBuyerBid, confirmSellerBid = setACLData.changeSellerBid, negotiation = setACLData.negotiation, releaseAssets = setACLData.releaseAsset)
          aclHashs.Service.addACLHash(acl)
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionSetACL.Service.kafkaPost( transactionSetACL.Request(from = request.session.get(constants.Security.USERNAME).get, password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAssets = setACLData.redeemAsset.toString, redeemFiats = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAssets = setACLData.releaseAsset.toString))
            aclAccounts.Service.addACLAccount(request.session.get(constants.Security.USERNAME).get, setACLData.aclAddress, setACLData.zoneID, setACLData.organizationID, acl)
            setACLs.Service.addSetACLKafka(from = request.session.get(constants.Security.USERNAME).get, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, acl = acl, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionSetACL.Service.post( transactionSetACL.Request(from = request.session.get(constants.Security.USERNAME).get, password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAssets = setACLData.redeemAsset.toString, redeemFiats = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAssets = setACLData.releaseAsset.toString))
            aclAccounts.Service.addACLAccount(request.session.get(constants.Security.USERNAME).get, setACLData.aclAddress, setACLData.zoneID, setACLData.organizationID, acl)
            setACLs.Service.addSetACL(from = request.session.get(constants.Security.USERNAME).get, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, acl = acl, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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
    Ok(views.html.component.blockchain.setACL(SetACL.form))
  }

  def blockchainSetACL: Action[AnyContent] = Action { implicit request =>
    SetACL.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.setACL(formWithErrors))
      },
      setACLData => {
        try {
          val acl = ACL(issueAssets = setACLData.issueAsset, issueFiats = setACLData.issueFiat, sendAssets = setACLData.sendAsset, sendFiats = setACLData.sendFiat, redeemAssets = setACLData.redeemAsset, redeemFiats = setACLData.redeemFiat, sellerExecuteOrder = setACLData.sellerExecuteOrder, buyerExecuteOrder = setACLData.buyerExecuteOrder, changeBuyerBid = setACLData.changeBuyerBid, changeSellerBid = setACLData.changeSellerBid, confirmBuyerBid = setACLData.confirmBuyerBid, confirmSellerBid = setACLData.changeSellerBid, negotiation = setACLData.negotiation, releaseAssets = setACLData.releaseAsset)
          aclHashs.Service.addACLHash(acl)
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionSetACL.Service.kafkaPost( transactionSetACL.Request(from = setACLData.from, password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAssets = setACLData.redeemAsset.toString, redeemFiats = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAssets = setACLData.releaseAsset.toString))
            aclAccounts.Service.addACLAccount(setACLData.from, setACLData.aclAddress, setACLData.zoneID, setACLData.organizationID, acl)
            setACLs.Service.addSetACLKafka(from = setACLData.from, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, acl = acl, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionSetACL.Service.post( transactionSetACL.Request(from = setACLData.from, password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAssets = setACLData.redeemAsset.toString, redeemFiats = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAssets = setACLData.releaseAsset.toString))
            aclAccounts.Service.addACLAccount(setACLData.from, setACLData.aclAddress, setACLData.zoneID, setACLData.organizationID, acl)
            setACLs.Service.addSetACL(from = setACLData.from, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, acl = acl, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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