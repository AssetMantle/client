package controllers

import exceptions.BaseException
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.SendFiat
import views.companion.blockchain.SendFiat

import scala.concurrent.ExecutionContext

class SendFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionSendFiat: SendFiat)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def sendFiatForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.sendFiat(SendFiat.form))
  }

  def sendFiat: Action[AnyContent] = Action { implicit request =>
    SendFiat.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.sendFiat(formWithErrors))
      },
      sendFiatData => {
        try {
          transactionSendFiat.Service.post(new transactionSendFiat.Request(sendFiatData.from, sendFiatData.password, sendFiatData.to, sendFiatData.amount, sendFiatData.pegHash, sendFiatData.chainID, sendFiatData.gas))

          Ok("")
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
        }
      })
  }
}
