package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.IssueFiat
import views.companion.blockchain.IssueFiat

import scala.concurrent.ExecutionContext

class IssueFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionIssueFiat: IssueFiat)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def issueFiatForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.issueFiat(IssueFiat.form))
  }

  def issueFiat: Action[AnyContent] = Action { implicit request =>
    IssueFiat.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.issueFiat(formWithErrors))
      },
      issueFiatData => {
        try {
          Ok(views.html.index(transactionIssueFiat.Service.post(new transactionIssueFiat.Request(issueFiatData.from, issueFiatData.to, issueFiatData.transactionID, issueFiatData.transactionAmount, issueFiatData.chainID, issueFiatData.password, issueFiatData.gas)).txHash))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))

        }
      })
  }
}
