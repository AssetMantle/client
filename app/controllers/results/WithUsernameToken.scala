package controllers.results

import constants.Security
import controllers.actions.LoginState
import javax.inject.Inject
import models.master
import models.masterTransaction.AccountTokens
import play.api.http.Writeable
import play.api.mvc.{RequestHeader, Result, Results}

import scala.concurrent.ExecutionContext

class WithUsernameToken @Inject()(accountTokens: AccountTokens, masterAccounts: master.Accounts)(implicit executionContext: ExecutionContext) {
  def Ok[C](content: C)(implicit request: RequestHeader, writeable: Writeable[C], loginState: LoginState): Result = Results.Ok(content).withSession(request.session - Security.TOKEN + (Security.TOKEN -> accountTokens.Service.refreshSessionToken(loginState.username)) - Security.USERNAME + (Security.USERNAME -> loginState.username))
}