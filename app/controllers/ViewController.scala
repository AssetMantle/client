package controllers

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.ACLAccounts
import models.{blockchain, master}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import queries.GetAccount

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ViewController @Inject()(
                                messagesControllerComponents: MessagesControllerComponents,
                                withLoginAction: WithLoginAction,
                                withTraderLoginAction: WithTraderLoginAction,
                                withZoneLoginAction: WithZoneLoginAction,
                                withOrganizationLoginAction: WithOrganizationLoginAction,
                                withGenesisLoginAction: WithGenesisLoginAction,
                                withUsernameToken: WithUsernameToken
                              )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_VIEW

  def genesisRequest: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future {
        Ok(views.html.genesisRequest())
      }.recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneRequest: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future {
        Ok(views.html.zoneRequest())
      }.recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def profile: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.profile())
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def account: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.account())
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def dashboard: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.dashboard())
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def trades: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.trades())
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def transactions: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.transactionsView())
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def tradeRoom(id: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.tradeRoom(id))
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def tradeRoomCompleted(id: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.tradeRoomCompleted(id))
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }
}
