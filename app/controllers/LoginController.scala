package controllers

import controllers.results.WithUsernameToken
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.master.Accounts
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.master.Login

import scala.concurrent.ExecutionContext

class LoginController @Inject()(messagesControllerComponents: MessagesControllerComponents, accounts: Accounts, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext, configuration: play.api.Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def loginForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.login(Login.form))
  }

  def login: Action[AnyContent] = Action { implicit request =>
    Login.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.login(formWithErrors))
      },
      loginData => {
        try {
          if (accounts.Service.validateLogin(loginData.username, loginData.password)) withUsernameToken.Ok(views.html.index(success = "Logged In!"), loginData.username) else Ok(views.html.index(failure = "Invalid Login!"))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      })
  }
}
