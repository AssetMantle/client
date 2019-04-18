package controllers.actions

import javax.inject.Inject
import models.masterTransaction.AccountTokens
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._

import scala.concurrent.ExecutionContext

class WithLoginActionTest @Inject()(messagesControllerComponents: MessagesControllerComponents, accountTokens: AccountTokens)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def action(f: ⇒ String => Request[AnyContent] => Result) = {
    Action { request ⇒
      getUsername(request).map {
        case constants.Error.TOKEN_TIMEOUT => onTokenTimeout(request)
        case constants.Error.INVALID_TOKEN => onInvalidToken(request)
        case username => f(username)(request)
      }.getOrElse(onUnauthenticated(request))
    }
  }

  def getUsername(request: RequestHeader): Option[String] = {
    val username = request.session.get(constants.Security.USERNAME)
    if (username.isEmpty) None
    else if (!accountTokens.Service.verifySessionTokenTime(username)) Some(constants.Error.TOKEN_TIMEOUT)
    else if (!accountTokens.Service.verifySessionToken(username, request.session.get(constants.Security.TOKEN))) Some(constants.Error.INVALID_TOKEN)
    else if (accountTokens.Service.verifySessionToken(username, request.session.get(constants.Security.TOKEN))) username
    else None
  }

  def onUnauthenticated(implicit request: RequestHeader) = Results.NotFound(views.html.index(failure = Messages(constants.Error.NOT_LOGGED_IN)))

  def onTokenTimeout(implicit request: RequestHeader) = Results.GatewayTimeout(views.html.index(failure = Messages(constants.Error.TOKEN_TIMEOUT)))

  def onInvalidToken(implicit request: RequestHeader) = Results.NotFound(views.html.index(failure = Messages(constants.Error.INVALID_TOKEN)))


}