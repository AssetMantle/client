package controllers.actions

import constants.AppConfig._
import controllers.logging.WithActionAsyncLoggingFilter
import exceptions.BaseException
import models.blockchain
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WithoutLoginAction @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                   withActionAsyncLoggingFilter: WithActionAsyncLoggingFilter,
                                   blockchainAccounts: blockchain.Accounts)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.ACTIONS_WITH_LOGIN_ACTION

  def apply(f: => Option[LoginState] => Request[AnyContent] => Result)(implicit logger: Logger): Action[AnyContent] = {
    withActionAsyncLoggingFilter.next { implicit request =>
      try {
        Future(f(None)(request))
      } catch {
        case baseException: BaseException => Future(Results.InternalServerError(views.html.index(failures = Seq(baseException.failure))))
      }
    }
  }
}
