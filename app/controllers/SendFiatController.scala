package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.SendFiat
import views.companion.blockchain.SendFiat

import scala.concurrent.ExecutionContext

class SendFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionSendFiat: SendFiat)(implicit exec: ExecutionContext, configuration: play.api.Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def sendFiatForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.sendFiat(SendFiat.form))
  }

  def sendFiat: Action[AnyContent] = Action { implicit request =>
    SendFiat.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.sendFiat(formWithErrors))
      },
      sendFiatData => {
        try {
          Ok(views.html.index(transactionSendFiat.Service.post(new transactionSendFiat.Request(sendFiatData.from, sendFiatData.password, sendFiatData.to, sendFiatData.amount, sendFiatData.pegHash, sendFiatData.chainID, sendFiatData.gas)).txHash))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      })
  }
}
