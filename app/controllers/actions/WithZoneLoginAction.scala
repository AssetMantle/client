package controllers.actions

import constants.Security
import javax.inject.Inject
import models.master.Accounts
import models.masterTransaction.AccountTokens
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class WithZoneLoginAction  @Inject()(defaultBodyParse: BodyParsers.Default, masterAccounts: Accounts, accountTokens: AccountTokens)(implicit executionContext: ExecutionContext) extends ActionBuilderImpl(defaultBodyParse) {
  override def invokeBlock[T](request: Request[T], block: Request[T] => Future[Result]): Future[Result] = {
    if (accountTokens.Service.verifySession(request.session.get(Security.USERNAME), request.session.get(Security.TOKEN)) && masterAccounts.Service.getUserType(request.session.get(Security.USERNAME).getOrElse(constants.User.UNKNOWN)) == Option(constants.User.ZONE)) {
      block(request)
    }
    else {
      Future.successful(Results.Forbidden("You are NOT a Zone!!"))
    }
  }
}