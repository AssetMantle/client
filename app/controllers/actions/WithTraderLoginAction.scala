package controllers.actions

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WithTraderLoginAction @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, blockchainACLHashes: blockchain.ACLHashes, blockchainACLAccounts: blockchain.ACLAccounts, masterTransactionAccountTokens: masterTransaction.AccountTokens)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.ACTIONS_WITH_TRADER_LOGIN_ACTION

  def authenticated(f: ⇒ LoginState => Request[AnyContent] => Future[Result])(implicit logger: Logger): Action[AnyContent] = {
    Action.async { implicit request ⇒
    try {
      val username = request.session.get(constants.Security.USERNAME).getOrElse(throw new BaseException(constants.Response.USERNAME_NOT_FOUND))
      val sessionToken = request.session.get(constants.Security.TOKEN).getOrElse(throw new BaseException(constants.Response.TOKEN_NOT_FOUND))
      val sessionTokenVerify = masterTransactionAccountTokens.Service.tryVerifyingSessionToken(username, sessionToken)
      val tokenTimeVerify = masterTransactionAccountTokens.Service.tryVerifyingSessionTokenTime(username)
      val verifyUserType = masterAccounts.Service.tryVerifyingUserType(username, constants.User.TRADER)
      val address = masterAccounts.Service.getAddress(username)

      def result(loginState: LoginState) = f(loginState)(request)

      (for {
        _ <- sessionTokenVerify
        _ <- tokenTimeVerify
        _ <- verifyUserType
        address <- address
        result <- result(LoginState(username, constants.User.TRADER, address))
      } yield result).recover {
        case baseException: BaseException => logger.info(baseException.failure.message, baseException)
          Results.Unauthorized(views.html.index(failures = Seq(baseException.failure)))
      }
    }catch {
      case baseException: BaseException => logger.info(baseException.failure.message, baseException)
        Future{Results.Unauthorized(views.html.index(failures = Seq(baseException.failure)))}
    }
    }
  }
}
