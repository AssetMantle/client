package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain
import models.master
import models.blockchain.ACLAccounts
import models.master.{Accounts, Organizations, Zones}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import queries.GetAccount
import utilities.LoginState

import scala.concurrent.ExecutionContext

@Singleton
class ComponentViewController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, masterAccounts: Accounts,  masterAccountFiles: master.AccountFiles, blockchainAclAccounts: ACLAccounts, blockchainZones: blockchain.Zones, blockchainOrganizations: blockchain.Organizations, blockchainAssets: blockchain.Assets, blockchainFiats: blockchain.Fiats, blockchainNegotiations: blockchain.Negotiations, masterOrganizations: Organizations, masterZones: Zones, blockchainAclHashes: blockchain.ACLHashes, blockchainOrders: blockchain.Orders, getAccount: GetAccount, blockchainAccounts: blockchain.Accounts, withUsernameToken: WithUsernameToken)(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  def commonHome(username: String): Action[AnyContent] = Action { implicit request =>
    try {
      val account = masterAccounts.Service.getAccount(username)
      account.userType match {
        case constants.User.UNKNOWN =>
          Ok(views.html.component.master.commonHome(username, account.userType, account.accountAddress, profilePicture = masterAccountFiles.Service.getProfilePicture(username)))
        case _ =>
          Ok(views.html.component.master.commonHome(username, account.userType, account.accountAddress, blockchainAccounts.Service.getCoins(account.accountAddress),profilePicture = masterAccountFiles.Service.getProfilePicture(username)))
      }
    } catch {
      case baseException: BaseException => NoContent
    }
  }

  def zone(username: String): Action[AnyContent] = Action { implicit request =>
    try {
      val account = masterAccounts.Service.getAccount(username)
      account.userType match {
        case constants.User.ZONE =>
          Ok(views.html.component.master.zoneDetails(masterZones.Service.get(blockchainZones.Service.getID(account.accountAddress))))
        case constants.User.TRADER =>
          Ok(views.html.component.master.zoneDetails(masterZones.Service.get(blockchainAclAccounts.Service.get(account.accountAddress).zoneID)))
      }
    } catch {
      case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def organization(username: String): Action[AnyContent] = Action { implicit request =>
    try {
      val account = masterAccounts.Service.getAccount(username)
      account.userType match {
        case constants.User.ORGANIZATION =>
          Ok(views.html.component.master.organizationDetails(masterOrganizations.Service.get(blockchainOrganizations.Service.getID(account.accountAddress))))
        case constants.User.TRADER =>
          Ok(views.html.component.master.organizationDetails(masterOrganizations.Service.get(blockchainAclAccounts.Service.get(account.accountAddress).organizationID)))
      }
    } catch {
      case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def assetPegWallet(username: String): Action[AnyContent] = Action { implicit request =>
    try {
      val address = masterAccounts.Service.getAddress(username)
      Ok(views.html.component.master.assetList(assetPegWallet = blockchainAssets.Service.getAssetPegWallet(address), aclHash = blockchainAclHashes.Service.get(blockchainAclAccounts.Service.get(address).aclHash)))
    } catch {
      case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def fiatPegWallet(username: String): Action[AnyContent] = Action { implicit request =>
    try {
      val address = masterAccounts.Service.getAddress(username)
      Ok(views.html.component.master.fiatList(fiatPegWallet = blockchainFiats.Service.getFiatPegWallet(address), aclHash = blockchainAclHashes.Service.get(blockchainAclAccounts.Service.get(address).aclHash)))
    } catch {
      case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def negotiation(username: String): Action[AnyContent] = Action { implicit request =>
    try {
      Ok(views.html.component.master.negotiationList(blockchainNegotiations.Service.getNegotiationsForAddress(masterAccounts.Service.getAddress(username))))
    } catch {
      case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def order(username: String): Action[AnyContent] = Action { implicit request =>
    try {
      Ok(views.html.component.master.orderList(blockchainOrders.Service.getOrders(blockchainNegotiations.Service.getNegotiationsForAddress(masterAccounts.Service.getAddress(username)).map(_.id))))
    } catch {
      case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def availableAssets: Action[AnyContent] = Action { implicit request =>
    try {
      Ok(views.html.component.master.availableAssetList(blockchainAssets.Service.getAllUnmoderated(blockchainOrders.Service.getAllOrderIds)))
    } catch {
      case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
    }
  }

}