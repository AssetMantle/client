package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.SetBuyerFeedback
import views.companion.blockchain.SetBuyerFeedback

import scala.concurrent.ExecutionContext

class SetBuyerFeedbackController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionSetBuyerFeedback: SetBuyerFeedback)(implicit exec: ExecutionContext, configuration: play.api.Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def setBuyerFeedbackForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.setBuyerFeedback(SetBuyerFeedback.form))
  }

  def setBuyerFeedback: Action[AnyContent] = Action { implicit request =>
    SetBuyerFeedback.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.setBuyerFeedback(formWithErrors))
      },
      setBuyerFeedbackData => {
        try {
          Ok(views.html.index(transactionSetBuyerFeedback.Service.post(new transactionSetBuyerFeedback.Request(setBuyerFeedbackData.from, setBuyerFeedbackData.password, setBuyerFeedbackData.to, setBuyerFeedbackData.pegHash, setBuyerFeedbackData.rating, setBuyerFeedbackData.chainID, setBuyerFeedbackData.gas)).txHash))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))

        }
      })
  }
}
