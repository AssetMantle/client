package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain
import models.blockchain.{ACLAccount, ACLAccounts}
import models.master._
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import queries.GetAccount

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndexController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, masterAccounts: Accounts, blockchainAclAccounts: ACLAccounts, blockchainZones: blockchain.Zones, blockchainOrganizations: blockchain.Organizations, blockchainAssets: blockchain.Assets, blockchainFiats: blockchain.Fiats, blockchainNegotiations: blockchain.Negotiations, masterOrganizations: Organizations, masterZones: Zones, blockchainAclHashes: blockchain.ACLHashes, blockchainOrders: blockchain.Orders, getAccount: GetAccount, blockchainAccounts: blockchain.Accounts, withUsernameToken: WithUsernameToken)(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_INDEX

  def index: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      (loginState.userType match {
        case constants.User.GENESIS => withUsernameToken.Ok(views.html.genesisIndex())
        case constants.User.ZONE =>
          val id = blockchainZones.Service.getID(loginState.address)

          def zone(id: String): Future[Zone] = masterZones.Service.get(id)

          for {
            id <- id
            zone <- zone(id)
            result<-withUsernameToken.Ok(views.html.zoneIndex(zone = zone))
          } yield result
        case constants.User.ORGANIZATION =>
          val id = blockchainOrganizations.Service.getID(loginState.address)

          def organization(id: String): Future[Organization] = masterOrganizations.Service.get(id)

          for {
            id <- id
            organization <- organization(id)
            result<-withUsernameToken.Ok(views.html.organizationIndex(organization = organization))
          } yield result
        case constants.User.TRADER =>
          val aclAccount = blockchainAclAccounts.Service.get(loginState.address)
          val totalFiat = blockchainFiats.Service.getFiatPegWallet(loginState.address)

          def getZoneAndOrganization(aclAccount: ACLAccount): Future[(Zone, Organization)] = {
            val zone = masterZones.Service.get(aclAccount.zoneID)
            val organization = masterOrganizations.Service.get(aclAccount.organizationID)
            for {
              zone <- zone
              organization <- organization
            } yield {
              (zone, organization)
            }
          }

          for {
            aclAccount <- aclAccount
            totalFiat <- totalFiat
            (zone, organization) <- getZoneAndOrganization(aclAccount)
            result<-withUsernameToken.Ok(views.html.traderIndex(totalFiat = totalFiat.map(_.transactionAmount.toInt).sum, zone = zone, organization = organization))
          } yield result
        case constants.User.USER => withUsernameToken.Ok(views.html.dashboard())
        case constants.User.UNKNOWN => withUsernameToken.Ok(views.html.dashboard())
        case constants.User.WITHOUT_LOGIN =>
          val updateUserType = masterAccounts.Service.updateUserType(loginState.username, constants.User.UNKNOWN)
          for {
            _ <- updateUserType
            result<-withUsernameToken.Ok(views.html.dashboard())
          } yield result
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }
}
