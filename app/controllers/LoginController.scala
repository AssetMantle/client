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
import transactions.Response.AccountResponse
import utilities.PushNotifications
import views.companion.master.Login

import scala.concurrent.ExecutionContext

class LoginController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: Accounts, blockchainAclAccounts: ACLAccounts, blockchainZones: models.blockchain.Zones, blockchainOrganizations: models.blockchain.Organizations, masterOrganizations: Organizations, masterZones: Zones, blockchainAclHashes: blockchain.ACLHashes, getAccount: GetAccount, blockchainAccounts: blockchain.Accounts, withUsernameToken: WithUsernameToken, pushNotifications: PushNotifications)(implicit exec: ExecutionContext, configuration: Configuration, wsClient: WSClient) extends AbstractController(messagesControllerComponents) with I18nSupport {

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
            val response = getAccount.Service.get(address)
            pushNotifications.registerNotificationToken(loginData.username, request.body.asFormUrlEncoded.get("token").headOption.get)
            pushNotifications.sendNotification(loginData.username, constants.Notification.LOGIN)
            masterAccounts.Service.getUserType(loginData.username).getOrElse(constants.User.USER) match {
              case constants.User.TRADER =>
                val aclAccount = blockchainAclAccounts.Service.getACLAccount(address)
                withUsernameToken.Ok(views.html.component.master.userHome(username = loginData.username, userType = constants.User.TRADER, account = response, zone = masterZones.Service.getZone(aclAccount.zoneID), organization = masterOrganizations.Service.getOrganization(aclAccount.organizationID), aclHash = blockchainAclHashes.Service.getACL(aclAccount.aclHash)), loginData.username)
              case constants.User.ZONE =>
                withUsernameToken.Ok(views.html.component.master.userHome(username = loginData.username, userType = constants.User.ZONE, account = response, zone = masterZones.Service.getZone(blockchainZones.Service.getID(address))), loginData.username)
              case constants.User.ORGANIZATION =>
                withUsernameToken.Ok(views.html.component.master.userHome(username = loginData.username, userType = constants.User.ORGANIZATION, account = response, organization = masterOrganizations.Service.getOrganization(blockchainOrganizations.Service.getID(address))), loginData.username)
              case constants.User.USER =>
                withUsernameToken.Ok(views.html.component.master.userHome(username = loginData.username, userType = constants.User.USER, account = response), loginData.username)
              case constants.User.GENESIS =>
                withUsernameToken.Ok(views.html.component.master.userHome(username = loginData.username, userType = constants.User.GENESIS, account = response), loginData.username)
            }
          }
          else Ok(views.html.index(failure = "Invalid Login!"))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => if (blockChainException.message == constants.Error.NO_RESPONSE) {
            withUsernameToken.Ok(views.html.component.master.userHome(username = loginData.username, userType = constants.User.UNKNOWN, account = AccountResponse.Response(value = AccountResponse.Value(masterAccounts.Service.getAddress(loginData.username), null, null, null, "-1", "0"))), loginData.username)
          } else {
            Ok(views.html.index(failure = blockChainException.message))
          }
        }
      }
    )
  }
}
