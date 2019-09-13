package controllers

import controllers.actions.LoginState
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.ACLAccounts
import models.{blockchain, master}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import queries.GetAccount
import utilities.PushNotification
import views.companion.master.Login

import scala.concurrent.ExecutionContext

@Singleton
class LoginController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, blockchainAclAccounts: ACLAccounts, blockchainZones: blockchain.Zones, blockchainOrganizations: blockchain.Organizations, blockchainAssets: blockchain.Assets, blockchainFiats: blockchain.Fiats, blockchainNegotiations: blockchain.Negotiations, masterOrganizations: master.Organizations, masterZones: master.Zones, blockchainAclHashes: blockchain.ACLHashes, blockchainOrders: blockchain.Orders, getAccount: GetAccount, blockchainAccounts: blockchain.Accounts, withUsernameToken: WithUsernameToken, pushNotification: PushNotification)(implicit exec: ExecutionContext, configuration: Configuration, wsClient: WSClient) extends AbstractController(messagesControllerComponents) with I18nSupport {

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
          val userType = masterAccounts.Service.getUserType(loginData.username)
          val address = masterAccounts.Service.getAddress(loginData.username)
          implicit val loginState: LoginState = LoginState(loginData.username, userType, address, if (userType == constants.User.TRADER) Option(blockchainAclHashes.Service.getACL(blockchainAclAccounts.Service.getACLHash(address))) else None)
          val contactWarnings: Seq[constants.Response.Warning] = utilities.Contact.getWarnings(masterAccounts.Service.validateLoginAndGetStatus(loginData.username, loginData.password))
          pushNotification.registerNotificationToken(loginData.username, loginData.notificationToken)
          pushNotification.sendNotification(loginData.username, constants.Notification.LOGIN, loginData.username)
          loginState.userType match {
            case constants.User.GENESIS =>
              withUsernameToken.Ok(views.html.genesisIndex(warnings = contactWarnings))
            case constants.User.ZONE =>
              withUsernameToken.Ok(views.html.zoneIndex(zone = masterZones.Service.get(blockchainZones.Service.getID(loginState.address)), warnings = contactWarnings))
            case constants.User.ORGANIZATION =>
              withUsernameToken.Ok(views.html.organizationIndex(organization = masterOrganizations.Service.get(blockchainOrganizations.Service.getID(loginState.address)), warnings = contactWarnings))
            case constants.User.TRADER =>
              val aclAccount = blockchainAclAccounts.Service.get(loginState.address)
              withUsernameToken.Ok(views.html.traderIndex(totalFiat = blockchainFiats.Service.getFiatPegWallet(loginState.address).map(_.transactionAmount.toInt).sum, zone = masterZones.Service.get(aclAccount.zoneID), organization = masterOrganizations.Service.get(aclAccount.organizationID), warnings = contactWarnings))
            case constants.User.USER =>
              withUsernameToken.Ok(views.html.userIndex(warnings = contactWarnings))
            case constants.User.UNKNOWN =>
              withUsernameToken.Ok(views.html.anonymousIndex(warnings = contactWarnings))
            case constants.User.WITHOUT_LOGIN =>
              masterAccounts.Service.updateUserType(loginData.username, constants.User.UNKNOWN)
              withUsernameToken.Ok(views.html.anonymousIndex(warnings = contactWarnings))
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
