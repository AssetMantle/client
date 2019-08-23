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

import scala.concurrent.ExecutionContext

@Singleton
class IndexController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, masterAccounts: Accounts, blockchainAclAccounts: ACLAccounts, blockchainZones: blockchain.Zones, blockchainOrganizations: blockchain.Organizations, blockchainAssets: blockchain.Assets, blockchainFiats: blockchain.Fiats, blockchainNegotiations: blockchain.Negotiations, masterOrganizations: Organizations, masterZones: Zones, blockchainAclHashes: blockchain.ACLHashes, blockchainOrders: blockchain.Orders, getAccount: GetAccount, blockchainAccounts: blockchain.Accounts, withUsernameToken: WithUsernameToken)(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  def index: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
    try {
      loginState.userType match {
        case constants.User.GENESIS =>
          withUsernameToken.Ok(views.html.genesisIndex(), loginState.username)
        case constants.User.ZONE =>
          withUsernameToken.Ok(views.html.zoneIndex(zone = masterZones.Service.get(blockchainZones.Service.getID(loginState.address))), loginState.username)
        case constants.User.ORGANIZATION =>
          withUsernameToken.Ok(views.html.organizationIndex(organization = masterOrganizations.Service.get(blockchainOrganizations.Service.getID(loginState.address))), loginState.username)
        case constants.User.TRADER =>
          val aclAccount = blockchainAclAccounts.Service.get(loginState.address)
          withUsernameToken.Ok(views.html.traderIndex(totalFiat =  blockchainFiats.Service.getFiatPegWallet(loginState.address).map(_.transactionAmount.toInt).sum, zone = masterZones.Service.get(aclAccount.zoneID), organization = masterOrganizations.Service.get(aclAccount.organizationID)), loginState.username)
        case constants.User.USER =>
          withUsernameToken.Ok(views.html.userIndex(), loginState.username)
        case constants.User.UNKNOWN =>
          withUsernameToken.Ok(views.html.anonymousIndex(), loginState.username)
        case constants.User.WITHOUT_LOGIN =>
          masterAccounts.Service.updateUserType(loginState.username, constants.User.UNKNOWN)
          withUsernameToken.Ok(views.html.anonymousIndex(), loginState.username)
      }
    }
    catch {
      case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
    }
  }
}
