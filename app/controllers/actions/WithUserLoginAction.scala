package controllers.actions

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class WithUserLoginAction @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, masterTransactionAccountTokens: masterTransaction.AccountTokens)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.ACTIONS_WITH_USER_LOGIN_ACTION

  def authenticated(f: ⇒ LoginState => Request[AnyContent] => Result)(implicit logger: Logger): Action[AnyContent] = {
    Action { implicit request ⇒
      try {
        val username = request.session.get(constants.Security.USERNAME).getOrElse(throw new BaseException(constants.Response.USERNAME_NOT_FOUND))
        val sessionToken = request.session.get(constants.Security.TOKEN).getOrElse(throw new BaseException(constants.Response.TOKEN_NOT_FOUND))
        masterTransactionAccountTokens.Service.tryVerifyingSessionToken(username, sessionToken)
        masterTransactionAccountTokens.Service.tryVerifyingSessionTokenTime(username)
        masterAccounts.Service.tryVerifyingUserType(username, constants.User.USER)
        f(LoginState(username, constants.User.USER, masterAccounts.Service.getAddress(username), null))(request)
      }
      catch {
        case baseException: BaseException => {
          logger.info(baseException.failure.message, baseException)
          Results.Unauthorized(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    }
  }
}
