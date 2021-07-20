package controllers

import controllers.actions._
import controllers.results.WithUsernameToken
import controllers.view.OtherApp
import exceptions.BaseException

import javax.inject.{Inject, Singleton}
import play.api.cache.Cached
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, EssentialAction, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ViewController @Inject()(
                                messagesControllerComponents: MessagesControllerComponents,
                                withLoginActionAsync: WithLoginActionAsync,
                                withUsernameToken: WithUsernameToken,
                                withoutLoginAction: WithoutLoginAction,
                                cached: Cached
                              )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_VIEW

  private val cacheDuration = configuration.get[Int]("webApp.cacheDuration").seconds

  private implicit val otherApps: Seq[OtherApp] = configuration.get[Seq[Configuration]]("webApp.otherApps").map { otherApp =>
    OtherApp(url = otherApp.get[String]("url"), name = otherApp.get[String]("name"))
  }

  def profile: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.profile())
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def account: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.account())
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def identity: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.identity())
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def asset: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.asset())
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def order: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.order())
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def validators(): EssentialAction = cached.apply(req => req.path, cacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.validators(None))
    }
  }

  def validator(address: String): EssentialAction = cached.apply(req => req.path, cacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.validators(Option(address)))
    }
  }

  def blocks(): EssentialAction = cached.apply(req => req.path, cacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.blocks(None))
    }
  }

  def block(height: Int): EssentialAction = cached.apply(req => req.path, cacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.blocks(Option(height)))
    }
  }

  def transactions(): EssentialAction = cached.apply(req => req.path, cacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.transactions(None))
    }
  }

  def transaction(txHash: String): EssentialAction = cached.apply(req => req.path, cacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.transactions(Option(txHash)))
    }
  }

  def proposals(): EssentialAction = cached.apply(req => req.path, cacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.proposals(None))
    }
  }

  def proposal(proposalID: Int): EssentialAction = cached.apply(req => req.path, cacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.proposals(Option(proposalID)))
    }
  }

  def wallet(address: String): EssentialAction = cached.apply(req => req.path, cacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.wallet(address))
    }
  }
}
