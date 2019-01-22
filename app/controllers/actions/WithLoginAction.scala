package controllers.actions

import constants.Security
import javax.inject.Inject
import models.master.Accounts
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class WithLoginAction @Inject()(defaultBodyParse: BodyParsers.Default, accounts: Accounts)(implicit executionContext: ExecutionContext) extends ActionBuilderImpl(defaultBodyParse) {
  override def invokeBlock[T](request: Request[T], block: Request[T] => Future[Result]): Future[Result] = {
    if (accounts.Service.verifySession(request.session.get(Security.username), token = request.session.get(Security.token))) {
      block(request)
    }
    else {
      Future.successful(Results.Forbidden("Dude, you’re not logged in."))
    }
  }
}