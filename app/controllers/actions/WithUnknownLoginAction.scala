package controllers.actions

import controllers.logging.WithActionAsyncLoggingFilter
import controllers.view.OtherApp
import exceptions.BaseException

import javax.inject.{Inject, Singleton}
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WithUnknownLoginAction @Inject()(messagesControllerComponents: MessagesControllerComponents, withActionAsyncLoggingFilter: WithActionAsyncLoggingFilter, blockchainAccounts: blockchain.Accounts, masterAccounts: master.Accounts, masterTransactionSessionTokens: masterTransaction.SessionTokens)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.ACTIONS_WITH_UNKNOWN_LOGIN_ACTION

  private implicit val otherApps: Seq[OtherApp] = configuration.get[Seq[Configuration]]("webApp.otherApps").map { otherApp =>
    OtherApp(url = otherApp.get[String]("url"), name = otherApp.get[String]("name"))
  }

  def authenticated(f: ⇒ LoginState => Request[AnyContent] => Future[Result])(implicit logger: Logger): Action[AnyContent] = {
    withActionAsyncLoggingFilter.next { implicit request ⇒
      val username = Future(request.session.get(constants.Security.USERNAME).getOrElse(throw new BaseException(constants.Response.USERNAME_NOT_FOUND)))
      val sessionToken = Future(request.session.get(constants.Security.TOKEN).getOrElse(throw new BaseException(constants.Response.TOKEN_NOT_FOUND)))

      def verifySessionTokenAndUserType(username: String, sessionToken: String): Future[String] = {
        val sessionTokenVerify = masterTransactionSessionTokens.Service.tryVerifyingSessionToken(username, sessionToken)
        val tokenTimeVerify = masterTransactionSessionTokens.Service.tryVerifyingSessionTokenTime(username)
        val verifyUserType = masterAccounts.Service.tryVerifyingUserType(username, constants.User.UNKNOWN)
        val address = blockchainAccounts.Service.tryGetAddressWithAccountActor(username)
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
