package controllers.actions

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WithLoginAction @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, blockchainACLHashes: blockchain.ACLHashes, blockchainACLAccounts: blockchain.ACLAccounts, masterTransactionSessionTokens: masterTransaction.SessionTokens)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.ACTIONS_WITH_LOGIN_ACTION

  def authenticated(f: ⇒ LoginState => Request[AnyContent] => Future[Result])(implicit logger: Logger): Action[AnyContent] = {
    Action.async { implicit request ⇒
      try {
        val username = request.session.get(constants.Security.USERNAME).getOrElse(throw new BaseException(constants.Response.USERNAME_NOT_FOUND))
        val sessionToken = request.session.get(constants.Security.TOKEN).getOrElse(throw new BaseException(constants.Response.TOKEN_NOT_FOUND))
        val sessionTokenVerify = masterTransactionSessionTokens.Service.tryVerifyingSessionToken(username, sessionToken)
        val tokenTimeVerify = masterTransactionSessionTokens.Service.tryVerifyingSessionTokenTime(username)
        val address = masterAccounts.Service.getAddress(username)
        val userType = masterAccounts.Service.getUserType(username)

        def getLoginState(address: String, userType: String) = {
          if (userType == constants.User.TRADER) {
            val aclHash = blockchainACLAccounts.Service.getACLHash(address)

            def acl(aclHash: String) = blockchainACLHashes.Service.getACL(aclHash)

            for {
              aclHash <- aclHash
              acl <- acl(aclHash)
            } yield LoginState(username, userType, address, Option(acl))
          } else Future {
            LoginState(username, userType, address, None)
          }
        }

        def result(loginState: LoginState) = f(loginState)(request)

        (for {
          _ <- sessionTokenVerify
          _ <- tokenTimeVerify
          userType <- userType
          address <- address
          loginState <- getLoginState(address, userType)
          result <- result(loginState)
        } yield result).recover {
          case baseException: BaseException => logger.info(baseException.failure.message, baseException)
            Results.Unauthorized(views.html.index()).withNewSession
        }
      }
      catch {
        case baseException: BaseException => logger.info(baseException.failure.message, baseException)
          Future {
            Results.Unauthorized(views.html.index()).withNewSession
          }
      }
    }
  }
}
