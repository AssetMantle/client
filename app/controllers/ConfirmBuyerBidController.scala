package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.ConfirmBuyerBid
import views.companion.blockchain.ConfirmBuyerBid

import scala.concurrent.ExecutionContext

class ConfirmBuyerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionConfirmBuyerBid: ConfirmBuyerBid)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def confirmBuyerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.confirmBuyerBid(ConfirmBuyerBid.form))
  }

  def confirmBuyerBid: Action[AnyContent] = Action { implicit request =>
    ConfirmBuyerBid.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.confirmBuyerBid(formWithErrors))
      },
      confirmBuyerBidData => {
        try {
          Ok(views.html.index(transactionConfirmBuyerBid.Service.post(new transactionConfirmBuyerBid.Request(confirmBuyerBidData.from, confirmBuyerBidData.password, confirmBuyerBidData.to, confirmBuyerBidData.bid, confirmBuyerBidData.time, confirmBuyerBidData.pegHash, confirmBuyerBidData.chainID, confirmBuyerBidData.gas)).txHash))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))

        }
      })
  }
}
