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

import scala.concurrent.ExecutionContext

@Singleton
class ViewController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccountKYC: master.AccountKYCs, masterAccountFile: master.AccountFiles, masterZoneKYC: master.ZoneKYCs, masterOrganizationKYC: master.OrganizationKYCs, masterTraderKYC: master.TraderKYCs, withLoginAction: WithLoginAction, withTraderLoginAction: WithTraderLoginAction, withZoneLoginAction: WithZoneLoginAction, withOrganizationLoginAction: WithOrganizationLoginAction, withGenesisLoginAction: WithGenesisLoginAction, masterAccounts: master.Accounts, blockchainAclAccounts: ACLAccounts, blockchainZones: blockchain.Zones, blockchainOrganizations: blockchain.Organizations, blockchainAssets: blockchain.Assets, blockchainFiats: blockchain.Fiats, blockchainNegotiations: blockchain.Negotiations, masterOrganizations: master.Organizations, masterZones: master.Zones, blockchainAclHashes: blockchain.ACLHashes, blockchainOrders: blockchain.Orders, getAccount: GetAccount, blockchainAccounts: blockchain.Accounts, withUsernameToken: WithUsernameToken)(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  def market: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.market())
      }
      catch {
        case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def genesisRequest: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.genesisRequest())
      }
      catch {
        case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def genesisInformation: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.genesisInformation())
      }
      catch {
        case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneRequest: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.zoneRequest())
      }
      catch {
        case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneInformation: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.zoneInformation())
      }
      catch {
        case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationRequest: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.organizationRequest())
      }
      catch {
        case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationInformation: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.organizationInformation())
      }
      catch {
        case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def profile: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.profile())
      }
      catch {
        case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
      }
  }

}
