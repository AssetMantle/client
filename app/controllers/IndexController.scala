package controllers

import akka.actor.CoordinatedShutdown
import constants.AppConfig._
import controllers.actions._
import models.blockchain
import play.api.{Configuration, Logger}
import play.api.cache.Cached
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, EssentialAction, MessagesControllerComponents}
import services.Startup

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
                                cached: Cached,
                                coordinatedShutdown: CoordinatedShutdown,
                               )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_INDEX

  def index: EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        println(constants.Blockchain.NubClassificationID.asString)
        Future(Ok(views.html.index()))
    }
  }

  def search(query: String): EssentialAction = cached.apply(req => req.path + "/" + query, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>

        val ownerId = IdentityID(HashID(commonUtilities.Secrets.base64URLDecode("S4W0p2S3eT1ruMcSfRz9tJTqRFkrUBrNCqeAK91zEpo=")))

        val a = Await.result(blockchainSplits.Service.getByOwnerID(ownerId), Duration.Inf)


        if (query == "") Future(Unauthorized(views.html.index(failures = Seq(constants.Response.EMPTY_QUERY))))
        else if (query.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)) Future(Redirect(routes.ComponentViewController.wallet(query)))
        else if (query.matches(constants.Blockchain.ValidatorPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex) || utilities.Validator.isHexAddress(query)) Future(Redirect(routes.ComponentViewController.validator(query)))
        else if (query.matches(constants.RegularExpression.TRANSACTION_HASH.regex)) Future(Redirect(routes.ComponentViewController.transaction(query)))
        else if (Try(query.toInt).isSuccess) Future(Redirect(routes.ComponentViewController.block(query.toInt)))
        else Future(Unauthorized(views.html.index(failures = Seq(constants.Response.SEARCH_QUERY_NOT_FOUND))))
    }
  }
  //  blockchainClassifications.Service.insertOrUpdate(Classification(id = constants.Blockchain.NubClassificationID, immutables = Array(), mutables = Array()))

  coordinatedShutdown.addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, "ThreadShutdown")(utilities.Scheduler.shutdownListener())
  utilities.Scheduler.setShutdownCancellable(startup.start())
}
