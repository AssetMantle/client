package controllers

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class ViewController @Inject()(
                                blockchainAccountBalances: blockchain.AccountBalances,
                                messagesControllerComponents: MessagesControllerComponents,
                                withLoginAction: WithLoginAction,
                                withUsernameToken: WithUsernameToken,
                                withoutLoginActionAsync: WithoutLoginActionAsync,
                                withoutLoginAction: WithoutLoginAction,
                              )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_VIEW

  def profile: Action[AnyContent] = withLoginAction.authenticated {
    implicit loginState =>
      implicit request =>
        (for {
          result <- withUsernameToken.Ok(views.html.profile())
        } yield result).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
  }

  def account(address: String): Action[AnyContent] = withoutLoginActionAsync {
    implicit request =>
      val exists = blockchainAccountBalances.Service.checkExists(address)
      (for {
        exists <- exists
      } yield if (exists) Ok(views.html.account(address)) else throw new BaseException(constants.Response.WALLET_NOT_FOUND)
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
      }

  }

  def blocks(): Action[AnyContent] = withoutLoginAction {
    implicit request =>
      Ok(views.html.blocks())
  }

  def block(height: Int): Action[AnyContent] = withoutLoginAction {
    implicit request =>
      Ok(views.html.block(height))
  }

  def transactions(): Action[AnyContent] = withoutLoginAction {
    implicit request =>
      Ok(views.html.transactions())
  }

  def transaction(txHash: String): Action[AnyContent] = withoutLoginAction {
    implicit request =>
      Ok(views.html.transaction(txHash))
  }

  def validators(): Action[AnyContent] = withoutLoginAction {
    implicit request =>
      Ok(views.html.validators())
  }

  def validator(address: String): Action[AnyContent] = withoutLoginAction {
    implicit request =>
      Ok(views.html.validator(address))
  }

  def dashboard: Action[AnyContent] = withoutLoginAction {
    implicit request =>
      Ok(views.html.dashboard())
  }

  def asset(id: String): Action[AnyContent] = withoutLoginAction {
    implicit request =>
      Ok(views.html.asset(id))
  }

  def identity(id: String): Action[AnyContent] = withoutLoginAction {
    implicit request =>
      Ok(views.html.identity(id))
  }

  def split(id: String): Action[AnyContent] = withoutLoginAction {
    implicit request =>
      Ok(views.html.split(id))
  }

  def order(id: String): Action[AnyContent] = withoutLoginAction {
    implicit request =>
      Ok(views.html.order(id))
  }


}
