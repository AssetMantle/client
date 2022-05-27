package controllers.actions

import constants.AppConfig._
import controllers.logging.WithActionAsyncLoggingFilter
import exceptions.BaseException
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WithUserLoginAction @Inject()(
                                     messagesControllerComponents: MessagesControllerComponents,
                                     withActionAsyncLoggingFilter: WithActionAsyncLoggingFilter,
                                     blockchainAccounts: blockchain.Accounts,
                                     blockchainIdentityProvision: blockchain.IdentityProvisions,
                                     masterAccounts: master.Accounts,
                                     masterTransactionSessionTokens: masterTransaction.SessionTokens
                                   )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.ACTIONS_WITH_USER_LOGIN_ACTION

  def authenticated(f: => LoginState => Request[AnyContent] => Future[Result])(implicit logger: Logger): Action[AnyContent] = {
    withActionAsyncLoggingFilter.next { implicit request =>
      val username = Future(request.session.get(constants.Security.USERNAME).getOrElse(throw new BaseException(constants.Response.USERNAME_NOT_FOUND)))
      val sessionToken = Future(request.session.get(constants.Security.TOKEN).getOrElse(throw new BaseException(constants.Response.TOKEN_NOT_FOUND)))
      val identityID = Future(request.session.get(constants.Security.IDENTITY_ID).getOrElse(throw new BaseException(constants.Response.SESSION_IDENTITY_ID_NOT_FOUND)))

      def isProvisioned(identityID: String, address: String) = blockchainIdentityProvision.Service.checkExists(id = identityID, address = address)

      def verifySessionTokenAndUserType(username: String, sessionToken: String): Future[String] = {
        val sessionTokenVerify = masterTransactionSessionTokens.Service.tryVerifyingSessionToken(username, sessionToken)
        val tokenTimeVerify = masterTransactionSessionTokens.Service.tryVerifyingSessionTokenTime(username)
        val verifyUserType = masterAccounts.Service.tryVerifyingUserType(username, constants.User.USER)
        val address = blockchainAccounts.Service.tryGetAddress(username)
        for {
          _ <- sessionTokenVerify
          _ <- tokenTimeVerify
          _ <- verifyUserType
          address <- address
        } yield address
      }

      def getResult(loginState: LoginState, isProvisioned: Boolean): Future[Result] = if (isProvisioned) f(loginState)(request)
      else Future(throw new BaseException(constants.Response.SESSION_IDENTITY_ID_ADDRESS_NOT_PROVISIONED))

      (for {
        username <- username
        sessionToken <- sessionToken
        identityID <- identityID
        address <- verifySessionTokenAndUserType(username, sessionToken)
        isProvisioned <- isProvisioned(identityID = identityID, address = address)
        result <- getResult(LoginState(username = username, userType = constants.User.USER, address = address, identityID = identityID), isProvisioned)
      } yield result).recover {
        case baseException: BaseException => logger.info(baseException.failure.message, baseException)
          Results.Unauthorized(views.html.index(failures = Seq(baseException.failure)))
      }
    }
  }
}
