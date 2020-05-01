package controllers

import java.util.Arrays

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
import com.docusign.esign.api.AuthenticationApi
import com.docusign.esign.api.EnvelopesApi
import com.docusign.esign.client.ApiClient
import com.docusign.esign.model.{EnvelopeDefinition, RecipientViewRequest, Recipients, ReturnUrlRequest, Signer, Document => DocusignDocument}
//import com.docusign.esign.model.Auth

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndexController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, masterAccounts: Accounts, blockchainAclAccounts: ACLAccounts, blockchainZones: blockchain.Zones, blockchainOrganizations: blockchain.Organizations, blockchainAssets: blockchain.Assets, blockchainFiats: blockchain.Fiats, blockchainNegotiations: blockchain.Negotiations, masterOrganizations: Organizations, masterZones: Zones, blockchainAclHashes: blockchain.ACLHashes, blockchainOrders: blockchain.Orders, getAccount: GetAccount, blockchainAccounts: blockchain.Accounts, withUsernameToken: WithUsernameToken)(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_INDEX

  def index: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>


      (loginState.userType match {
        case constants.User.GENESIS => {
          withUsernameToken.Ok(views.html.genesisIndex())
        }
        case constants.User.ZONE =>
          val id = blockchainZones.Service.getID(loginState.address)

          def zone(id: String): Future[Zone] = masterZones.Service.tryGet(id)

          for {
            id <- id
            zone <- zone(id)
            result <- withUsernameToken.Ok(views.html.zoneIndex(zone = zone))
          } yield result
        case constants.User.ORGANIZATION =>
          val id = blockchainOrganizations.Service.getID(loginState.address)

          def organization(id: String): Future[Organization] = masterOrganizations.Service.tryGet(id)

          for {
            id <- id
            organization <- organization(id)
            result <- withUsernameToken.Ok(views.html.organizationIndex(organization = organization))
          } yield result
        case constants.User.TRADER =>
          val aclAccount = blockchainAclAccounts.Service.get(loginState.address)
          val totalFiat = blockchainFiats.Service.getFiatPegWallet(loginState.address)

          def getZoneAndOrganization(aclAccount: ACLAccount): Future[(Zone, Organization)] = {
            val zone = masterZones.Service.tryGet(aclAccount.zoneID)
            val organization = masterOrganizations.Service.tryGet(aclAccount.organizationID)
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
            result <- withUsernameToken.Ok(views.html.traderIndex(totalFiat = totalFiat.map(_.transactionAmount.toInt).sum, zone = zone, organization = organization))
          } yield result
        case constants.User.USER => withUsernameToken.Ok(views.html.userIndex())
        case constants.User.UNKNOWN => withUsernameToken.Ok(views.html.anonymousIndex())
        case constants.User.WITHOUT_LOGIN =>
          val markUserTypeUser = masterAccounts.Service.markUserTypeUser(loginState.username)
          for {
            _ <- markUserTypeUser
            result <- withUsernameToken.Ok(views.html.dashboard())
          } yield result
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }


 /* def indexTest()= Action{

    val baseUri="https://demo.docusign.net/restapi"
    val clientID= "1c09afe5-88ee-4e4b-a4d3-b3c038c31953"
    val clientSecret="c849bf50-1b3a-4a91-ba0d-fdfe8ca344a1"
    val redirectUrl="http://localhost:9000/docusignReturn"
    val apiClient = new ApiClient(baseUri)
    val uri= apiClient.getAuthorizationUri(clientID,Arrays.asList("signature") ,redirectUrl,"code")
   // apiClient.getA
    /*val something=apiClient.generateAccessToken(clientID,clientSecret,redirectUrl)
    val x= something.getAccessToken
    val y= something.
    println(x)
    println(y)
    println(something)*/
    println(uri)
    println(uri.getUserInfo)
      Redirect(uri.toString.replace("https:/","https://"))
    //Redirect(uri.toString).withHeaders("Access-Control-Allow-Methods" -> "OPTIONS, GET, POST, PUT, DELETE, HEAD")
   // Future(Ok(views.html.component.master.docusignView(uri.toString)))

  }

  def returnDocuSign(code:String)=Action{
    println("code----------------------"+code)

    val baseUri="https://demo.docusign.net/restapi"
    val clientID= "1c09afe5-88ee-4e4b-a4d3-b3c038c31953"
    val clientSecret="c849bf50-1b3a-4a91-ba0d-fdfe8ca344a1"
    val redirectUrl="http://localhost:9000/docusignReturn"
    val apiClient = new ApiClient(baseUri)
    //apiClient.generateAccessToken()
    val x= apiClient.generateAccessToken(clientID,clientSecret,code)
    println(x)
    println("accessToken-------"+x.getAccessToken)
    println("expireTime-------"+x.getExpiresIn)
    val uri= apiClient.getAuthorizationUri(clientID,Arrays.asList("signature") ,redirectUrl,"code")
   // val y= apiClient.registerAccessTokenListener()
    //val z= apiClient.addAuthorization()
    //val w= new Authen
    Ok(code)
  }*/
}
