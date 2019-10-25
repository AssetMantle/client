package controllers.results

import constants.Security
import controllers.actions.LoginState
import javax.inject.Inject
import models.masterTransaction
import play.api.http.Writeable
import play.api.mvc.{RequestHeader, Result, Results}

import scala.concurrent.ExecutionContext

class WithUsernameToken @Inject()(masterTransactionSessionTokens: masterTransaction.SessionTokens)(implicit executionContext: ExecutionContext) {

  def Ok[C](content: C)(implicit request: RequestHeader, writeable: Writeable[C], loginState: LoginState): Result = Results.Ok(content).withSession(request.session - Security.TOKEN + (Security.TOKEN -> masterTransactionSessionTokens.Service.refresh(loginState.username)) - Security.USERNAME + (Security.USERNAME -> loginState.username))

  def PartialContent[C](content: C)(implicit request: RequestHeader, writeable: Writeable[C], loginState: LoginState): Result = Results.PartialContent(content).withSession(request.session - Security.TOKEN + (Security.TOKEN -> masterTransactionSessionTokens.Service.refresh(loginState.username)) - Security.USERNAME + (Security.USERNAME -> loginState.username))
}