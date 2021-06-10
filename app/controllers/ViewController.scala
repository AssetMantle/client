package controllers

import controllers.actions._
import controllers.results.WithUsernameToken
import controllers.view.OtherApp
import exceptions.BaseException

import javax.inject.{Inject, Singleton}
import models.blockchain
import play.api.i18n.I18nSupport
import play.api.libs.json.{Json, Reads}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, PathBindable}
import play.api.{ConfigLoader, Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ViewController @Inject()(
                                blockchainAccounts: blockchain.Accounts,
                                messagesControllerComponents: MessagesControllerComponents,
                                withLoginActionAsync: WithLoginActionAsync,
                                withUsernameToken: WithUsernameToken,
                                withoutLoginActionAsync: WithoutLoginActionAsync,
                                withoutLoginAction: WithoutLoginAction
                              )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_VIEW

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

  def validators(): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.validators(None))
  }

  def validator(address: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.validators(Option(address)))
  }

  def blocks(): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.blocks(None))
  }

  def block(height: Int): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.blocks(Option(height)))
  }

  def transactions(): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.transactions(None))
  }

  def transaction(txHash: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.transactions(Option(txHash)))
  }

  def proposals(): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.proposals(None))
  }

  def proposal(proposalID: Int): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.proposals(Option(proposalID)))
  }

  def wallet(address: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.wallet(address))
  }

}
