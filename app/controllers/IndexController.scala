package controllers

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import models.blockchain
import models.blockchain.{Maintainer, Meta}
import models.master._
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import queries.blockchain.GetAccount
import services.Startup

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

@Singleton
class IndexController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                withLoginActionAsync: WithLoginActionAsync,
                                masterAccounts: Accounts,
                                blockchainAssets: blockchain.Assets,
                                blockchainSplits: blockchain.Splits,
                                blockchainMetas: blockchain.Metas,
                                blockchainIdentities: blockchain.Identities,
                                blockchainMaintainers: blockchain.Maintainers,
                                blockchainOrders: blockchain.Orders,
                                blockchainClassifications: blockchain.Classifications,
                                withUsernameToken: WithUsernameToken,
                                withoutLoginAction: WithoutLoginAction,
                                withoutLoginActionAsync: WithoutLoginActionAsync,
                                startup: Startup,
                                getAccount: GetAccount
                               )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_INDEX

  def index: Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val a = "CqcBCqQBCiUvY29zbW9zLnN0YWtpbmcudjFiZXRhMS5Nc2dVbmRlbGVnYXRlEnsKMnBlcnNpc3RlbmNlMXhmZzI4Y3pqeHNmNzV4OXRoNGtlanJqdjNuNnQ3d2ZjdTZnanBlEjlwZXJzaXN0ZW5jZXZhbG9wZXIxeGZnMjhjemp4c2Y3NXg5dGg0a2Vqcmp2M242dDd3ZmM0N2cwZ3EaCgoFdXhwcnQSATESWApQCkYKHy9jb3Ntb3MuY3J5cHRvLnNlY3AyNTZrMS5QdWJLZXkSIwohAtPShcQkaoW8XM32RiViq0DulpOZ9IJVPVCxF073796oEgQKAggBGAQSBBDAmgwaQOqwKRjpptD5hLQDGN0mTs/ZMTeKS4vCi9mn4w2/p8Sqfy2PwrinLiG+kRI232tWNz53Pg1/cr+XGmiDBcme1uY="
      println(utilities.Hash.base64URLDecoder(a))
      loginState match {
        case Some(loginState) =>
          implicit val loginStateImplicit: LoginState = loginState
          withUsernameToken.Ok(views.html.index())
        case None => Future(Ok(views.html.index()))
      }
  }

  def search(query: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>

      if (query.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)) Future(Redirect(routes.ViewController.wallet(query)))
      else if (query.matches(constants.Blockchain.ValidatorPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex) || utilities.Validator.isHexAddress(query)) Future(Redirect(routes.ViewController.validator(query)))
      else if (query.matches(constants.RegularExpression.TRANSACTION_HASH.regex)) Future(Redirect(routes.ViewController.transaction(query)))
      else if (Try(query.toInt).isSuccess) Future(Redirect(routes.ViewController.block(query.toInt)))
      else {
        val asset = blockchainAssets.Service.get(query)
        val splits = blockchainSplits.Service.getByOwnerOrOwnable(query)
        val identity = blockchainIdentities.Service.get(query)
        val order = blockchainOrders.Service.get(query)
        val metaList = blockchainMetas.Service.get(Seq(query))
        val classification = blockchainClassifications.Service.get(query)
        val maintainer = blockchainMaintainers.Service.get(query)

        def searchResult(asset: Option[models.blockchain.Asset], splits: Seq[blockchain.Split], identity: Option[blockchain.Identity], order: Option[blockchain.Order], metaList: Seq[Meta], classification: Option[blockchain.Classification], maintainer: Option[Maintainer]) = {
          if (asset.isEmpty && splits.isEmpty && identity.isEmpty && order.isEmpty && metaList.isEmpty && classification.isEmpty && maintainer.isEmpty) Future(InternalServerError(views.html.dashboard(Seq(constants.Response.SEARCH_QUERY_NOT_FOUND))))
          else {
            loginState match {
              case Some(loginState) =>
                implicit val loginStateImplicit: LoginState = loginState
                if (asset.isEmpty && splits.isEmpty && identity.isEmpty && order.isEmpty && metaList.isEmpty && classification.isEmpty && maintainer.isEmpty) {
                  withUsernameToken.Ok(views.html.dashboard(Seq(constants.Response.SEARCH_QUERY_NOT_FOUND)))
                } else withUsernameToken.Ok(views.html.search(asset, identity, splits, order, metaList, classification, maintainer))
              case None =>
                if (asset.isEmpty && splits.isEmpty && identity.isEmpty && order.isEmpty && metaList.isEmpty && classification.isEmpty && maintainer.isEmpty) {
                  Future(Ok(views.html.dashboard(Seq(constants.Response.SEARCH_QUERY_NOT_FOUND))))
                } else Future(Ok(views.html.search(asset, identity, splits, order, metaList, classification, maintainer)))
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
          case _: BaseException => InternalServerError(views.html.dashboard(Seq(constants.Response.SEARCH_QUERY_NOT_FOUND)))
        }
      }
  }

  startup.start()
}
