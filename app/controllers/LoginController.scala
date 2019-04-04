package controllers

import controllers.results.WithUsernameToken
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchain
import models.blockchain.ACLAccounts
import models.master.{Accounts, Organizations, Zones}
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.GetAccount
import utilities.PushNotifications
import views.companion.master.Login

import scala.concurrent.ExecutionContext

class LoginController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: Accounts, blockchainAclAccounts: ACLAccounts, blockchainZones: blockchain.Zones, blockchainOrganizations: blockchain.Organizations, blockchainAssets: blockchain.Assets, blockchainFiats: blockchain.Fiats, blockchainOwners: blockchain.Owners, masterOrganizations: Organizations, masterZones: Zones, blockchainAclHashes: blockchain.ACLHashes, getAccount: GetAccount, blockchainAccounts: blockchain.Accounts, withUsernameToken: WithUsernameToken, pushNotifications: PushNotifications)(implicit exec: ExecutionContext, configuration: Configuration, wsClient: WSClient) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_LOGIN

  def loginForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.login(Login.form))
  }

  def login: Action[AnyContent] = Action { implicit request =>
    Login.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.login(formWithErrors))
      },
      loginData => {
        try {
          if (masterAccounts.Service.validateLogin(loginData.username, loginData.password)) {
            val address = masterAccounts.Service.getAddress(loginData.username)
            pushNotifications.registerNotificationToken(loginData.username, loginData.notificationToken)
            pushNotifications.sendNotification(loginData.username, constants.Notification.LOGIN)
            masterAccounts.Service.getUserType(loginData.username) match {
              case constants.User.GENESIS =>
                withUsernameToken.Ok(views.html.component.master.genesisHome(username = loginData.username, userType = constants.User.GENESIS, address = address, coins = blockchainAccounts.Service.getCoins(address)), loginData.username)
              case constants.User.ZONE =>
                withUsernameToken.Ok(views.html.component.master.zoneHome(username = loginData.username, userType = constants.User.ZONE, address = address, coins = blockchainAccounts.Service.getCoins(address), zone = masterZones.Service.getZone(blockchainZones.Service.getID(address))), loginData.username)
              case constants.User.ORGANIZATION =>
                withUsernameToken.Ok(views.html.component.master.organizationHome(username = loginData.username, userType = constants.User.ORGANIZATION, address = address, coins = blockchainAccounts.Service.getCoins(address), organization = masterOrganizations.Service.getOrganization(blockchainOrganizations.Service.getID(address))), loginData.username)
              case constants.User.TRADER =>
                val aclAccount = blockchainAclAccounts.Service.getACLAccount(address)
                val owners = blockchainOwners.Service.getOwners(address)
                withUsernameToken.Ok(views.html.component.master.traderHome(username = loginData.username, userType = constants.User.TRADER, address = address, coins = blockchainAccounts.Service.getCoins(address), assetPegWallet = blockchainAssets.Service.getAssetPegWallet(address), fiatPegWallet = blockchainFiats.Service.getFiatPegWallet(owners.map(_.pegHash).seq), totalFiat = owners.map(_.amount).sum, zone = masterZones.Service.getZone(aclAccount.zoneID), organization = masterOrganizations.Service.getOrganization(aclAccount.organizationID), aclHash = blockchainAclHashes.Service.getACLHash(aclAccount.aclHash)), loginData.username)
              case constants.User.USER =>
                withUsernameToken.Ok(views.html.component.master.userHome(username = loginData.username, userType = constants.User.USER, address = address, coins = blockchainAccounts.Service.getCoins(address)), loginData.username)
              case constants.User.UNKNOWN =>
                withUsernameToken.Ok(views.html.component.master.unknownHome(username = loginData.username, userType = constants.User.UNKNOWN, address = address), loginData.username)
              case constants.User.WITHOUT_LOGIN =>
                masterAccounts.Service.updateUserType(loginData.username, constants.User.UNKNOWN)
                withUsernameToken.Ok(views.html.component.master.unknownHome(username = loginData.username, userType = constants.User.UNKNOWN, address = address), loginData.username)
            }
          }
          else Ok(views.html.index(failure = Messages(constants.Security.INVALID_LOG_IN)))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
        }
      }
    )
  }
}
