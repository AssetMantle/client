package controllers.logging

import constants.AppConfig._
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}
import play.api.i18n.{I18nSupport, Lang, MessagesApi}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, Request, Result, Results}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WithActionLoggingFilter @Inject()(messagesControllerComponents: MessagesControllerComponents, messagesApi: MessagesApi, utilitiesLog: utilities.Log)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val lang: Lang = Lang(configuration.get[String]("play.log.lang"))

  def next(f: => Request[AnyContent] => Result)(implicit logger: Logger): Action[AnyContent] = Action { implicit request =>
    val startTime = System.currentTimeMillis()
    try {
      logger.info(messagesApi(constants.Log.Info.CONTROLLERS_REQUEST, request.method, request.path, request.remoteAddress, request.session.get(constants.Security.USERNAME).getOrElse(constants.View.UNKNOWN)))
      val result = f(request)
      val endTime = System.currentTimeMillis()
      logger.info(messagesApi(constants.Log.Info.CONTROLLERS_RESPONSE, request.method, request.path, request.remoteAddress, request.session.get(constants.Security.USERNAME).getOrElse(constants.View.UNKNOWN), result.header.status, endTime - startTime))
      result
    } catch {
      case baseException: BaseException =>
        val endTime = System.currentTimeMillis()
        logger.info(messagesApi(constants.Log.Info.CONTROLLERS_RESPONSE, request.method, request.path, request.remoteAddress, request.session.get(constants.Security.USERNAME).getOrElse(constants.View.UNKNOWN), Results.InternalServerError.header.status, endTime - startTime))
        Results.InternalServerError(views.html.index(failures = Seq(baseException.failure)))
    }

  }
}