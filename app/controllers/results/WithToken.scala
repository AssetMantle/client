package controllers.results

import constants.Security
import javax.inject.Inject
import models.master.Accounts
import play.api.http.Writeable
import play.api.mvc.{RequestHeader, Result, Results}

class WithToken @Inject()(accounts: Accounts) {
  def Ok[C](content: C)(implicit request: RequestHeader, writeable: Writeable[C]): Result = Results.Ok(content).withSession(request.session - Security.token + (Security.token -> accounts.Service.refreshToken(request.session.get(Security.username).getOrElse("ss"))))
}