package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.SetACL
import views.companion.blockchain.SetACL

import scala.concurrent.ExecutionContext

class SetACLController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionSetACL: SetACL)(implicit exec: ExecutionContext, configuration: play.api.Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

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
          Ok(views.html.index(transactionSetACL.Service.post(new transactionSetACL.Request(setACLData.from, setACLData.password, setACLData.aclAddress, setACLData.organizationID, setACLData.zoneID, setACLData.chainID, setACLData.issueAsset, setACLData.issueFiat, setACLData.sendAsset, setACLData.sendFiat, setACLData.redeemAsset, setACLData.redeemFiat, setACLData.sellerExecuteOrder, setACLData.buyerExecuteOrder, setACLData.changeBuyerBid, setACLData.changeSellerBid, setACLData.confirmBuyerBid, setACLData.confirmSellerBid, setACLData.negotiation, setACLData.releaseAssets)).txHash))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      })
  }
}