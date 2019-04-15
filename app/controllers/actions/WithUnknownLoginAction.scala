package controllers.actions

import constants.Security
import javax.inject.Inject
import models.master
import models.masterTransaction.AccountTokens
import play.api.mvc._
import play.api.mvc.{Security => ResultSecurity}

import scala.concurrent.{ExecutionContext, Future}

class WithUnknownLoginAction  @Inject()(defaultBodyParse: BodyParsers.Default, masterAccounts: master.Accounts, accountTokens: AccountTokens)(implicit executionContext: ExecutionContext) extends ActionBuilderImpl(defaultBodyParse)  {
 /*
  override def invokeBlock[T](request: Request[T], block: Request[T] => Future[Result]): Future[Result] = {
    if (accountTokens.Service.verifySession(request.session.get(Security.USERNAME), request.session.get(Security.TOKEN)) && masterAccounts.Service.getUserType(request.session.get(Security.USERNAME).getOrElse("")) == constants.User.UNKNOWN) {
      block(request)
    }
    else {
      Future.successful(Results.Forbidden(constants.Error.INCORRECT_LOG_IN))
    }
  }
  */
  def getUsername(request: RequestHeader): Option[String] = request.session.get("USERNAME")

  def onUnauthenticated(request: RequestHeader) = Results.Forbidden(constants.Error.INCORRECT_LOG_IN)

  def action(f: ⇒ String => Request[AnyContent] => Result) = {
    ResultSecurity.Authenticated(getUsername, onUnauthenticated) { username ⇒
      Action(request ⇒ {
        if (accountTokens.Service.verifySession(Some(username), request.session.get(Security.TOKEN)) && masterAccounts.Service.getUserType(username) == constants.User.UNKNOWN) {
          f(username)(request)
        }
        else{
          Results.Forbidden(constants.Error.INCORRECT_LOG_IN)
        }
      })
    }
  }

}