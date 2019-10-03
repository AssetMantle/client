package controllers.actions

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WithOrganizationLoginAction @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, masterTransactionAccountTokens: masterTransaction.AccountTokens)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.ACTIONS_WITH_ORGANIZATION_LOGIN_ACTION

  def authenticated(f: ⇒ LoginState => Request[AnyContent] => Future[Result])(implicit logger: Logger): Action[AnyContent] = {
    Action.async { implicit request ⇒

      val username = request.session.get(constants.Security.USERNAME).getOrElse(throw new BaseException(constants.Response.USERNAME_NOT_FOUND))
      val sessionToken = request.session.get(constants.Security.TOKEN).getOrElse(throw new BaseException(constants.Response.TOKEN_NOT_FOUND))
      val sessionTokenVerify = masterTransactionAccountTokens.Service.tryVerifyingSessionToken(username, sessionToken)
      val tokenTimeVerify = masterTransactionAccountTokens.Service.tryVerifyingSessionTokenTime(username)
      val verifyUserTypeFuture = masterAccounts.Service.tryVerifyingUserType(username, constants.User.ORGANIZATION)
      val addressFuture = masterAccounts.Service.getAddress(username)
      def result(loginState: LoginState)=f(loginState)(request)
      (for {
        _ <- sessionTokenVerify
        _ <- tokenTimeVerify
        _ <- verifyUserTypeFuture
        address <- addressFuture
        result <- result(LoginState(username, constants.User.ORGANIZATION, address))
      } yield result).recover {
        case baseException: BaseException => logger.info(baseException.failure.message, baseException)
          Results.Unauthorized(views.html.index(failures = Seq(baseException.failure)))
      }

    }
  }
}
