package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}

class LogoutController @Inject()(messagesControllerComponents: MessagesControllerComponents)(implicit configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_LOGOUT

  def logoutForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.logout(Logout.form))
  }

  def logout: Action[AnyContent] = Action { implicit request =>
    Logout.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.login(formWithErrors))
      },
      loginData => {
        try {
          if (accounts.Service.validateLogin(loginData.username, loginData.password)) {
            pushNotifications.registerNotificationToken(loginData.username, request.body.asFormUrlEncoded.get("token").headOption.get)
            pushNotifications.sendNotification(loginData.username, constants.Notification.LOGIN)
            withUsernameToken.Ok(views.html.index(success = "Logged In!"), loginData.username)
          }
          else Ok(views.html.index()).withNewSession
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      })
  }
}
