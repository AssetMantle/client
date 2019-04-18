package controllers.actions

import constants.Security
import javax.inject.Inject
import models.masterTransaction.AccountTokens
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class WithLoginAction @Inject()(defaultBodyParse: BodyParsers.Default, accountTokens: AccountTokens)(implicit executionContext: ExecutionContext) extends ActionBuilderImpl(defaultBodyParse)  {
  override def invokeBlock[T](request: Request[T], block: Request[T] => Future[Result]): Future[Result] = {
    if (accountTokens.Service.verifySessionToken(request.session.get(Security.USERNAME), request.session.get(Security.TOKEN))) {
      block(request)
    }
    else {
      Future.successful(Results.Forbidden(constants.Error.NOT_LOGGED_IN))
    }
  }
}
