package controllers

import controllers.actions._
import controllers.results.WithUsernameToken
import controllers.view.OtherApp
import exceptions.BaseException

import javax.inject.{Inject, Singleton}
import models.blockchain
import play.api.i18n.I18nSupport
import play.api.libs.json.{Json, Reads}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{ConfigLoader, Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ViewController @Inject()(
                                blockchainAccounts: blockchain.Accounts,
                                messagesControllerComponents: MessagesControllerComponents,
                                withLoginActionAsync: WithLoginActionAsync,
                                withUsernameToken: WithUsernameToken,
                                withoutLoginActionAsync: WithoutLoginActionAsync,
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

  def wallet(address: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      loginState match {
        case Some(loginState) => {
          implicit val loginStateImplicit: LoginState = loginState
          withUsernameToken.Ok(views.html.account())
        }
        case None => Future(Ok(views.html.wallet(address)))
      }
  }

  def blocks(): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      loginState match {
        case Some(loginState) => {
          implicit val loginStateImplicit: LoginState = loginState
          withUsernameToken.Ok(views.html.blocks())
        }
        case None => Future(Ok(views.html.blocks()))
      }

  }

  def block(height: Int): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      loginState match {
        case Some(loginState) => {
          implicit val loginStateImplicit: LoginState = loginState
          withUsernameToken.Ok(views.html.block(height))
        }
        case None => Future(Ok(views.html.block(height)))
      }
  }

  def transactions(): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      loginState match {
        case Some(loginState) => {
          implicit val loginStateImplicit: LoginState = loginState
          withUsernameToken.Ok(views.html.transactions())
        }
        case None => Future(Ok(views.html.transactions()))
      }
  }

  def transaction(txHash: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      loginState match {
        case Some(loginState) => {
          implicit val loginStateImplicit: LoginState = loginState
          withUsernameToken.Ok(views.html.transaction(txHash))
        }
        case None => Future(Ok(views.html.transaction(txHash)))
      }
  }

  def validators(): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      loginState match {
        case Some(loginState) => {
          implicit val loginStateImplicit: LoginState = loginState
          withUsernameToken.Ok(views.html.validators())
        }
        case None => Future(Ok(views.html.validators()))
      }
  }

  def validator(address: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      loginState match {
        case Some(loginState) => {
          implicit val loginStateImplicit: LoginState = loginState
          withUsernameToken.Ok(views.html.validator(address))
        }
        case None => Future(Ok(views.html.validator(address)))
      }
  }

  def proposal(id: Int): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      loginState match {
        case Some(loginState) => {
          implicit val loginStateImplicit: LoginState = loginState
          withUsernameToken.Ok(views.html.proposal(id))
        }
        case None => Future(Ok(views.html.proposal(id)))
      }
  }

  def proposals(): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      loginState match {
        case Some(loginState) => {
          implicit val loginStateImplicit: LoginState = loginState
          withUsernameToken.Ok(views.html.proposals())
        }
        case None => Future(Ok(views.html.proposals()))
      }
  }

  def dashboard: Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      loginState match {
        case Some(loginState) => {
          implicit val loginStateImplicit: LoginState = loginState
          withUsernameToken.Ok(views.html.dashboard())
        }
        case None => Future(Ok(views.html.dashboard()))
      }
  }

  def identity: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.identity())
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
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


}
