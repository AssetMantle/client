package controllers

import controllers.actions.WithLoginAction
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.masterTransaction.AccountTokens
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import utilities.actors.ShutdownActors
import views.companion.master.Logout

import scala.concurrent.ExecutionContext

@Singleton
class LogoutController @Inject()(messagesControllerComponents: MessagesControllerComponents, accountTokens: AccountTokens, withLoginAction: WithLoginAction, shutdownActors: ShutdownActors)(implicit configuration: Configuration, exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_LOGOUT

  def logoutForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.logout(Logout.form))
  }

  def logout: Action[AnyContent] = withLoginAction.authenticated { username =>
    implicit request =>
      Logout.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.logout(formWithErrors))
        },
        loginData => {
          try {
            if (!loginData.receiveNotifications) {
              accountTokens.Service.deleteToken(username)
            }
            shutdownActors.logOutShutdown(constants.Module.ACTOR_MAIN_ASSET, username)
            shutdownActors.logOutShutdown(constants.Module.ACTOR_MAIN_FIAT, username)
            accountTokens.Service.resetSessionTokenTime(username)
            Ok(views.html.index(successes = Seq(constants.Response.LOGGED_OUT))).withNewSession
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }
}
