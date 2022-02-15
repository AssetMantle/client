package controllers.actions

import controllers.logging.{WithActionAsyncLoggingFilter, WithActionLoggingFilter}
import constants.AppConfig._
import exceptions.BaseException

import javax.inject.{Inject, Singleton}
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WithoutLoginActionAsync @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                        withActionAsyncLoggingFilter: WithActionAsyncLoggingFilter,
                                        blockchainAccounts: blockchain.Accounts, masterAccounts:
                                        master.Accounts,
                                        masterTransactionSessionTokens: masterTransaction.SessionTokens)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.ACTIONS_WITH_LOGIN_ACTION

  def apply(f: ⇒ Option[LoginState] => Request[AnyContent] => Future[Result])(implicit logger: Logger): Action[AnyContent] = {
    withActionAsyncLoggingFilter.next { implicit request ⇒
      val username = request.session.get(constants.Security.USERNAME)
      val sessionToken = request.session.get(constants.Security.TOKEN)
      val identityID = request.session.get(constants.Security.IDENTITY_ID)

      def verifySessionTokenUserTypeAndGetResult(username: Option[String], sessionToken: Option[String]) = {
        username match {
          case Some(username) => {
            val sessionTokenVerify = masterTransactionSessionTokens.Service.tryVerifyingSessionToken(username, sessionToken.getOrElse(throw new BaseException(constants.Response.TOKEN_NOT_FOUND)))
            val tokenTimeVerify = masterTransactionSessionTokens.Service.tryVerifyingSessionTokenTime(username)
            val userType = masterAccounts.Service.getUserType(username)
            val address = blockchainAccounts.Service.tryGetAddress(username)
            for {
              _ <- sessionTokenVerify
              _ <- tokenTimeVerify
              userType <- userType
              address <- address
              result <- f(Some(LoginState(username = username, userType = userType, address = address, identityID = identityID.getOrElse(""))))(request)
            } yield result
          }
          case None => f(None)(request)
        }
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
