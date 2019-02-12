package controllers

import exceptions.BaseException
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.RedeemFiat
import views.companion.blockchain.RedeemFiat

import scala.concurrent.ExecutionContext

class RedeemFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionRedeemFiat: RedeemFiat)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def redeemFiatForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.redeemFiat(RedeemFiat.form))
  }

  def redeemFiat: Action[AnyContent] = Action { implicit request =>
    RedeemFiat.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.redeemFiat(formWithErrors))
      },
      redeemFiatData => {
        try {
          transactionRedeemFiat.Service.post(new transactionRedeemFiat.Request(redeemFiatData.from, redeemFiatData.password, redeemFiatData.to, redeemFiatData.redeemAmount, redeemFiatData.chainID, redeemFiatData.gas))
          Ok("")
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
        }
      })
  }
}
