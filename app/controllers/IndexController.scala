package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain
import models.blockchain.{ACLAccount, ACLAccounts}
import models.master.{Accounts, Organizations, Zones}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import queries.GetAccount

import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

@Singleton
class IndexController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, masterAccounts: Accounts, blockchainAclAccounts: ACLAccounts, blockchainZones: blockchain.Zones, blockchainOrganizations: blockchain.Organizations, blockchainAssets: blockchain.Assets, blockchainFiats: blockchain.Fiats, blockchainNegotiations: blockchain.Negotiations, masterOrganizations: Organizations, masterZones: Zones, blockchainAclHashes: blockchain.ACLHashes, blockchainOrders: blockchain.Orders, getAccount: GetAccount, blockchainAccounts: blockchain.Accounts, withUsernameToken: WithUsernameToken)(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_INDEX

  def index: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
    /*  try {
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
      }*/
      (loginState.userType match {
        case constants.User.GENESIS =>
          Future{withUsernameToken.Ok(views.html.genesisIndex())}
        case constants.User.ZONE =>
          val id=blockchainZones.Service.getID(loginState.address)
          def zone(id:String)=masterZones.Service.get(id)
          for{
            id<-id
            zone<-zone(id)
          }yield withUsernameToken.Ok(views.html.zoneIndex(zone = zone))

        case constants.User.ORGANIZATION =>
          //withUsernameToken.Ok(views.html.organizationIndex(organization = masterOrganizations.Service.get(blockchainOrganizations.Service.getID(loginState.address))))
          val id=blockchainOrganizations.Service.getID(loginState.address)
          def organization(id:String)=masterOrganizations.Service.get(id)
          for{
            id<-id
            organization<-organization(id)
          }yield withUsernameToken.Ok(views.html.organizationIndex(organization = organization))
        case constants.User.TRADER =>
          val aclAccount = blockchainAclAccounts.Service.get(loginState.address)
          val totalFiat=blockchainFiats.Service.getFiatPegWallet(loginState.address)
          def getZoneAndOrganization(aclAccount:ACLAccount)={
            val zone=masterZones.Service.get(aclAccount.zoneID)
            val organization=masterOrganizations.Service.get(aclAccount.organizationID)
            for{
              zone<-zone
              organization<-organization
            }yield{(zone,organization)}
          }
          for{
            aclAccount<-aclAccount
            totalFiat<-totalFiat
            (zone,organization)<- getZoneAndOrganization(aclAccount)
          }yield withUsernameToken.Ok(views.html.traderIndex(totalFiat = totalFiat.map(_.transactionAmount.toInt).sum, zone = zone, organization = organization))

        case constants.User.USER =>
          Future{withUsernameToken.Ok(views.html.userIndex())}
        case constants.User.UNKNOWN =>
          Future{withUsernameToken.Ok(views.html.anonymousIndex())}
        case constants.User.WITHOUT_LOGIN =>
          val updateUserType=masterAccounts.Service.updateUserType(loginState.username, constants.User.UNKNOWN)
          for{
            _<-updateUserType
          }yield withUsernameToken.Ok(views.html.anonymousIndex())

      }).recover{
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

  /*def func(usr:String)={
    usr+"sgsfs"
  }*/

  def testActionAsync=Action.async{

   // val account = masterAccounts.Service.getAccountAsync("buyer014789")
   // val address = masterAccounts.Service.getAddressAsync("buyer014789")
  //  val userType = masterAccounts.Service.getUserTypeAsync("buyer014789")
    val lang = masterAccounts.Service.getLanguageAsync("minijoker123")
    val availability = masterAccounts.Service.checkUsernameAvailableAsync("minijoker123")

    def markDirtyFromAddress= {
      if (3 == 4) {
        val account = masterAccounts.Service.getAccountAsync("minijoker123")
        val address = masterAccounts.Service.getAddressAsync("minijoker123")

        for {
          account <- account
          address <- address

        } yield {}
      }else{Future{Unit}}

    }

    val somet=for {
      _ <- lang
      _ <- availability
      _<- markDirtyFromAddress
    } yield {}


    val x=Future{
      println("Future running")
      Thread.sleep(10000)
      println("Future Complete")
      throw new BaseException(constants.Response.DOCUMENT_NOT_FOUND)
    }


    val userType = masterAccounts.Service.getUserTypeAsync("minijoker123")

    (for{
      _<-somet
      _ <- userType
    }yield Ok("fwef")
      ).recover{
      case baseException: BaseException=> Ok(baseException.module+"   "+baseException.failure.message)
      case e:Exception=> Ok("someOtherError")
    }
   /*somet.map{some=>
     Ok("fwef")
   }.recover{
     case baseException: BaseException=> Ok(baseException.module+"   "+baseException.failure.message)
     case e:Exception=> Ok("someOtherError")
   }*/

  }


}
