package controllers.actions

import controllers.logging.WithActionAsyncLoggingFilter
import exceptions.BaseException
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WithLoginActionAsync @Inject()(
                                      messagesControllerComponents: MessagesControllerComponents,
                                      withActionAsyncLoggingFilter: WithActionAsyncLoggingFilter,
                                      blockchainAccounts: blockchain.Accounts,
                                      blockchainIdentityProvision: blockchain.IdentityProvisions,
                                      masterAccounts: master.Accounts,
                                      masterTransactionSessionTokens: masterTransaction.SessionTokens
                                    )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.ACTIONS_WITH_LOGIN_ACTION

  def apply(f: ⇒ LoginState => Request[AnyContent] => Future[Result])(implicit logger: Logger): Action[AnyContent] = {
    withActionAsyncLoggingFilter.next { implicit request ⇒

      val username = Future(request.session.get(constants.Security.USERNAME).getOrElse(throw new BaseException(constants.Response.USERNAME_NOT_FOUND)))
      val sessionToken = Future(request.session.get(constants.Security.TOKEN).getOrElse(throw new BaseException(constants.Response.TOKEN_NOT_FOUND)))

      def isProvisioned(identityID: String, address: String) = blockchainIdentityProvision.Service.checkExists(id = identityID, address = address)

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

      def getResult(loginState: LoginState): Future[Result] = f(loginState)(request)

      (for {
        username <- username
        sessionToken <- sessionToken
        (userType, address) <- verifySessionTokenAndUserType(username, sessionToken)
        result <- getResult(LoginState(username = username, userType = userType, address = address))
      } yield {
        result
      }).recover {
        case baseException: BaseException =>
          logger.info(baseException.failure.message, baseException)
          Results.Unauthorized(views.html.index()).withNewSession
      }
    }
  }
}
