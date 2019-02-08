package controllers

import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.Inject
import models.master.Accounts
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.blockchain.IssueFiat

import scala.concurrent.ExecutionContext

class IssueFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents, accounts: Accounts, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def issueFiatForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.issueFiat(IssueFiat.form))
  }

  def issueFiat: Action[AnyContent] = Action { implicit request =>
    IssueFiat.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.issueFiat(formWithErrors))
      },
      loginData => {
        try {
          Ok("") //if (accounts.Service.validateLogin(loginData.username, loginData.password)) withUsernameToken.Ok(views.html.index(success = "Logged In!"), loginData.username) else Ok(views.html.index(failure = "Invalid Login!"))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
        }
      })
  }
}
