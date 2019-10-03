package controllers.actions

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WithLoginAction @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, blockchainACLHashes: blockchain.ACLHashes, blockchainACLAccounts: blockchain.ACLAccounts, masterTransactionAccountTokens: masterTransaction.AccountTokens)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.ACTIONS_WITH_LOGIN_ACTION

  def authenticated(f: ⇒ LoginState => Request[AnyContent] => Future[Result])(implicit logger: Logger): Action[AnyContent] = {
    Action.async { implicit request ⇒

      val username = request.session.get(constants.Security.USERNAME).getOrElse(throw new BaseException(constants.Response.USERNAME_NOT_FOUND))
      val sessionToken = request.session.get(constants.Security.TOKEN).getOrElse(throw new BaseException(constants.Response.TOKEN_NOT_FOUND))
      val sessionTokenVerify = masterTransactionAccountTokens.Service.tryVerifyingSessionToken(username, sessionToken)
      val tokenTimeVerify = masterTransactionAccountTokens.Service.tryVerifyingSessionTokenTime(username)
      val userTypeFuture = masterAccounts.Service.getUserType(username)
      val addressFuture = masterAccounts.Service.getAddress(username)
      def result(loginState: LoginState)=f(loginState)(request)
      (for {
        _ <- sessionTokenVerify
        _ <- tokenTimeVerify
        userType <- userTypeFuture
        address <- addressFuture
        aclHash <- blockchainACLAccounts.Service.getACLHash(address)
        acl <- blockchainACLHashes.Service.getACL(aclHash)
        result <- result(LoginState(username, userType, address, if (userType == constants.User.TRADER) Option(acl) else None))
      } yield result).recover {
        case baseException: BaseException => logger.info(baseException.failure.message, baseException)
          Results.Unauthorized(views.html.index()).withNewSession
      }
    }
  }
}
