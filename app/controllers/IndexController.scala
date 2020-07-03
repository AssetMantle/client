package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain
import models.blockchain.{ACLAccount, ACLAccounts}
import models.master._
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import queries.GetAccount
import utilities.MicroLong

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndexController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, masterAccounts: Accounts, withUsernameToken: WithUsernameToken)(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_INDEX

  def index: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>

      (loginState.userType match {
        case constants.User.USER => withUsernameToken.Ok(views.html.profile())
        case constants.User.WITHOUT_LOGIN =>
          val markUserTypeUser = masterAccounts.Service.markUserTypeUser(loginState.username)
          for {
            _ <- markUserTypeUser
            result <- withUsernameToken.Ok(views.html.profile())
          } yield result
        case _ => withUsernameToken.Ok(views.html.dashboard())
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }
}
