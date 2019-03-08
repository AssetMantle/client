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

import scala.concurrent.{ExecutionContext}
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
          val acl = ACL(issueAsset = setACLData.issueAsset, issueFiat = setACLData.issueFiat, sendAsset = setACLData.sendAsset, sendFiat = setACLData.sendFiat, redeemAsset = setACLData.redeemAsset, redeemFiat = setACLData.redeemFiat, sellerExecuteOrder = setACLData.sellerExecuteOrder, buyerExecuteOrder = setACLData.buyerExecuteOrder, changeBuyerBid = setACLData.changeBuyerBid, changeSellerBid = setACLData.changeSellerBid, confirmBuyerBid = setACLData.confirmBuyerBid, confirmSellerBid = setACLData.changeSellerBid, negotiation = setACLData.negotiation, releaseAsset = setACLData.releaseAsset)
          aclHashs.Service.addACLHash(acl)
          aclAccounts.Service.addACLAccount(setACLData.from, setACLData.aclAddress, setACLData.zoneID, setACLData.organizationID, setACLData.chainID, acl)
          setACLs.Service.addSetACL(setACLData.from, setACLData.aclAddress, setACLData.organizationID, setACLData.zoneID, setACLData.chainID,  acl, null, null, (Random.nextInt(899999999) + 100000000).toString, null)
          Ok(views.html.index(transactionSetACL.Service.post(new transactionSetACL.Request(setACLData.from, setACLData.password, setACLData.aclAddress, setACLData.organizationID, setACLData.zoneID, setACLData.chainID, setACLData.issueAsset, setACLData.issueFiat, setACLData.sendAsset, setACLData.sendFiat, setACLData.redeemAsset, setACLData.redeemFiat, setACLData.sellerExecuteOrder, setACLData.buyerExecuteOrder, setACLData.changeBuyerBid, setACLData.changeSellerBid, setACLData.confirmBuyerBid, setACLData.confirmSellerBid, setACLData.negotiation, setACLData.releaseAsset)).txHash))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      })
  }
}