package controllers

import akka.actor.CoordinatedShutdown
import constants.AppConfig._
import controllers.actions._
import models.campaign._
import play.api.cache.Cached
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, EssentialAction, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import services.Startup

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class IndexController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                withoutLoginActionAsync: WithoutLoginActionAsync,
                                startup: Startup,
                                claimNames: ClaimNames,
                                cached: Cached,
                                coordinatedShutdown: CoordinatedShutdown,
                               )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_INDEX

  def index: EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        Future(Ok(views.html.index()))
    }
  }

  def search(query: String): EssentialAction = cached.apply(req => req.path + "/" + query, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        if (query == "") Future(Unauthorized(views.html.index(failures = Seq(constants.Response.EMPTY_QUERY))))
        else if (query.matches(constants.Blockchain.AccountRegexString)) Future(Redirect(routes.ComponentViewController.wallet(query)))
        else if (query.matches(constants.Blockchain.ValidatorRegexString) || utilities.Validator.isHexAddress(query)) Future(Redirect(routes.ComponentViewController.validator(query)))
        else if (query.matches(constants.RegularExpression.TRANSACTION_HASH.regex)) Future(Redirect(routes.ComponentViewController.transaction(query)))
        else if (Try(query.toInt).isSuccess) Future(Redirect(routes.ComponentViewController.block(query.toInt)))
        else Future(Redirect(routes.ComponentViewController.document(query)))
    }
  }

  utilities.Scheduler.startSchedulers(
    startup.explorerScheduler,
    claimNames.Utility.scheduler
  )

  coordinatedShutdown.addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, "ThreadShutdown")(utilities.Scheduler.shutdownListener())
}
