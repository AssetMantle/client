package controllers

import controllers.actions.{WithLoginAction, WithOrganizationLoginAction, WithTraderLoginAction, WithZoneLoginAction}
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

import scala.concurrent.ExecutionContext

@Singleton
class ComponentViewController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: Accounts,  masterAccountFiles: master.AccountFiles, blockchainAclAccounts: ACLAccounts, blockchainZones: blockchain.Zones, blockchainOrganizations: blockchain.Organizations, blockchainAssets: blockchain.Assets, blockchainFiats: blockchain.Fiats, blockchainNegotiations: blockchain.Negotiations, masterOrganizations: Organizations, masterZones: Zones, blockchainAclHashes: blockchain.ACLHashes, blockchainOrders: blockchain.Orders, getAccount: GetAccount, blockchainAccounts: blockchain.Accounts, withUsernameToken: WithUsernameToken, withLoginAction: WithLoginAction, withZoneLoginAction: WithZoneLoginAction, withOrganizationLoginAction: WithOrganizationLoginAction, withTraderLoginAction: WithTraderLoginAction)(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  def commonHome: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
    try {
      loginState.userType match {
        case constants.User.UNKNOWN =>
          Ok(views.html.component.master.commonHome(loginState.username, loginState.userType, loginState.address, profilePicture = masterAccountFiles.Service.getProfilePicture(loginState.username)))
        case _ =>
          Ok(views.html.component.master.commonHome(loginState.username, loginState.userType, loginState.address, blockchainAccounts.Service.getCoins(loginState.address),profilePicture = masterAccountFiles.Service.getProfilePicture(loginState.username)))
      }
    } catch {
      case baseException: BaseException => NoContent
    }
  }

  def zoneDetails: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
    try {
      loginState.userType match {
        case constants.User.ZONE =>
          Ok(views.html.component.master.zoneDetails(masterZones.Service.get(blockchainZones.Service.getID(loginState.address))))
        case constants.User.TRADER =>
          Ok(views.html.component.master.zoneDetails(masterZones.Service.get(blockchainAclAccounts.Service.get(loginState.address).zoneID)))
      }
    } catch {
      case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def organizationDetails: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
    try {
      loginState.userType match {
        case constants.User.ORGANIZATION =>
          Ok(views.html.component.master.organizationDetails(masterOrganizations.Service.get(blockchainOrganizations.Service.getID(loginState.address))))
        case constants.User.TRADER =>
          Ok(views.html.component.master.organizationDetails(masterOrganizations.Service.get(blockchainAclAccounts.Service.get(loginState.address).organizationID)))
      }
  } catch {
    case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
  }
  }

  def assetList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
    try {
      Ok(views.html.component.master.assetList(assetPegWallet = blockchainAssets.Service.getAssetPegWallet(loginState.address), aclHash = blockchainAclHashes.Service.get(blockchainAclAccounts.Service.get(loginState.address).aclHash)))
    } catch {
      case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def fiatList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState => implicit request =>
    try {
      Ok(views.html.component.master.fiatList(fiatPegWallet = blockchainFiats.Service.getFiatPegWallet(loginState.address), aclHash = blockchainAclHashes.Service.get(blockchainAclAccounts.Service.get(loginState.address).aclHash)))
    } catch {
      case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def negotiationList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState => implicit request =>
    try {
      val negotiations = blockchainNegotiations.Service.getNegotiationsForAddress(loginState.address)
      Ok(views.html.component.master.negotiationList(negotiations.filter(_.buyerAddress == loginState.address),negotiations.filter(_.sellerAddress == loginState.address), blockchainAssets.Service.getAssetPegWallet(loginState.address).map(_.pegHash)))
    } catch {
      case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def orderList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState => implicit request =>
    try {
      Ok(views.html.component.master.orderList(blockchainOrders.Service.getOrders(blockchainNegotiations.Service.getNegotiationsForAddress(loginState.address).map(_.id))))
    } catch {
      case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def availableAssetList: Action[AnyContent] = Action { implicit request =>
    try {
      Ok(views.html.component.master.availableAssetList(blockchainAssets.Service.getAllUnmoderated(blockchainOrders.Service.getAllOrderIds)))
    } catch {
      case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def availableAssetListWithLogin: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState => implicit request =>
    try {
      Ok(views.html.component.master.availableAssetListWithLogin(blockchainAssets.Service.getAllUnmoderated(blockchainOrders.Service.getAllOrderIds), blockchainAclHashes.Service.get(blockchainAclAccounts.Service.get(loginState.address).aclHash)))
    } catch {
      case baseException: BaseException => NoContent
    }
  }
}