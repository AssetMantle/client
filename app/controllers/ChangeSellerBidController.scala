package controllers

import exceptions.BaseException
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.ChangeSellerBid
import views.companion.blockchain.ChangeSellerBid

import scala.concurrent.ExecutionContext

class ChangeSellerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionChangeSellerBid: ChangeSellerBid)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def changeSellerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.changeSellerBid(ChangeSellerBid.form))
  }

  def changeSellerBid: Action[AnyContent] = Action { implicit request =>
    ChangeSellerBid.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.changeSellerBid(formWithErrors))
      },
      changeSellerBidData => {
        try {
          transactionChangeSellerBid.Service.post(new transactionChangeSellerBid.Request(changeSellerBidData.from, changeSellerBidData.password, changeSellerBidData.to, changeSellerBidData.bid, changeSellerBidData.time, changeSellerBidData.pegHash, changeSellerBidData.chainID, changeSellerBidData.gas))
          Ok("")
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
        }
      })
  }
}
