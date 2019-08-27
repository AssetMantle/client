package controllers

import actors.ShutdownActors
import controllers.actions.WithLoginAction
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master
import models.masterTransaction.AccountTokens
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.master.Logout

import scala.concurrent.ExecutionContext

@Singleton
class LogoutController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, accountTokens: AccountTokens, withLoginAction: WithLoginAction, shutdownActors: ShutdownActors)(implicit configuration: Configuration, exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_LOGOUT

  def logoutForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.logout(Logout.form))
  }

  def logout: Action[AnyContent] = withLoginAction.authenticated { loginState =>
    implicit request =>
      Logout.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.logout(formWithErrors))
        },
        loginData => {
          try {
            if (!loginData.receiveNotifications) {
              accountTokens.Service.deleteToken(loginState.username)
            } else {
              accountTokens.Service.resetSessionTokenTime(loginState.username)
            }
            shutdownActors.onLogOut(constants.Module.ACTOR_MAIN_ACCOUNT, loginState.username)
            if (masterAccounts.Service.getUserType(loginState.username) == constants.User.TRADER) {
              shutdownActors.onLogOut(constants.Module.ACTOR_MAIN_ASSET, loginState.username)
              shutdownActors.onLogOut(constants.Module.ACTOR_MAIN_FIAT, loginState.username)
              shutdownActors.onLogOut(constants.Module.ACTOR_MAIN_NEGOTIATION, loginState.username)
              shutdownActors.onLogOut(constants.Module.ACTOR_MAIN_ORDER, loginState.username)
            }
            Ok(views.html.index(successes = Seq(constants.Response.LOGGED_OUT))).withNewSession
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }
}
