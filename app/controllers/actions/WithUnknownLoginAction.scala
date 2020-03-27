package controllers.actions

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WithUnknownLoginAction @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, masterTransactionSessionTokens: masterTransaction.SessionTokens)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.ACTIONS_WITH_UNKNOWN_LOGIN_ACTION

  def authenticated(f: ⇒ LoginState => Request[AnyContent] => Future[Result])(implicit logger: Logger): Action[AnyContent] = {
    Action.async { implicit request ⇒
      val username = Future(request.session.get(constants.Security.USERNAME).getOrElse(throw new BaseException(constants.Response.NOT_LOGGED_IN)))
      val sessionToken = Future(request.session.get(constants.Security.TOKEN).getOrElse(throw new BaseException(constants.Response.SESSION_TIMED_OUT)))

      def verifySessionTokenAndUserType(username: String, sessionToken: String): Future[String] = {
        val sessionTokenVerify = masterTransactionSessionTokens.Service.tryVerifyingSessionToken(username, sessionToken)
        val tokenTimeVerify = masterTransactionSessionTokens.Service.tryVerifyingSessionTokenTime(username)
        val verifyUserType = masterAccounts.Service.tryVerifyingUserType(username, constants.User.UNKNOWN)
        val address = masterAccounts.Service.getAddress(username)
        for {
          _ <- sessionTokenVerify
          _ <- tokenTimeVerify
          _ <- verifyUserType
          address <- address
        } yield address
      }

      def result(loginState: LoginState): Future[Result] = f(loginState)(request)

      (for {
        username <- username
        sessionToken <- sessionToken
        address <- verifySessionTokenAndUserType(username, sessionToken)
        result <- result(LoginState(username, constants.User.UNKNOWN, address))
      } yield result).recover {
        case baseException: BaseException => logger.info(baseException.failure.message, baseException)
          Results.Unauthorized(views.html.index(failures = Seq(baseException.failure)))
      }
    }
  }
}
