package controllers.actions

import exceptions.BaseException
import javax.inject.Inject
import models.master
import models.masterTransaction.AccountTokens
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

class WithUnknownLoginAction @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, accountTokens: AccountTokens)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.ACTIONS_WITH_UNKNOWN_LOGIN_ACTION

  def authenticated(f: ⇒ String => Request[AnyContent] => Result)(implicit logger: Logger): Action[AnyContent] = {
    Action { implicit request ⇒
      try {
        val username = request.session.get(constants.Security.USERNAME).getOrElse(throw new BaseException(constants.Error.USERNAME_NOT_FOUND))
        val sessionToken = request.session.get(constants.Security.TOKEN).getOrElse(throw new BaseException(constants.Error.TOKEN_NOT_FOUND))
        accountTokens.Service.tryVerifySessionToken(username, sessionToken)
        accountTokens.Service.tryVerifySessionTokenTime(username)
        masterAccounts.Service.tryVerifyUserType(username, constants.User.UNKNOWN)
        f(username)(request)
      }
      catch {
        case baseException: BaseException => {
          logger.info(constants.Error.BASE_EXCEPTION, baseException)
          Results.Unauthorized(views.html.index(failure = Messages(baseException.message)))
        }
      }
    }
  }
}
