package controllers.logging

import constants.AppConfig._
import exceptions.BaseException
import play.api.i18n.{I18nSupport, Lang, MessagesApi}
import play.api.mvc._
import play.api.Configuration
import org.slf4j.{Logger, LoggerFactory}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WithActionAsyncLoggingFilter @Inject()(messagesControllerComponents: MessagesControllerComponents, messagesApi: MessagesApi, utilitiesLog: utilities.Log)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val lang: Lang = Lang(configuration.get[String]("play.log.lang"))

  def next(f: => Request[AnyContent] => Future[Result])(implicit logger: Logger): Action[AnyContent] = Action.async { implicit request =>
    val startTime = System.currentTimeMillis()
    logger.info(messagesApi(constants.Log.Info.CONTROLLERS_REQUEST, request.method, request.path, request.remoteAddress))
    val result = f(request)
    (for {
      result <- result
    } yield {
      val endTime = System.currentTimeMillis()
      logger.info(messagesApi(constants.Log.Info.CONTROLLERS_RESPONSE, request.method, request.path, request.remoteAddress, result.header.status, endTime - startTime))
      result
    }).recover {
      case baseException: BaseException =>
        val endTime = System.currentTimeMillis()
        logger.info(messagesApi(constants.Log.Info.CONTROLLERS_RESPONSE, request.method, request.path, request.remoteAddress, Results.InternalServerError.header.status, endTime - startTime))
        Results.InternalServerError(views.html.index(failures = Seq(baseException.failure)))
    }
  }
}