package controllers.logging

import controllers.actions.LoginState
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, master, masterTransaction}
import org.slf4j.MarkerFactory
import play.api.{Configuration, Logger, MarkerContext}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, Request, Result, Results}
import play.api.i18n.{Lang, MessagesApi}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WithActionAsyncLoggingFilter @Inject()(messagesControllerComponents: MessagesControllerComponents, messagesApi: MessagesApi, utilitiesLog: utilities.Log)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val language = configuration.get[String]("play.log.lang")

  def next(f: => Request[AnyContent] => Future[Result])(implicit logger: Logger): Action[AnyContent] = Action.async { implicit request ⇒
    val startTime = System.currentTimeMillis()
    logger.info(messagesApi(constants.Log.Info.CONTROLLERS_REQUEST, request.method, request.path, request.remoteAddress, request.session.get(constants.Security.USERNAME).getOrElse("None"))(Lang(language)))
    val result = f(request)
    (for {
      result <- result
    } yield {
      val endTime = System.currentTimeMillis()
      logger.info(messagesApi(constants.Log.Info.CONTROLLERS_RESPONSE, request.method, request.path, request.remoteAddress, request.session.get(constants.Security.USERNAME).getOrElse("None"), result.header.status, endTime - startTime)(Lang(language)))
      result
    }).recover {
      case baseException: BaseException =>
        val endTime = System.currentTimeMillis()
        logger.info(messagesApi(constants.Log.Info.CONTROLLERS_RESPONSE, request.method, request.path, request.remoteAddress, request.session.get(constants.Security.USERNAME).getOrElse("None"), Results.InternalServerError.header.status, endTime - startTime)(Lang(language)))
        Results.InternalServerError(views.html.index(failures = Seq(baseException.failure)))
    }
  }
}