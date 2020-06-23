package controllers.actions

import controllers.logging.{WithActionAsyncLoggingFilter, WithActionLoggingFilter}
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WithoutLoginAction @Inject()(messagesControllerComponents: MessagesControllerComponents, withActionLoggingFilter: WithActionLoggingFilter)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.ACTIONS_WITH_LOGIN_ACTION

  def apply(f: ⇒ Request[AnyContent] => Result)(implicit logger: Logger): Action[AnyContent] = {
    withActionLoggingFilter.next { implicit request ⇒
      try {
        f(request)
      } catch {
        case baseException: BaseException =>
          Results.InternalServerError(views.html.index(Seq(baseException.failure)))
      }
    }
  }
}
