package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain
import models.blockchain.ACLAccounts
import models.master.{Accounts, Organizations, Zones}
import play.api.{Configuration, Logger}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import queries.GetAccount
import utilities.{LoginState, PushNotification}

import scala.concurrent.ExecutionContext

@Singleton
class IndexController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, masterAccounts: Accounts, blockchainAclAccounts: ACLAccounts, blockchainZones: blockchain.Zones, blockchainOrganizations: blockchain.Organizations, blockchainAssets: blockchain.Assets, blockchainFiats: blockchain.Fiats, blockchainNegotiations: blockchain.Negotiations, masterOrganizations: Organizations, masterZones: Zones, blockchainAclHashes: blockchain.ACLHashes, blockchainOrders: blockchain.Orders, getAccount: GetAccount, blockchainAccounts: blockchain.Accounts, withUsernameToken: WithUsernameToken)(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  def index: Action[AnyContent] = withLoginAction.authenticated { username =>
    implicit request =>
    try {
      implicit val loginState:LoginState = LoginState(username)
      val userType = masterAccounts.Service.getUserType(username)
      val address = masterAccounts.Service.getAddress(username)
      userType match {
        case constants.User.GENESIS =>
          withUsernameToken.Ok(views.html.genesisHome(username = username, address = address, coins = blockchainAccounts.Service.getCoins(address)), username)
        case constants.User.ZONE =>
          withUsernameToken.Ok(views.html.zoneHome(username = username, address = address, coins = blockchainAccounts.Service.getCoins(address), zone = masterZones.Service.get(blockchainZones.Service.getID(address))), username)
        case constants.User.ORGANIZATION =>
          withUsernameToken.Ok(views.html.organizationHome(username = username, address = address, coins = blockchainAccounts.Service.getCoins(address), organization = masterOrganizations.Service.get(blockchainOrganizations.Service.getID(address))), username)
        case constants.User.TRADER =>
          val aclAccount = blockchainAclAccounts.Service.get(address)
          val fiatPegWallet = blockchainFiats.Service.getFiatPegWallet(address)
          val negotiations = blockchainNegotiations.Service.getNegotiationsForAddress(masterAccounts.Service.getAddress(username))
          withUsernameToken.Ok(views.html.traderHome(username = username, address = address, coins = blockchainAccounts.Service.getCoins(address), assetPegWallet = blockchainAssets.Service.getAssetPegWallet(address), fiatPegWallet = fiatPegWallet, totalFiat = fiatPegWallet.map(_.transactionAmount.toInt).sum, zone = masterZones.Service.get(aclAccount.zoneID), organization = masterOrganizations.Service.get(aclAccount.organizationID), orders = blockchainOrders.Service.getOrders(negotiations.map(_.id)), negotiations = negotiations, aclHash = blockchainAclHashes.Service.get(aclAccount.aclHash)), username)
        case constants.User.USER =>
          withUsernameToken.Ok(views.html.userHome(username = username, address = address, coins = blockchainAccounts.Service.getCoins(address)), username)
        case constants.User.UNKNOWN =>
          withUsernameToken.Ok(views.html.unknownHome(username = username, address = address), username)
        case constants.User.WITHOUT_LOGIN =>
          masterAccounts.Service.updateUserType(username, constants.User.UNKNOWN)
          withUsernameToken.Ok(views.html.unknownHome(username = username, address = address), username)
      }
    }
    catch {
      case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
    }
  }
}
