package controllers

import controllers.actions.WithLoginAction
import exceptions.BaseException
import javax.inject.Inject
import models.masterTransaction.AccountTokens
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.master.Logout

import scala.concurrent.ExecutionContext

class LogoutController @Inject()(messagesControllerComponents: MessagesControllerComponents, accountTokens: AccountTokens, withLoginAction: WithLoginAction)(implicit configuration: Configuration, exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_LOGOUT

  def logoutForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.logout(Logout.form))
  }

  def logout: Action[AnyContent] = withLoginAction { implicit request =>
    Logout.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.logout(formWithErrors))
      },
      loginData => {
        try {
          if (!loginData.receiveNotifications) {
            accountTokens.Service.deleteToken(request.session.get(constants.Security.USERNAME).get)
          }
          Ok(views.html.index(Messages(constants.Success.LOG_OUT))).withNewSession
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
        }
      })
  }
}
