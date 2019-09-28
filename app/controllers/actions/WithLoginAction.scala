package controllers.actions

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class WithLoginAction @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, blockchainACLHashes: blockchain.ACLHashes, blockchainACLAccounts: blockchain.ACLAccounts, masterTransactionSessionTokens: masterTransaction.SessionTokens)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.ACTIONS_WITH_LOGIN_ACTION

  def authenticated(f: ⇒ LoginState => Request[AnyContent] => Result)(implicit logger: Logger): Action[AnyContent] = {
    Action { implicit request ⇒
      try {
        val username = request.session.get(constants.Security.USERNAME).getOrElse(throw new BaseException(constants.Response.USERNAME_NOT_FOUND))
        masterTransactionSessionTokens.Service.tryVerifyingSessionToken(username, request.session.get(constants.Security.TOKEN).getOrElse(throw new BaseException(constants.Response.TOKEN_NOT_FOUND)))
        masterTransactionSessionTokens.Service.tryVerifyingSessionTokenTime(username)
        val address = masterAccounts.Service.getAddress(username)
        val userType = masterAccounts.Service.getUserType(username)
        f(LoginState(username, userType, address, if (userType == constants.User.TRADER) Option(blockchainACLHashes.Service.getACL(blockchainACLAccounts.Service.getACLHash(address))) else None))(request)
      }
      catch {
        case baseException: BaseException => logger.info(baseException.failure.message, baseException)
          Results.Unauthorized(views.html.index()).withNewSession
      }
    }
  }
}
