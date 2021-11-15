package controllers

import akka.actor.Props
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.pattern.ask
import controllers.actions._
import controllers.results.WithUsernameToken
import controllers.view.OtherApp
import dbActors.{AddActor, BlockchainActor, Master}
import exceptions.BaseException
import models.blockchain
import models.blockchain.{Balances, Maintainer, Meta}
import models.master._
import play.api.cache.Cached
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, EssentialAction, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import queries.blockchain.GetAccount
import services.Startup

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, ExecutionContext, Future}
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
                                balances: Balances
                               )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_INDEX

  private val cacheDuration = configuration.get[Int]("webApp.cacheDuration").milliseconds

  private implicit val otherApps: Seq[OtherApp] = configuration.get[Seq[Configuration]]("webApp.otherApps").map { otherApp =>
    OtherApp(url = otherApp.get[String]("url"), name = otherApp.get[String]("name"))
  }

  def index: EssentialAction = cached.apply(req => req.path, cacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        loginState match {
          case Some(loginState) =>
            implicit val loginStateImplicit: LoginState = loginState
            withUsernameToken.Ok(views.html.index())
          case None => Future(Ok(views.html.index()))
        }
    }
  }

  def search(query: String): EssentialAction = cached.apply(req => req.path, cacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>

        if (query == "") Future(Unauthorized(views.html.index(failures = Seq(constants.Response.EMPTY_QUERY))))
        else if (query.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)) Future(Redirect(routes.ComponentViewController.wallet(query)))
        else if (query.matches(constants.Blockchain.ValidatorPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex) || utilities.Validator.isHexAddress(query)) Future(Redirect(routes.ComponentViewController.validator(query)))
        else if (query.matches(constants.RegularExpression.TRANSACTION_HASH.regex)) Future(Redirect(routes.ComponentViewController.transaction(query)))
        else if (Try(query.toInt).isSuccess) Future(Redirect(routes.ComponentViewController.block(query.toInt)))
        else {
          val asset = blockchainAssets.Service.getAssetWithActor(query)
          val splits = blockchainSplits.Service.getSplitByOwnerOrOwnableWithActo(query)
          val identity = blockchainIdentities.Service.getIdentityWithActor(query)
          val order = blockchainOrders.Service.getOrderWithActor(query)
          val metaList = blockchainMetas.Service.getMetasWithActor(Seq(query))
          val classification = blockchainClassifications.Service.getClassificationWithActor(query)
          val maintainer = blockchainMaintainers.Service.getMaintainerWithActor(query)

          def searchResult(asset: Option[models.blockchain.Asset], splits: Seq[blockchain.Split], identity: Option[blockchain.Identity], order: Option[blockchain.Order], metaList: Seq[Meta], classification: Option[blockchain.Classification], maintainer: Option[Maintainer]) = {
            if (asset.isEmpty && splits.isEmpty && identity.isEmpty && order.isEmpty && metaList.isEmpty && classification.isEmpty && maintainer.isEmpty) Future(Unauthorized(views.html.index(failures = Seq(constants.Response.SEARCH_QUERY_NOT_FOUND))))
            else {
              loginState match {
                case Some(loginState) =>
                  implicit val loginStateImplicit: LoginState = loginState
                  if (asset.isEmpty && splits.isEmpty && identity.isEmpty && order.isEmpty && metaList.isEmpty && classification.isEmpty && maintainer.isEmpty) withUsernameToken.Ok(views.html.index(failures = Seq(constants.Response.SEARCH_QUERY_NOT_FOUND)))
                  else withUsernameToken.Ok(views.html.component.blockchain.search(asset, identity, splits, order, metaList, classification, maintainer))
                case None =>
                  Future(Ok(views.html.component.blockchain.search(asset, identity, splits, order, metaList, classification, maintainer)))
              }
            }
          }

          (for {
            asset <- asset
            splits <- splits
            identity <- identity
            order <- order
            metaList <- metaList
            classification <- classification
            maintainer <- maintainer
            result <- searchResult(asset, splits, identity, order, metaList, classification, maintainer)
          } yield result).recover {
            case _: BaseException => Unauthorized(views.html.index(failures = Seq(constants.Response.SEARCH_QUERY_NOT_FOUND)))
          }
        }
    }
  }
  startup.start()

  ClusterSharding(dbActors.Service.actorSystem).start(
    typeName = "blockchainCluster",
    entityProps = BlockchainActor.props(balances),
    settings = ClusterShardingSettings(dbActors.Service.actorSystem),
    extractEntityId = dbActors.ShardSettings.extractEntityId,
    extractShardId = dbActors.ShardSettings.extractShardId

  )
}
