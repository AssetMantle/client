package controllers.actions

import controllers.logging.WithActionAsyncLoggingFilter
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WithOrWithoutLoginAction @Inject()(
                                 messagesControllerComponents: MessagesControllerComponents,
                                 withActionAsyncLoggingFilter: WithActionAsyncLoggingFilter,
                                 blockchainAccounts: blockchain.Accounts, masterAccounts:
                                 master.Accounts,
                                 masterTransactionSessionTokens: masterTransaction.SessionTokens
                               )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.ACTIONS_WITH_LOGIN_ACTION

 // case class UltimateLoginState(loginState: Option[LoginState])

  def authenticated(f: ⇒ Option[LoginState] => Request[AnyContent] => Future[Result])(implicit logger: Logger): Action[AnyContent] = {
    withActionAsyncLoggingFilter.next { implicit request ⇒

      val username = Future(request.session.get(constants.Security.USERNAME).getOrElse(throw new BaseException(constants.Response.USERNAME_NOT_FOUND)))
      val sessionToken = Future(request.session.get(constants.Security.TOKEN).getOrElse(throw new BaseException(constants.Response.TOKEN_NOT_FOUND)))

      def verifySessionTokenAndUserType(username: String, sessionToken: String) = {
        val sessionTokenVerify = masterTransactionSessionTokens.Service.tryVerifyingSessionToken(username, sessionToken)
        val tokenTimeVerify = masterTransactionSessionTokens.Service.tryVerifyingSessionTokenTime(username)
        val userType = masterAccounts.Service.getUserType(username)
        val address = blockchainAccounts.Service.tryGetAddress(username)
        for {
          _ <- sessionTokenVerify
          _ <- tokenTimeVerify
          userType <- userType
          address <- address
        } yield (userType, address)
      }

      def result(loginState: LoginState): Future[Result] = f(Some(loginState))(request)

      (for {
        username <- username
        sessionToken <- sessionToken
        (userType, address) <- verifySessionTokenAndUserType(username, sessionToken)
        result <- result(LoginState(username, userType, address))
      } yield {
        result
      }).recoverWith {
        case baseException: BaseException =>
          if(baseException.failure.message == constants.Response.USERNAME_NOT_FOUND.message){
            f(None)(request)
          } else{
            logger.info(baseException.failure.message, baseException)
            Future(Results.Unauthorized(views.html.index()).withNewSession)
          }

      }
    }
  }
}
