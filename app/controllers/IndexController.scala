package controllers

import constants.AppConfig._
import controllers.actions._
import controllers.results.WithUsernameToken
import models.blockchain
import models.common.ID._
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
                                blockchainAssets: blockchain.Assets,
                                blockchainSplits: blockchain.Splits,
                                blockchainMetas: blockchain.Metas,
                                blockchainIdentities: blockchain.Identities,
                                blockchainMaintainers: blockchain.Maintainers,
                                blockchainOrders: blockchain.Orders,
                                blockchainClassifications: blockchain.Classifications,
                                withUsernameToken: WithUsernameToken,
                                withoutLoginActionAsync: WithoutLoginActionAsync,
                                startup: Startup,
                                cached: Cached,
                               )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_INDEX

  def index: EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        loginState match {
          case Some(loginState) =>
            implicit val loginStateImplicit: LoginState = loginState
            withUsernameToken.Ok(views.html.assetMantle.account())
          case None => Future(Ok(views.html.index()))
        }
    }
  }

  def search(query: String): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>

        if (query == "") Future(Unauthorized(views.html.index(failures = Seq(constants.Response.EMPTY_QUERY))))
        else if (query.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)) Future(Redirect(routes.ComponentViewController.wallet(query)))
        else if (query.matches(constants.Blockchain.ValidatorPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex) || utilities.Validator.isHexAddress(query)) Future(Redirect(routes.ComponentViewController.validator(query)))
        else if (query.matches(constants.RegularExpression.TRANSACTION_HASH.regex)) Future(Redirect(routes.ComponentViewController.transaction(query)))
        else if (Try(query.toInt).isSuccess) Future(Redirect(routes.ComponentViewController.block(query.toInt)))
        else {
          val assetID = getAssetID(query)
          val identityID = getIdentityID(query)
          val orderID = getOrderID(query)
          val metaID = getMetaID(query)
          val classificationID = getClassificationID(query)
          val maintainerID = getMaintainerID(query)
          if (assetID.nonEmpty) Future(Redirect(routes.ViewController.asset(assetID.get.asString)))
          else if (identityID.nonEmpty) Future(Redirect(routes.ViewController.identity(identityID.get.asString)))
          else if (orderID.nonEmpty) Future(Redirect(routes.ViewController.order(orderID.get.asString)))
          else if (classificationID.nonEmpty) Future(Redirect(routes.ViewController.classification(classificationID.get.asString)))
          else if (metaID.nonEmpty) Future(Redirect(routes.ViewController.meta(metaID.get.asString)))
          else if (maintainerID.nonEmpty) Future(Redirect(routes.ViewController.maintainer(maintainerID.get.asString)))
          else Future(Unauthorized(views.html.index(failures = Seq(constants.Response.SEARCH_QUERY_NOT_FOUND))))
        }
    }
  }

  startup.start()
}
