package controllers

import controllers.actions.{WithLoginAction, WithoutLoginAction, WithoutLoginActionAsync}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain
import models.master._
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import services.Startup

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndexController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                withLoginAction: WithLoginAction,
                                masterAccounts: Accounts,
                                blockchainAssets: blockchain.Assets,
                                blockchainSplits: blockchain.Splits,
                                blockchainIdentities: blockchain.Identities,
                                blockchainOrders: blockchain.Orders,
                                withUsernameToken: WithUsernameToken,
                                withoutLoginAction: WithoutLoginAction,
                                withoutLoginActionAsync: WithoutLoginActionAsync,
                                startup: Startup
                               )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_INDEX

  private val accountPrefix = configuration.get[String]("blockchain.prefix.account")

  private val validatorPrefix = configuration.get[String]("blockchain.prefix.validator")

  //  def index: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
  //    implicit request =>
  //      (loginState.userType match {
  //        case constants.User.USER => withUsernameToken.Ok(views.html.profile())
  //        case constants.User.WITHOUT_LOGIN =>
  //          val markUserTypeUser = masterAccounts.Service.markUserTypeUser(loginState.username)
  //          for {
  //            _ <- markUserTypeUser
  //            result <- withUsernameToken.Ok(views.html.profile())
  //          } yield result
  //        case _ => withUsernameToken.Ok(views.html.dashboard())
  //      }).recover {
  //        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
  //      }
  //  }

  def index: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.dashboard())
  }

  def search(query: String): Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    if (query.matches(s"${accountPrefix}[a-z0-9]{39}")) Future(Redirect(routes.ViewController.account(query)))
    else if (query.matches(s"${validatorPrefix}[a-z0-9]{39}")) Future(Redirect(routes.ViewController.validator(query)))
    else if (utilities.Validator.isHexAddress(query)) Future(Redirect(routes.ViewController.validator(query)))
    else if (query.matches("[A-F0-9]{64}")) Future(Redirect(routes.ViewController.transaction(query)))
    else if (query.matches("[0-9]{1,40}")) Future(Redirect(routes.ViewController.block(query.toInt)))
    else {
      val asset = blockchainAssets.Service.get(query)
      val splits = blockchainSplits.Service.getByOwnerOrOwnable(query)
      val identity = blockchainIdentities.Service.get(query)
      val order = blockchainOrders.Service.get(query)

      (for {
        asset <- asset
        splits <- splits
        identity <- identity
        order <- order
      } yield {
        if (asset.isEmpty && splits.isEmpty && identity.isEmpty && order.isEmpty) InternalServerError(views.html.dashboard(Seq(constants.Response.SEARCH_QUERY_NOT_FOUND)))
        else Ok(views.html.search(query, asset, identity, splits, order))
      }).recover {
        case _: BaseException => InternalServerError(views.html.dashboard(Seq(constants.Response.SEARCH_QUERY_NOT_FOUND)))
      }
    }
  }
}
