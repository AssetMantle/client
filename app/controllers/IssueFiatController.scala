package controllers

import exceptions.BaseException
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.IssueFiat
import views.companion.blockchain.IssueFiat

import scala.concurrent.ExecutionContext

class IssueFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionIssueFiat: IssueFiat)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def issueFiatForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.issueFiat(IssueFiat.form))
  }

  def issueFiat: Action[AnyContent] = Action { implicit request =>
    IssueFiat.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.issueFiat(formWithErrors))
      },
      issueFiatData => {
        try {
          transactionIssueFiat.Service.post(new transactionIssueFiat.Request(issueFiatData.from, issueFiatData.to, issueFiatData.transactionID, issueFiatData.transactionAmount, issueFiatData.chainID, issueFiatData.password, issueFiatData.gas))

          Ok("")
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
        }
      })
  }
}
