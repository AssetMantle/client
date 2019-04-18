package controllers.actions

import javax.inject.Inject
import models.master
import models.masterTransaction.AccountTokens
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._

import scala.concurrent.ExecutionContext

class WithUnknownLoginActionTest @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, accountTokens: AccountTokens)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val timeout = configuration.get[Long]("sessionToken.timeout")

  def action(f: ⇒ String => Request[AnyContent] => Result) = {
    Action { request ⇒
      getUsername(request).map {
        case constants.Error.TOKEN_TIMEOUT => onTokenTimeout(request)
        case constants.Error.INVALID_TOKEN => onInvalidToken(request)
        case constants.Error.UNAUTHORIZED => onUnauthorized(request)
        case username => f(username)(request)
      }.getOrElse(onUnauthenticated(request))
    }
  }

  def getUsername(request: RequestHeader): Option[String] = {
    val username = request.session.get(constants.Security.USERNAME)
    val sessionToken = request.session.get(constants.Security.USERNAME)
    if (username.isEmpty) None
    else if (!accountTokens.Service.verifySessionTokenTime(username)) Some(constants.Error.TOKEN_TIMEOUT)
    else if (!accountTokens.Service.verifySessionToken(username, sessionToken)) Some(constants.Error.INVALID_TOKEN)
    else if (accountTokens.Service.verifySessionToken(username, sessionToken) && masterAccounts.Service.getUserType(username.getOrElse("")) != constants.User.UNKNOWN) Some(constants.Error.UNAUTHORIZED)
    else if (accountTokens.Service.verifySessionToken(username, sessionToken) && masterAccounts.Service.getUserType(username.getOrElse("")) == constants.User.UNKNOWN) username
    else None
  }

  def onUnauthorized(implicit request: RequestHeader) = Results.Unauthorized(views.html.index(failure = Messages(constants.Error.UNAUTHORIZED)))

  def onUnauthenticated(implicit request: RequestHeader) = Results.NotFound(views.html.index(failure = Messages(constants.Error.NOT_LOGGED_IN)))

  def onTokenTimeout(implicit request: RequestHeader) = Results.GatewayTimeout(views.html.index(failure = Messages(constants.Error.TOKEN_TIMEOUT)))

  def onInvalidToken(implicit request: RequestHeader) = Results.NotFound(views.html.index(failure = Messages(constants.Error.INVALID_TOKEN)))

}
