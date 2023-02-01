package controllers

import akka.actor.CoordinatedShutdown
import constants.AppConfig._
import controllers.actions._
import models.blockchain
import models.blockchain.Classification
import play.api.cache.Cached
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, EssentialAction, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import schema.list.PropertyList
import schema.qualified.{Immutables, Mutables}
import services.Startup
import utilities.Bech32.from5Bit

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

@Singleton
class IndexController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                withoutLoginActionAsync: WithoutLoginActionAsync,
                                startup: Startup,
                                blockchainSplits: blockchain.Splits,
                                blockchainClassifications: blockchain.Classifications,
                                blockchainIdentities: blockchain.Identities,
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
        else if (query.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)) Future(Redirect(routes.ComponentViewController.wallet(query)))
        else if (query.matches(constants.Blockchain.ValidatorPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex) || utilities.Validator.isHexAddress(query)) Future(Redirect(routes.ComponentViewController.validator(query)))
        else if (query.matches(constants.RegularExpression.TRANSACTION_HASH.regex)) Future(Redirect(routes.ComponentViewController.transaction(query)))
        else if (Try(query.toInt).isSuccess) Future(Redirect(routes.ComponentViewController.block(query.toInt)))
        else Future(Unauthorized(views.html.index(failures = Seq(constants.Response.SEARCH_QUERY_NOT_FOUND))))
    }
  }

  blockchainClassifications.Service.insertOrUpdate(Classification(
    id = constants.Blockchain.NubClassificationID.getBytes,
    immutables = Immutables(PropertyList(Seq(constants.Blockchain.NubProperty))).getProtoBytes,
    mutables = Mutables(PropertyList(Seq(constants.Blockchain.AuthenticationProperty))).getProtoBytes))

  coordinatedShutdown.addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, "ThreadShutdown")(utilities.Scheduler.shutdownListener())
  utilities.Scheduler.setShutdownCancellable(startup.start())
}
