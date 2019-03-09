package controllers

import controllers.results.WithUsernameToken
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.master.{Accounts, Notifications}
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import utilities.PushNotifications
import views.companion.master.Login

import scala.concurrent.ExecutionContext

class LoginController @Inject()(messagesControllerComponents: MessagesControllerComponents, accounts: Accounts, withUsernameToken: WithUsernameToken, pushNotifications: PushNotifications)(implicit exec: ExecutionContext, configuration: Configuration, wsClient: WSClient, notifications: Notifications) extends AbstractController(messagesControllerComponents) with I18nSupport {

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
          if (accounts.Service.validateLogin(loginData.username, loginData.password)) {
            pushNotifications.Push.registerNotificationToken(loginData.username, request.body.asFormUrlEncoded.get("token").headOption.get)
            pushNotifications.Push.sendNotification(loginData.username, "Login", "")
            withUsernameToken.Ok(views.html.index(success = "Logged In!"), loginData.username)
          }
          else Ok(views.html.index(failure = "Invalid Login!"))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      })
  }
}
