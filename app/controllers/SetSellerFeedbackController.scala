package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.SetSellerFeedback
import views.companion.blockchain.SetSellerFeedback

import scala.concurrent.ExecutionContext

class SetSellerFeedbackController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionSetSellerFeedback: SetSellerFeedback)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def setSellerFeedbackForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.setSellerFeedback(SetSellerFeedback.form))
  }

  def setSellerFeedback: Action[AnyContent] = Action { implicit request =>
    SetSellerFeedback.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.setSellerFeedback(formWithErrors))
      },
      setSellerFeedbackData => {
        try {
          Ok(views.html.index(transactionSetSellerFeedback.Service.post(new transactionSetSellerFeedback.Request(setSellerFeedbackData.from, setSellerFeedbackData.password, setSellerFeedbackData.to, setSellerFeedbackData.pegHash, setSellerFeedbackData.rating, setSellerFeedbackData.chainID, setSellerFeedbackData.gas)).txHash))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      })
  }
}
