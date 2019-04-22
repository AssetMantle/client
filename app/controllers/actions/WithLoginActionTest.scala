package controllers.actions

import exceptions.BaseException
import javax.inject.Inject
import models.master
import models.masterTransaction.AccountTokens
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._

import scala.concurrent.ExecutionContext

class WithLoginActionTest @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, accountTokens: AccountTokens)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.MASTER_ACCOUNT

  def isAuthenticated(f: ⇒ String => Request[AnyContent] => Result) = {
    Action { request ⇒
      getUsername(request).map {
        case constants.Error.NOT_LOGGED_IN => onUnauthenticated(request)
        case constants.Error.TOKEN_TIMEOUT => onTokenTimeout(request)
        case constants.Error.INVALID_TOKEN => onInvalidToken(request)
        case username => f(username)(request)
      }.getOrElse(onUnauthenticated(request))
    }
  }

  def getUsername(request: RequestHeader): Option[String] = {
    try{
      val username = request.session.get(constants.Security.USERNAME).getOrElse(throw new BaseException(constants.Error.NOT_LOGGED_IN))
      val sessionToken = request.session.get(constants.Security.TOKEN).getOrElse(throw new BaseException(constants.Error.NOT_LOGGED_IN))
      accountTokens.Service.tryVerifySessionToken(username, sessionToken)
      accountTokens.Service.tryVerifySessionTokenTime(username)
      Some(username)
    }
    catch{
      case baseException: BaseException => println(baseException.message.stripPrefix(module+"."));Some(baseException.message.stripPrefix(module+"."))
    }
  }

  def onUnauthenticated(implicit request: RequestHeader) = Results.NotFound(views.html.index(failure = Messages(constants.Error.NOT_LOGGED_IN)))

  def onTokenTimeout(implicit request: RequestHeader) = Results.GatewayTimeout(views.html.index(failure = Messages(constants.Error.TOKEN_TIMEOUT)))

  def onInvalidToken(implicit request: RequestHeader) = Results.NotFound(views.html.index(failure = Messages(constants.Error.INVALID_TOKEN)))

}
