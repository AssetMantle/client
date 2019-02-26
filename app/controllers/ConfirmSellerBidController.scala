package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.ConfirmSellerBid
import views.companion.blockchain.ConfirmSellerBid

import scala.concurrent.ExecutionContext

class ConfirmSellerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionConfirmSellerBid: ConfirmSellerBid)(implicit exec: ExecutionContext,configuration: play.api.Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def confirmSellerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.confirmSellerBid(ConfirmSellerBid.form))
  }

  def confirmSellerBid: Action[AnyContent] = Action { implicit request =>
    ConfirmSellerBid.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.confirmSellerBid(formWithErrors))
      },
      confirmSellerBidData => {
        try {
          Ok(views.html.index(transactionConfirmSellerBid.Service.post(new transactionConfirmSellerBid.Request(confirmSellerBidData.from, confirmSellerBidData.password, confirmSellerBidData.to, confirmSellerBidData.bid, confirmSellerBidData.time, confirmSellerBidData.pegHash, confirmSellerBidData.chainID, confirmSellerBidData.gas)).txHash))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))

        }
      })
  }
}
