package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.ACLAccounts
import models.{blockchain, master}
import models.master.{Accounts, Organizations, Zones}
import models.{blockchain, master}
import play.api.http.ContentTypes
import play.api.i18n.I18nSupport
import play.api.libs.Comet
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import queries.GetAccount

import scala.concurrent.ExecutionContext

@Singleton
class ComponentViewController @Inject()(messagesControllerComponents: MessagesControllerComponents, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, masterAccounts: Accounts,  masterAccountFiles: master.AccountFiles, blockchainAclAccounts: ACLAccounts, blockchainZones: blockchain.Zones, blockchainOrganizations: blockchain.Organizations, blockchainAssets: blockchain.Assets, blockchainFiats: blockchain.Fiats, blockchainNegotiations: blockchain.Negotiations, masterOrganizations: Organizations, masterZones: Zones, blockchainAclHashes: blockchain.ACLHashes, blockchainOrders: blockchain.Orders, getAccount: GetAccount, blockchainAccounts: blockchain.Accounts, withUsernameToken: WithUsernameToken)(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

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

  def zoneDetails(username: String): Action[AnyContent] = Action { implicit request =>
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

  def organizationDetails(username: String): Action[AnyContent] = Action { implicit request =>
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

  def assetList(username: String): Action[AnyContent] = Action { implicit request =>
    try {
      val address = masterAccounts.Service.getAddress(username)
      Ok(views.html.component.master.assetList(assetPegWallet = blockchainAssets.Service.getAssetPegWallet(address), aclHash = blockchainAclHashes.Service.get(blockchainAclAccounts.Service.get(address).aclHash)))
    } catch {
      case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def fiatList(username: String): Action[AnyContent] = Action { implicit request =>
    try {
      val address = masterAccounts.Service.getAddress(username)
      Ok(views.html.component.master.fiatList(fiatPegWallet = blockchainFiats.Service.getFiatPegWallet(address), aclHash = blockchainAclHashes.Service.get(blockchainAclAccounts.Service.get(address).aclHash)))
    } catch {
      case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def negotiationList(username: String): Action[AnyContent] = Action { implicit request =>
    try {
      val address = masterAccounts.Service.getAddress(username)
      val negotiations = blockchainNegotiations.Service.getNegotiationsForAddress(address)
      Ok(views.html.component.master.negotiationList(negotiations.filter(_.buyerAddress == address),negotiations.filter(_.sellerAddress == address )))
    } catch {
      case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def orderList(username: String): Action[AnyContent] = Action { implicit request =>
    try {
      Ok(views.html.component.master.orderList(blockchainOrders.Service.getOrders(blockchainNegotiations.Service.getNegotiationsForAddress(masterAccounts.Service.getAddress(username)).map(_.id))))
    } catch {
      case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def availableAssetList: Action[AnyContent] = Action { implicit request =>
    try {
      Ok(views.html.component.master.availableAssetList(blockchainAssets.Service.getAllModerated(blockchainOrders.Service.getAllOrderIds)))
    } catch {
      case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def availableAssetListWithLogin(username:String): Action[AnyContent] = Action { implicit request =>
    try {
      Ok(views.html.component.master.availableAssetListWithLogin(blockchainAssets.Service.getAllModerated(blockchainOrders.Service.getAllOrderIds), blockchainAclHashes.Service.get(blockchainAclAccounts.Service.get(masterAccounts.Service.getAddress(username)).aclHash)))
    } catch {
      case baseException: BaseException => NoContent
    }
  }

  def accountComet: Action[AnyContent] = withLoginAction.authenticated { username =>
  implicit request =>
    Ok.chunked(blockchainAccounts.Service.accountCometSource(username) via Comet.json("parent.accountCometMessage")).as(ContentTypes.HTML)

  }

  def assetComet: Action[AnyContent] = withTraderLoginAction.authenticated { username =>
    implicit request =>
      Ok.chunked(blockchainAssets.Service.assetCometSource(username) via Comet.json("parent.assetCometMessage")).as(ContentTypes.HTML)
  }

  def fiatComet: Action[AnyContent] = withTraderLoginAction.authenticated { username =>
    implicit request =>
      Ok.chunked(blockchainFiats.Service.fiatCometSource(username) via Comet.json("parent.fiatCometMessage")).as(ContentTypes.HTML)
  }

  def negotiationComet: Action[AnyContent] = withTraderLoginAction.authenticated { username =>
    implicit request =>
      Ok.chunked(blockchainNegotiations.Service.negotiationCometSource(username) via Comet.json("parent.negotiationCometMessage")).as(ContentTypes.HTML)
  }

  def orderComet: Action[AnyContent] = withTraderLoginAction.authenticated { username =>
    implicit request =>
      Ok.chunked(blockchainOrders.Service.orderCometSource(username) via Comet.json("parent.orderCometMessage")).as(ContentTypes.HTML)
  }

}