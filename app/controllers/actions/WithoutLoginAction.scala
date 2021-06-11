package controllers.actions

import controllers.logging.{WithActionAsyncLoggingFilter, WithActionLoggingFilter}
import controllers.view.OtherApp
import exceptions.BaseException

import javax.inject.{Inject, Singleton}
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WithoutLoginAction @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                   withActionAsyncLoggingFilter: WithActionAsyncLoggingFilter,
                                   blockchainAccounts: blockchain.Accounts, masterAccounts:
                                   master.Accounts,
                                   masterTransactionSessionTokens: masterTransaction.SessionTokens)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.ACTIONS_WITH_LOGIN_ACTION

  private implicit val otherApps: Seq[OtherApp] = configuration.get[Seq[Configuration]]("webApp.otherApps").map { otherApp =>
    OtherApp(url = otherApp.get[String]("url"), name = otherApp.get[String]("name"))
  }

  def apply(f: ⇒ Option[LoginState] => Request[AnyContent] => Result)(implicit logger: Logger): Action[AnyContent] = {
    withActionAsyncLoggingFilter.next { implicit request ⇒
      val username = Future(request.session.get(constants.Security.USERNAME))
      val sessionToken = Future(request.session.get(constants.Security.TOKEN))


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
            } yield f(Some(LoginState(username, userType, address)))(request)
          }
          case None => Future(f(None)(request))
        }
      }

      (for {
        username <- username
        sessionToken <- sessionToken
        result <- verifySessionTokenUserTypeAndGetResult(username, sessionToken)
      } yield result).recover {
        case baseException: BaseException =>
          Results.InternalServerError(views.html.index(failures = Seq(baseException.failure))).withNewSession
      }
    }
  }
}
