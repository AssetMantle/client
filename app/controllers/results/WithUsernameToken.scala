package controllers.results

import constants.Security
import javax.inject.Inject
import models.masterTransaction.AccountTokens
import models.master
import play.api.http.Writeable
import play.api.mvc.{RequestHeader, Result, Results}
import scala.concurrent.ExecutionContext

class WithUsernameToken @Inject()(accountTokens: AccountTokens, masterAccounts:master.Accounts)(implicit executionContext: ExecutionContext) {
  def Ok[C](content: C, username: String)(implicit request: RequestHeader, writeable: Writeable[C]): Result = Results.Ok(content).withSession(request.session - Security.TOKEN + (Security.TOKEN -> accountTokens.Service.refreshSessionToken(username)) - Security.USERNAME + (Security.USERNAME -> username) - Security.USER_TYPE + (Security.USER_TYPE -> masterAccounts.Service.getUserType(username)))
}