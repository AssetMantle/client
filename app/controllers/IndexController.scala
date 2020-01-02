package controllers

import akka.stream.scaladsl.Source
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

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

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
        case constants.User.USER => withUsernameToken.Ok(views.html.userIndex())
        case constants.User.UNKNOWN => withUsernameToken.Ok(views.html.anonymousIndex())
        case constants.User.WITHOUT_LOGIN =>
          val updateUserType = masterAccounts.Service.updateUserType(loginState.username, constants.User.UNKNOWN)
          for {
            _ <- updateUserType
            result<-withUsernameToken.Ok(views.html.anonymousIndex())
          } yield result
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def testPdf()=Action{

    Ok(views.html.component.master.pdfViewer())
  }

  def testFunc(username: String)={
    val exSeq=Seq("testBuyer1511","testZone1511","testOrg1511","testSeller1511")

   exSeq.foldLeft(Future(List.empty[String])){(prev,user)=>

      val account = masterAccounts.Service.getAccountAsync(user)

      val lang = masterAccounts.Service.getLanguageAsync(user)
     println(user)
      val userType = masterAccounts.Service.getUserType(user)
      println("userType222")
      //account.accountAddress+"   "+lang+"    "+userType

      for{
        prev<-prev
        account<-account
        lang<-lang
        userType<-userType
      }yield {
        println("Above sleep2222")
        Thread.sleep(2000)
        println("Below sleep2222")
        prev :+ (account.accountAddress+"   "+lang+"    "+userType)
      }
    }



    /*Source(exSeq.toList).mapAsync(parallelism = 2){user=>
      val account = masterAccounts.Service.getAccountAsync(user)

      val lang = masterAccounts.Service.getLanguageAsync(user)

      val userType = masterAccounts.Service.getUserType(user)
      println("userType222")
      //account.accountAddress+"   "+lang+"    "+userType

      for{
        account<-account
        lang<-lang
        userType<-userType
      }yield {
        println("Above sleep2222")
        Thread.sleep(2000)
        println("Below sleep2222")
        account.accountAddress+"   "+lang+"    "+userType
      }
      //  Await.result(z,Duration.Inf)

    }*/

    /* exSeq.map{user=>
       val account = masterAccounts.Service.getAccountAsync(user)

       val lang = masterAccounts.Service.getLanguageAsync(user)

       val userType = masterAccounts.Service.getUserType(user)
       println("userType")
       //account.accountAddress+"   "+lang+"    "+userType

       val z=for{
         account<-account
         lang<-lang
         userType<-userType
       }yield {
         println("Above sleep")
         Thread.sleep(2000)
         println("Below sleep")
         account.accountAddress+"   "+lang+"    "+userType
       }*/





    /* val account = masterAccounts.Service.getAccount(username)
     val address = masterAccounts.Service.getAddress(account.id)

     val userType = masterAccounts.Service.getUserType(account.id)

     for{
       address<- address
       userType<- userType
     }yield address+"         "+userType*/
  }

  def testActionAsync(username: String) = Action.async {
 //   println("getting Account for" + username)


  /*  val account = masterAccounts.Service.getAccountAsync(username)

    val address = masterAccounts.Service.getAddress(username)

    val userType = masterAccounts.Service.getUserType(username)

    val lang = masterAccounts.Service.getLanguageAsync(username)

    val availability = masterAccounts.Service.checkUsernameAvailable(username)

    for {
      account <- account
      address <- address
      userType <- userType
      lang <- lang
      availability <- availability
    } yield {
      println("Info-hash" + username + "         " + account.secretHash + "        " + address + "        " + userType + "        " + lang + "        " + availability)
      Ok
    }*/

   /* val exSeq=Seq("testBuyer1511","testZone1511","testOrg1511","testSeller1511")
    val y=
      exSeq.map { user =>
        val account = masterAccounts.Service.getAccount(user)
        println("1st Print")
        Thread.sleep(3000)
        val lang = masterAccounts.Service.getLanguage(user)
        println("2nd Print")
        (account.accountAddress+"  "+lang)
    }*/
   val z=testFunc(username)
    for{
      z<-z
    }yield Ok(z.mkString(""))
  /*  val y=testFunc(username)
    Future(Ok(y.mkString("")))*/
    /*(for{
      y<-y
    }yield Ok(y.mkString("  "))
      ).recover{
      case baseException: BaseException => Ok("Error Found")
    }*/
}





    /* val x=Future{
       println("Future running")
       Thread.sleep(10000)
       println("Future Complete")
       throw new BaseException(constants.Response.DOCUMENT_NOT_FOUND)
     }

     Future{Ok("what!!!")}*/
    /* val userType = masterAccounts.Service.getUserTypeAsync("minijoker123")

     (for{
       _<-somet
       _ <- userType
     }yield Ok("fwef")
       ).recover{
       case baseException: BaseException=> Ok(baseException.module+"   "+baseException.failure.message+"kjbhnjjnkjn")
       case e:Exception=> Ok("someOtherError")
     }*/
    /*somet.map{some=>
      Ok("fwef")
    }.recover{
      case baseException: BaseException=> Ok(baseException.module+"   "+baseException.failure.message)
      case e:Exception=> Ok("someOtherError")
    }*/


}
