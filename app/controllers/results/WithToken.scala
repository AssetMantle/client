package controllers.results

import constants.Security
import javax.inject.Inject
import models.masterTransaction
import play.api.http.Writeable
import play.api.mvc.{RequestHeader, Result, Results}

class WithToken @Inject()(masterTransactionSessionTokens: masterTransaction.SessionTokens) {
  def Ok[C](content: C)(implicit request: RequestHeader, writeable: Writeable[C]): Result = Results.Ok(content).withSession(request.session - Security.TOKEN + (Security.USERNAME -> masterTransactionSessionTokens.Service.insertOrUpdate(request.session.get(Security.USERNAME).getOrElse(""))))
}