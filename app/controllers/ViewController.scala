package controllers

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext}

@Singleton
class ViewController @Inject()(
                                messagesControllerComponents: MessagesControllerComponents,
                                withLoginAction: WithLoginAction,
                                withUsernameToken: WithUsernameToken
                              )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_VIEW

  def profile: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.profile())
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def account: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.account())
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def dashboard: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.dashboard())
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

}
