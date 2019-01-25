package controllers.results

import constants.Security
import javax.inject.Inject
import models.master.Accounts
import play.api.http.Writeable
import play.api.mvc.{RequestHeader, Result, Results}

class WithUsernameToken @Inject()(accounts: Accounts) {
  def Ok[C](content: C, username: String)(implicit request: RequestHeader, writeable: Writeable[C]): Result = Results.Ok(content).withSession(request.session - Security.TOKEN + (Security.TOKEN -> accounts.Service.refreshToken(username)) - Security.USERNAME + (Security.USERNAME -> username))
}