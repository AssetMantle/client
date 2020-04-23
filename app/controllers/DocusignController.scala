package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{master, masterTransaction}
import models.master.Contact
import play.api.i18n.I18nSupport
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, Result}
import play.api.{Configuration, Logger}
import views.companion.master.UpdateContact
import com.docusign.esign.api._
import play.api.libs.Files
import java.io.File
import java.util.List

import javax.ws.rs.core.UriBuilderException
import com.sun.jersey.core.util.Base64
import com.docusign.esign.model.Document
import com.docusign.esign.model._
import java.util.Arrays

import com.docusign.esign.client.ApiClient
import models.masterTransaction.NegotiationFile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DocusignController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                   utilitiesNotification: utilities.Notification,
                                   fileResourceManager: utilities.FileResourceManager,
                                   docusign: utilities.Docusign,
                                   masterContacts: master.Contacts,
                                   masterNegotiations: master.Negotiations,
                                   withLoginAction: WithLoginAction,
                                   withTraderLoginAction: WithTraderLoginAction,
                                   masterAccounts: master.Accounts,
                                   masterTraders: master.Traders,
                                   masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                   masterTransactionDocusigns: masterTransaction.Docusigns,
                                   withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CONTACT

  implicit val contactWrites: OWrites[master.Contact] = Json.writes[master.Contact]

  //val baseUrl = configuration.get[String]("comdex.url")

  val accessToken="eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IjY4MTg1ZmYxLTRlNTEtNGNlOS1hZjFjLTY4OTgxMjIwMzMxNyJ9.eyJUb2tlblR5cGUiOjUsIklzc3VlSW5zdGFudCI6MTU4NzU4Njg3NywiZXhwIjoxNTg3NjE1Njc3LCJVc2VySWQiOiJjMjViNmE3OC1kZDc2LTQwOGEtODllZi0wZjhjMTVmYmZhZjYiLCJzaXRlaWQiOjEsInNjcCI6WyJzaWduYXR1cmUiLCJjbGljay5tYW5hZ2UiLCJvcmdhbml6YXRpb25fcmVhZCIsInJvb21fZm9ybXMiLCJncm91cF9yZWFkIiwicGVybWlzc2lvbl9yZWFkIiwidXNlcl9yZWFkIiwidXNlcl93cml0ZSIsImFjY291bnRfcmVhZCIsImRvbWFpbl9yZWFkIiwiaWRlbnRpdHlfcHJvdmlkZXJfcmVhZCIsImR0ci5yb29tcy5yZWFkIiwiZHRyLnJvb21zLndyaXRlIiwiZHRyLmRvY3VtZW50cy5yZWFkIiwiZHRyLmRvY3VtZW50cy53cml0ZSIsImR0ci5wcm9maWxlLnJlYWQiLCJkdHIucHJvZmlsZS53cml0ZSIsImR0ci5jb21wYW55LnJlYWQiLCJkdHIuY29tcGFueS53cml0ZSJdLCJhdWQiOiJmMGYyN2YwZS04NTdkLTRhNzEtYTRkYS0zMmNlY2FlM2E5NzgiLCJhenAiOiJmMGYyN2YwZS04NTdkLTRhNzEtYTRkYS0zMmNlY2FlM2E5NzgiLCJpc3MiOiJodHRwczovL2FjY291bnQtZC5kb2N1c2lnbi5jb20vIiwic3ViIjoiYzI1YjZhNzgtZGQ3Ni00MDhhLTg5ZWYtMGY4YzE1ZmJmYWY2IiwiYW1yIjpbImludGVyYWN0aXZlIl0sImF1dGhfdGltZSI6MTU4NzU4Njg3MiwicHdpZCI6ImIwZDk2OTU0LTczNmItNDk4Mi04NjNiLTk4YWY5OTNmZTIyMCJ9.DUdLFSlTsKYjU4O14qP_UfAr3qyw6VYhJBd1EoFy2me17gHVq4n9c7BJ0dbRi3Al35f8ZqARRl8tRBkyz_8T7zC1gZM-IeumHp7P5dTzqvQYeaIRti-r6a9MWRImXxK3jqVfDICh7X9Q6c0EDCD20mqDXOuACbs_5aXBlwZrnwz-Q4_sdf2_Z-GszlqH8Kdxnnr4ln_nkpBp2ztxfeRBiuQ_6BfOFszsZjh9DUaiADkcOEc69I2mDK-0WOndxYySfWG1hmk5fE0N6XY3xCFtaaMzFxHuRH_CV43URVpdzGSbpNKAfkbjPrkvRsBsrFLOfAx219nDyzui9yevXEDnKA"
  val accountID="10371036"
  val signerName="John Mclane"
  val signerEmail="chardinkichandani123@gmail.com"
  val baseUrl="http://192.168.0.105:9000"
  val clientUserID="9052042A09CA3974"
  val authenticationMethod="None"
  val basePath="https://demo.docusign.net/restapi"

  val tokenExpiration= (60.toLong).*(480:Long)

  def embeddedSending(id:String, documentType:String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val buyerTraderID= masterNegotiations.Service.tryGetBuyerTraderID(id)
      def buyerTrader(traderID:String)= masterTraders.Service.tryGet(traderID)
      def emailAddress(accountID:String) = masterContacts.Service.tryGetVerifiedEmailAddress(accountID)

      val fileName= masterTransactionNegotiationFiles.Service.getFileName(id, documentType)
      def create(id: String,envelopeID: String) =  masterTransactionDocusigns.Service.create(id,envelopeID)
      (for{
        buyerTraderID<-buyerTraderID
        buyerTrader<-buyerTrader(buyerTraderID)
        emailAddress<-emailAddress(buyerTrader.accountID)
        fileName<-fileName
        (senderURL,envelopeID)<-Future(docusign.createEnvelope(id,emailAddress,documentType ,fileName,buyerTrader))
        _<- create(id,envelopeID)
      }yield Ok(views.html.component.master.docusignView(senderURL))
      ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(id=id,failures = Seq(baseException.failure)))
        case e:Exception=> logger.error(e.getMessage)
          InternalServerError(views.html.tradeRoom(id=id,failures = Seq(constants.Response.ALL_ASSET_FILES_NOT_VERIFIED)))
      }
  }

  def docusignReturn(envelopeId:String, event:String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      println("event----"+event)
    val updateStatus=event match{
      case constants.Status.DocuSignEnvelopeStatus.SENT=>{
         masterTransactionDocusigns.Service.markSent(envelopeId)
      }
      case constants.Status.DocuSignEnvelopeStatus.SIGNING_COMPLETE=>{
        val docuSign= masterTransactionDocusigns.Service.tryGetByEnvelopeID(envelopeId)
        def oldFile(id:String)= masterTransactionNegotiationFiles.Service.tryGet(id,constants.File.CONTRACT)
        def getSignedDocument(fileName:String)= Future(docusign.updateSignedDOcuemnt(envelopeId,fileName))
        //def
        def updateFile(negotiationFile: NegotiationFile,newFileNme:String,file:Option[Array[Byte]])= masterTransactionNegotiationFiles.Service.insertOrUpdateOldDocument(negotiationFile.updateFileName(newFileNme).updateFile(file))

        def markSigningComplete=masterTransactionDocusigns.Service.markSigningComplete(envelopeId)
        for{
          docuSign<-docuSign
          oldFile<-oldFile(docuSign.id)
          (fileName,file)<-getSignedDocument(oldFile.fileName)
          _<- updateFile(oldFile,fileName,file)
          _<-markSigningComplete
        }yield 0
      }
      case _=>Future(0)
    }

    for{
      _<-updateStatus
    }  yield Ok(views.html.component.master.documentSentForSignView(event))
  }

  def embeddedSigning(id:String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val emailAddress = masterContacts.Service.tryGetVerifiedEmailAddress(loginState.username)
      val trader= masterTraders.Service.tryGetByAccountID(loginState.username)
      val envelopeID= masterTransactionDocusigns.Service.tryGetEnvelopeID(id)
    for{
      emailAddress<-emailAddress
      trader<-trader
      envelopeID<-envelopeID
    }yield Ok(views.html.component.master.docusignView(docusign.createRecipientView(envelopeID,emailAddress,trader)))

  }


  def docuSignTest: Action[AnyContent] = Action {

      val fileByteArray=utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile("/home/prashant/projects/commitCentral/public/images/", "dummy.pdf"))
      println("fileByteArray"+fileByteArray)
      val base64File= Base64.encode(fileByteArray).toString
      val base64File2=new String(Base64.encode(fileByteArray))
      val document= new Document()

      document.setDocumentBase64(base64File2)
      document.setName("Example document")
      document.setFileExtension("pdf")
      document.setDocumentId("1")

      val signer=new Signer()
      signer.setEmail(signerEmail)
      signer.setName(signerName)
      signer.clientUserId(clientUserID)
      signer.recipientId("1")

      val envelopeDefinition=new EnvelopeDefinition()
      envelopeDefinition.setEmailSubject("Please sign this document")
      envelopeDefinition.setDocuments(Arrays.asList(document))

      val recipients= new Recipients()
      recipients.setSigners(Arrays.asList(signer))

      envelopeDefinition.setRecipients(recipients)
      envelopeDefinition.setStatus("created")

      val apiClient= new ApiClient(basePath)
      apiClient.setAccessToken(accessToken,tokenExpiration)

      val envelopesApi=new EnvelopesApi(apiClient)
      val results=envelopesApi.createEnvelope(accountID, envelopeDefinition)
      val envelopeId = results.getEnvelopeId()

      val viewRequest = new RecipientViewRequest()
      viewRequest.setReturnUrl(baseUrl + "/docuSignReturn")
      viewRequest.setAuthenticationMethod(authenticationMethod)
      viewRequest.setEmail(signerEmail)
      viewRequest.setUserName(signerName)
      viewRequest.setClientUserId(clientUserID)
    /*  val apiClient= new ApiClient(basePath)
      apiClient.setAccessToken(accessToken,tokenExpiration)
      //apiClient.addDefaultHeader("Authorization", "Bearer " + accessToken)*/

    //  val envelopesApi=new EnvelopesApi(apiClient)

      val results1 = envelopesApi.createRecipientView(accountID, envelopeId, viewRequest)
      val redirectUrl = results1.getUrl
      println(redirectUrl)
      val redirect= Redirect(redirectUrl)
      redirect
  }
}