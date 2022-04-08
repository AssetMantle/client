package controllers.results

import constants.Security
import controllers.actions.LoginState
import models.masterTransaction
import play.api.http.Writeable
import play.api.mvc.{RequestHeader, Result, Results}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WithUsernameToken @Inject()(masterTransactionSessionTokens: masterTransaction.SessionTokens)(implicit executionContext: ExecutionContext) {

  def Ok[C](content: C)(implicit request: RequestHeader, writeable: Writeable[C], loginState: LoginState): Future[Result] = {
    val newToken = masterTransactionSessionTokens.Service.refresh(loginState.username)
    for {
      newToken <- newToken
    } yield Results.Ok(content).withSession(request.session
      - Security.TOKEN + (Security.TOKEN -> newToken)
      - Security.USERNAME + (Security.USERNAME -> loginState.username))
  }

  def PartialContent[C](content: C)(implicit request: RequestHeader, writeable: Writeable[C], loginState: LoginState): Future[Result] = {
    val newToken = masterTransactionSessionTokens.Service.refresh(loginState.username)
    for {
      newToken <- newToken
    } yield Results.PartialContent(content).withSession(request.session
      - Security.TOKEN + (Security.TOKEN -> newToken)
      - Security.USERNAME + (Security.USERNAME -> loginState.username))
  }
}