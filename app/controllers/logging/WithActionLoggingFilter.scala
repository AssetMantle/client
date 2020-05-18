package controllers.logging

import controllers.actions.LoginState
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, master, masterTransaction}
import play.api.{Configuration, Logger}
import play.api.i18n.{I18nSupport, Lang, MessagesApi}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, Request, Result, Results}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WithActionLoggingFilter @Inject()(messagesControllerComponents: MessagesControllerComponents,messagesApi: MessagesApi,utilitiesLog: utilities.Log)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val language= configuration.get[String]("play.log.lang")

  def next(f: => Request[AnyContent] => Result)(implicit logger: Logger): Action[AnyContent] = Action { implicit request ⇒
    val startTime = System.currentTimeMillis()
    try {
      utilitiesLog.infoLog(messagesApi(constants.Log.Info.CONTROLLERS_REQUEST,request.method,request.path,request.remoteAddress,request.session.get(constants.Security.USERNAME).getOrElse("None"))(Lang(language)))
      val result = f(request)
      val endTime = System.currentTimeMillis()
      utilitiesLog.infoLog(messagesApi(constants.Log.Info.CONTROLLERS_RESPONSE,request.method,request.path,request.remoteAddress,request.session.get(constants.Security.USERNAME).getOrElse("None"),result.header.status,endTime - startTime)(Lang(language)))
      result
    } catch {
      case baseException: BaseException =>
        val endTime = System.currentTimeMillis()
        utilitiesLog.infoLog(messagesApi(constants.Log.Info.CONTROLLERS_RESPONSE,request.method,request.path,request.remoteAddress,request.session.get(constants.Security.USERNAME).getOrElse("None"),Results.InternalServerError.header.status,endTime - startTime)(Lang(language)))
        Results.InternalServerError(views.html.index(failures = Seq(baseException.failure)))
    }

  }
}