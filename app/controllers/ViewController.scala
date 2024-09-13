package controllers

import constants.AppConfig._
import controllers.actions._
import play.api.{Configuration, Logger}
import play.api.cache.Cached
import play.api.i18n.I18nSupport
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ViewController @Inject()(
                                messagesControllerComponents: MessagesControllerComponents,
                                withoutLoginAction: WithoutLoginAction,
                                withoutLoginActionAsync: WithoutLoginActionAsync,
                                cached: Cached
                              )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_VIEW

//  def validators(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
//    withoutLoginAction { implicit loginState =>
//      implicit request =>
//        Ok(views.html.explorer.validators(None))
//    }
//  }

//  def validator(address: String): EssentialAction = cached.apply(req => req.path + "/" + address, constants.AppConfig.CacheDuration) {
//    withoutLoginAction { implicit loginState =>
//      implicit request =>
//        Ok(views.html.explorer.validators(Option(address)))
//    }
//  }

  def blocks(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.explorer.blocks(None))
    }
  }

  def block(height: Int): EssentialAction = cached.apply(req => req.path + "/" + height.toString, constants.AppConfig.CacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.explorer.blocks(Option(height)))
    }
  }

//  def parameters: EssentialAction = cached.apply(req => req.path, 3600) {
//    withoutLoginAction { implicit loginState =>
//      implicit request =>
//        Ok(views.html.explorer.parameters())
//    }
//  }

  def transactions(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.explorer.transactions(None))
    }
  }

  def transaction(txHash: String): EssentialAction = cached.apply(req => req.path + "/" + txHash, constants.AppConfig.CacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.explorer.transactions(Option(txHash)))
    }
  }

//  def proposals(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
//    withoutLoginAction { implicit loginState =>
//      implicit request =>
//        Ok(views.html.explorer.proposals(None))
//    }
//  }
//
//  def proposal(proposalID: Int): EssentialAction = cached.apply(req => req.path + "/" + proposalID.toString, constants.AppConfig.CacheDuration) {
//    withoutLoginAction { implicit loginState =>
//      implicit request =>
//        Ok(views.html.explorer.proposals(Option(proposalID)))
//    }
//  }
//
//  def wallet(address: String): EssentialAction = cached.apply(req => req.path + "/" + address, constants.AppConfig.CacheDuration) {
//    withoutLoginAction { implicit loginState =>
//      implicit request =>
//        Ok(views.html.explorer.wallet(address))
//    }
//  }

  def document(id: String): EssentialAction = cached.apply(req => req.path + "/" + id, constants.AppConfig.CacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.explorer.document(id))
    }
  }
}
