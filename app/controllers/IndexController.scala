package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain
import models.blockchain.ACLAccounts
import models.master.{Accounts, Organizations, Zones}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import queries.GetAccount
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

@Singleton
class IndexController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, masterAccounts: Accounts, blockchainAclAccounts: ACLAccounts, blockchainZones: blockchain.Zones, blockchainOrganizations: blockchain.Organizations, blockchainAssets: blockchain.Assets, blockchainFiats: blockchain.Fiats, blockchainNegotiations: blockchain.Negotiations, masterOrganizations: Organizations, masterZones: Zones, blockchainAclHashes: blockchain.ACLHashes, blockchainOrders: blockchain.Orders, getAccount: GetAccount, blockchainAccounts: blockchain.Accounts, withUsernameToken: WithUsernameToken)(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  def index: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        loginState.userType match {
          case constants.User.GENESIS =>
            withUsernameToken.Ok(views.html.genesisIndex())
          case constants.User.ZONE =>
            withUsernameToken.Ok(views.html.zoneIndex(zone = masterZones.Service.get(blockchainZones.Service.getID(loginState.address))))
          case constants.User.ORGANIZATION =>
            withUsernameToken.Ok(views.html.organizationIndex(organization = masterOrganizations.Service.get(blockchainOrganizations.Service.getID(loginState.address))))
          case constants.User.TRADER =>
            val aclAccount = blockchainAclAccounts.Service.get(loginState.address)
            withUsernameToken.Ok(views.html.traderIndex(totalFiat = blockchainFiats.Service.getFiatPegWallet(loginState.address).map(_.transactionAmount.toInt).sum, zone = masterZones.Service.get(aclAccount.zoneID), organization = masterOrganizations.Service.get(aclAccount.organizationID)))
          case constants.User.USER =>
            withUsernameToken.Ok(views.html.userIndex())
          case constants.User.UNKNOWN =>
            withUsernameToken.Ok(views.html.anonymousIndex())
          case constants.User.WITHOUT_LOGIN =>
            masterAccounts.Service.updateUserType(loginState.username, constants.User.UNKNOWN)
            withUsernameToken.Ok(views.html.anonymousIndex())
        }
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def testAction(username:String)=Action{

      val timeInitial=System.currentTimeMillis()
      val account=masterAccounts.Service.getAccount(username)
      val address=masterAccounts.Service.getAddress(username)

      val timeNow=System.currentTimeMillis()
      println( "The user details are \n username -> buyer025 \n address-> "+address+"\n serretHash -> "+account.secretHash+"\n and time taken"+(timeNow-timeInitial))
     // val str= "The user details are \n username -> buyer025 \n address-> "+address+"\n serretHash -> "+account.secretHash+"\n usertype ->"+userType+"\n lang-> "+lang+"\n availability->"+ !availability +"\n and time taken"+(timeNow-timeInitial)
      Ok("Done")
  }

  def func(usr:String)={
    usr+"sgsfs"
  }

  def testActionAsync=Action.async{

    val timeInitial = System.currentTimeMillis()

    val account = masterAccounts.Service.getAccountAsync("SELL15B2A9cajl")
    val address = masterAccounts.Service.getAddressAsync("SELL15B2A9cajl")
    val userType = masterAccounts.Service.getUserTypeAsync("SELL15B2A9cajl")
    val lang = masterAccounts.Service.getLanguageAsync("SELL15B2A9ca")
    val availability = masterAccounts.Service.checkUsernameAvailableAsync("SELL15B2A9cajl")

    for {
      _ <- account
      _ <- address
      _ <- userType
      _ <- lang
      _ <- availability
    } yield {}

   somet.map{some=>
     Ok("fwef")
   }.recover{
     case baseException: BaseException=> Ok(baseException.module+"   "+baseException.failure.message)
     case e:Exception=> Ok("someOtherError")
   }

  }


}
