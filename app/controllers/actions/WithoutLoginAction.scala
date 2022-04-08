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
class WithoutLoginAction @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                   withActionAsyncLoggingFilter: WithActionAsyncLoggingFilter,
                                   blockchainAccounts: blockchain.Accounts,
                                   blockchainIdentityProvision: blockchain.IdentityProvisions,
                                   masterAccounts: master.Accounts,
                                   masterTransactionSessionTokens: masterTransaction.SessionTokens)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.ACTIONS_WITH_LOGIN_ACTION

  def apply(f: ⇒ Option[LoginState] => Request[AnyContent] => Result)(implicit logger: Logger): Action[AnyContent] = {
    withActionAsyncLoggingFilter.next { implicit request ⇒
      val username = request.session.get(constants.Security.USERNAME)
      val sessionToken = request.session.get(constants.Security.TOKEN)

      def verifySessionTokenUserTypeAndGetResult(username: Option[String], sessionToken: Option[String]) = {
        if (username.nonEmpty && sessionToken.nonEmpty) {
          val sessionTokenVerify = masterTransactionSessionTokens.Service.tryVerifyingSessionToken(username.get, sessionToken.getOrElse(throw new BaseException(constants.Response.TOKEN_NOT_FOUND)))
          val tokenTimeVerify = masterTransactionSessionTokens.Service.tryVerifyingSessionTokenTime(username.get)
          val userType = masterAccounts.Service.getUserType(username.get)
          val address = blockchainAccounts.Service.tryGetAddress(username.get)
          for {
            _ <- sessionTokenVerify
            _ <- tokenTimeVerify
            userType <- userType
            address <- address
          } yield f(Some(LoginState(username = username.get, userType = userType, address = address)))(request)
        } else if (username.isEmpty && sessionToken.isEmpty) {
          Future(f(None)(request))
        } else Future(throw new BaseException(constants.Response.INVALID_SESSION))
      }

      (for {
        result <- verifySessionTokenUserTypeAndGetResult(username, sessionToken)
      } yield result).recover {
        case baseException: BaseException =>
          Results.InternalServerError(views.html.index(failures = Seq(baseException.failure))).withNewSession
      }
    }
  }
}
