package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.ChangeBuyerBid
import views.companion.blockchain.ChangeBuyerBid

import scala.concurrent.ExecutionContext

class ChangeBuyerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionChangeBuyerBid: ChangeBuyerBid)(implicit exec: ExecutionContext,configuration: play.api.Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def changeBuyerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.changeBuyerBid(ChangeBuyerBid.form))
  }

  def changeBuyerBid: Action[AnyContent] = Action { implicit request =>
    ChangeBuyerBid.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.changeBuyerBid(formWithErrors))
      },
      changeBuyerBidData => {
        try {
          Ok(views.html.index(transactionChangeBuyerBid.Service.post(new transactionChangeBuyerBid.Request(changeBuyerBidData.from, changeBuyerBidData.password, changeBuyerBidData.to, changeBuyerBidData.bid, changeBuyerBidData.time, changeBuyerBidData.pegHash, changeBuyerBidData.chainID, changeBuyerBidData.gas)).txHash))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))

        }
      })
  }
}
