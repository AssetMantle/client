package controllers.results

import constants.Security
import javax.inject.Inject
import models.masterTransaction.AccountTokens
import play.api.http.Writeable
import play.api.mvc.{RequestHeader, Result, Results}

class WithToken @Inject()(accountTokens: AccountTokens) {
  def Ok[C](content: C)(implicit request: RequestHeader, writeable: Writeable[C]): Result = Results.Ok(content).withSession(request.session - Security.TOKEN + (Security.USERNAME -> accountTokens.Service.refreshSessionToken(request.session.get(Security.USERNAME).getOrElse("ss"))))
}