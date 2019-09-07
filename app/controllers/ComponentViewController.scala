package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Document
import models.blockchain.{ACLAccounts, Negotiation}
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
class ComponentViewController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccountKYC: master.AccountKYCs, masterAccountFile: master.AccountFiles, masterZoneKYC: master.ZoneKYCs, masterOrganizationKYC: master.OrganizationKYCs, masterTraderKYC: master.TraderKYCs, withZoneLoginAction: WithZoneLoginAction, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, masterAccounts: Accounts, masterAccountFiles: master.AccountFiles, blockchainAclAccounts: ACLAccounts, blockchainZones: blockchain.Zones, blockchainOrganizations: blockchain.Organizations, blockchainAssets: blockchain.Assets, blockchainFiats: blockchain.Fiats, blockchainNegotiations: blockchain.Negotiations, masterOrganizations: Organizations, masterZones: Zones, blockchainAclHashes: blockchain.ACLHashes, blockchainOrders: blockchain.Orders, getAccount: GetAccount, blockchainAccounts: blockchain.Accounts, withUsernameToken: WithUsernameToken)(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  def commonHome: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        loginState.userType match {
          case constants.User.UNKNOWN =>
            withUsernameToken.Ok(views.html.component.master.commonHome(profilePicture = masterAccountFiles.Service.getProfilePicture(loginState.username)))
          case _ =>
            withUsernameToken.Ok(views.html.component.master.commonHome(blockchainAccounts.Service.getCoins(loginState.address), masterAccountFiles.Service.getProfilePicture(loginState.username)))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def genesisDetails: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.genesisDetails(masterAccounts.Service.getAddress("main")))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneDetails: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        loginState.userType match {
          case constants.User.ZONE =>
            withUsernameToken.Ok(views.html.component.master.zoneDetails(masterZones.Service.get(blockchainZones.Service.getID(loginState.address))))
          case constants.User.ORGANIZATION =>
            withUsernameToken.Ok(views.html.component.master.zoneDetails(masterZones.Service.get(masterOrganizations.Service.getByAccountID(loginState.username).zoneID)))
          case constants.User.TRADER =>
            withUsernameToken.Ok(views.html.component.master.zoneDetails(masterZones.Service.get(blockchainAclAccounts.Service.get(loginState.address).zoneID)))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationDetails: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        loginState.userType match {
          case constants.User.ORGANIZATION =>
            withUsernameToken.Ok(views.html.component.master.organizationDetails(masterOrganizations.Service.get(blockchainOrganizations.Service.getID(loginState.address))))
          case constants.User.TRADER =>
            withUsernameToken.Ok(views.html.component.master.organizationDetails(masterOrganizations.Service.get(blockchainAclAccounts.Service.get(loginState.address).organizationID)))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def assetList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.assetList(blockchainAssets.Service.getAssetPegWallet(loginState.address)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def fiatList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.fiatList(blockchainFiats.Service.getFiatPegWallet(loginState.address)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def buyNegotiationList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.buyNegotiationList(blockchainNegotiations.Service.getNegotiationsForBuyerAddress(loginState.address), blockchainAssets.Service.getAssetPegHashes(loginState.address)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def sellNegotiationList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.sellNegotiationList(blockchainNegotiations.Service.getNegotiationsForSellerAddress(loginState.address), blockchainAssets.Service.getAssetPegHashes(loginState.address)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def orderList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val negotiations = blockchainNegotiations.Service.getNegotiationsForAddress(loginState.address)
        val orders = blockchainOrders.Service.getOrders(negotiations.map(_.id))
        val negotiationsOfOrders: Seq[Negotiation] = negotiations.filter(negotiation => orders.map(_.id) contains negotiation.id)
        val assets = blockchainAssets.Service.getByPegHashes(negotiationsOfOrders.map(_.assetPegHash))
        withUsernameToken.Ok(views.html.component.master.orderList(orders.filter(order => (for (negotiationsOfOrder <- negotiationsOfOrders; if negotiationsOfOrder.buyerAddress == loginState.address && !assets.find(asset => asset.pegHash == negotiationsOfOrder.assetPegHash).orNull.moderated) yield negotiationsOfOrder).map(_.id) contains order.id),
          orders.filter(order => (for (negotiationsOfOrder <- negotiationsOfOrders; if negotiationsOfOrder.sellerAddress == loginState.address && !assets.find(asset => asset.pegHash == negotiationsOfOrder.assetPegHash).orNull.moderated) yield negotiationsOfOrder).map(_.id) contains order.id),
          orders.filter(order => (for (negotiationsOfOrder <- negotiationsOfOrders; if assets.find(asset => asset.pegHash == negotiationsOfOrder.assetPegHash).orNull.moderated) yield negotiationsOfOrder).map(_.id) contains order.id)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def availableAssetList: Action[AnyContent] = Action { implicit request =>
    try {
      Ok(views.html.component.master.availableAssetList(blockchainAssets.Service.getAllPublic(blockchainOrders.Service.getAllOrderIds)))
    } catch {
      case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def availableAssetListWithLogin: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.availableAssetListWithLogin(blockchainAssets.Service.getAllPublic(blockchainOrders.Service.getAllOrderIds)))
      } catch {
        case _: BaseException => NoContent
      }
  }

  def accountComet: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Ok.chunked(blockchainAccounts.Service.accountCometSource(loginState.username) via Comet.json("parent.accountCometMessage")).as(ContentTypes.HTML)
  }

  def assetComet: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Ok.chunked(blockchainAssets.Service.assetCometSource(loginState.username) via Comet.json("parent.assetCometMessage")).as(ContentTypes.HTML)
  }

  def fiatComet: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Ok.chunked(blockchainFiats.Service.fiatCometSource(loginState.username) via Comet.json("parent.fiatCometMessage")).as(ContentTypes.HTML)
  }

  def negotiationComet: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Ok.chunked(blockchainNegotiations.Service.negotiationCometSource(loginState.username) via Comet.json("parent.negotiationCometMessage")).as(ContentTypes.HTML)
  }

  def orderComet: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Ok.chunked(blockchainOrders.Service.orderCometSource(loginState.username) via Comet.json("parent.orderCometMessage")).as(ContentTypes.HTML)
  }

  def profileDocuments(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val documents: Seq[Document[_]] = loginState.userType match {
          case constants.User.ZONE => masterZoneKYC.Service.getAllDocuments(loginState.username)
          case constants.User.ORGANIZATION => masterOrganizationKYC.Service.getAllDocuments(masterOrganizations.Service.getID(loginState.username))
          case constants.User.TRADER => masterTraderKYC.Service.getAllDocuments(loginState.username)
          case constants.User.USER => masterAccountKYC.Service.getAllDocuments(loginState.username)
          case _ => masterAccountFile.Service.getAllDocuments(loginState.username)
        }
        withUsernameToken.Ok(views.html.component.master.profileDocuments(documents))
      } catch {
        case _: BaseException => InternalServerError
      }
  }

  def profilePicture(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.profilePicture(masterAccountFile.Service.getProfilePicture(loginState.username)))
      } catch {
        case _: BaseException => InternalServerError(views.html.component.master.profilePicture())
      }
  }
}