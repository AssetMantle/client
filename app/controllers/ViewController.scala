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
                                masterAccountKYC: master.AccountKYCs,
                                masterAccountFile: master.AccountFiles,
                                masterZoneKYC: master.ZoneKYCs,
                                masterOrganizationKYC: master.OrganizationKYCs,
                                masterTraderKYC: master.TraderKYCs,
                                withLoginAction: WithLoginAction,
                                withTraderLoginAction: WithTraderLoginAction,
                                withZoneLoginAction: WithZoneLoginAction,
                                withOrganizationLoginAction: WithOrganizationLoginAction,
                                withGenesisLoginAction: WithGenesisLoginAction,
                                masterAccounts: master.Accounts,
                                blockchainAclAccounts: ACLAccounts,
                                blockchainZones: blockchain.Zones,
                                blockchainOrganizations: blockchain.Organizations,
                                blockchainAssets: blockchain.Assets,
                                blockchainFiats: blockchain.Fiats,
                                blockchainNegotiations: blockchain.Negotiations,
                                masterOrganizations: master.Organizations,
                                masterZones: master.Zones,
                                blockchainAclHashes: blockchain.ACLHashes,
                                blockchainOrders: blockchain.Orders,
                                getAccount: GetAccount,
                                blockchainAccounts: blockchain.Accounts,
                                withUsernameToken: WithUsernameToken
                              )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  def market: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(
        Ok(views.html.market())
      ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

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

  def organizationRequest: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future {
        Ok(views.html.organizationRequest())
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
        result <- withUsernameToken.Ok(views.html.transactions())
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def tradeRoom(id: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      (for {
        result <- withUsernameToken.Ok(views.html.tradeRoom(id))
      } yield {
        result.withHeaders("Access-Control-Allow-Origin" ->"https://account.docusign.com/")
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }
}
