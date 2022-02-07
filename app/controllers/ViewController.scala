package controllers

import constants.AppConfig._
import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import play.api.cache.Cached
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

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

  def account: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.assetMantle.account())
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def profile: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.assetMantle.profile())
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def identity: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.assetMantle.identity())
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def asset: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.assetMantle.asset())
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def order: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.assetMantle.order())
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def validators(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.explorer.validators(None))
    }
  }

  def validator(address: String): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.explorer.validators(Option(address)))
    }
  }

  def blocks(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.explorer.blocks(None))
    }
  }

  def block(height: Int): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.explorer.blocks(Option(height)))
    }
  }

  def transactions(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.explorer.transactions(None))
    }
  }

  def transaction(txHash: String): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.explorer.transactions(Option(txHash)))
    }
  }

  def proposals(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.explorer.proposals(None))
    }
  }

  def proposal(proposalID: Int): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.explorer.proposals(Option(proposalID)))
    }
  }

  def wallet(address: String): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.explorer.wallet(address))
    }
  }
}
