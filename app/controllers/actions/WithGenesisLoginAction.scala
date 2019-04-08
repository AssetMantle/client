package controllers.actions

import constants.Security
import javax.inject.Inject
import models.master
import models.masterTransaction.AccountTokens
import play.api.mvc._
import play.api.mvc.Result

import scala.concurrent.{ExecutionContext, Future}

class WithGenesisLoginAction  @Inject()(defaultBodyParse: BodyParsers.Default, masterAccounts: master.Accounts, accountTokens: AccountTokens)(implicit executionContext: ExecutionContext, configuration: play.api.Configuration) extends ActionBuilderImpl(defaultBodyParse) {
  override def invokeBlock[T](request: Request[T], block: Request[T] => Future[Result]): Future[Result] = {
    if (accountTokens.Service.verifySession(request.session.get(Security.USERNAME), request.session.get(Security.TOKEN)) && masterAccounts.Service.getUserType(request.session.get(Security.USERNAME).getOrElse("")) == constants.User.GENESIS) {
      block(request)
    }
    else {
      Future.successful(Results.Forbidden(constants.Error.INCORRECT_LOG_IN))
    }
  }
}