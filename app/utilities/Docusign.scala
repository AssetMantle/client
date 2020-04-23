package utilities

import java.util.Arrays

import com.docusign.esign.api.EnvelopesApi
import com.docusign.esign.client.ApiClient
import com.docusign.esign.model.{Document, EnvelopeDefinition, RecipientViewRequest, Recipients, ReturnUrlRequest, Signer}
import com.sun.jersey.core.util.{Base64 => Base64Docusign}
import com.twilio.Twilio
import com.twilio.`type`.PhoneNumber
import com.twilio.exception.{ApiConnectionException, ApiException}
import com.twilio.rest.api.v2010.account.Message
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.Trader
import models.{master, masterTransaction}
import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.json.{Json, OWrites}
import play.api.libs.mailer._
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import java.io.{BufferedOutputStream, File, FileInputStream, FileOutputStream, FilterInputStream}

import models.masterTransaction.NegotiationFile
import org.apache.commons.codec.binary.Base64

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Docusign @Inject()(masterContacts: master.Contacts,
                             masterTransactionNotifications: masterTransaction.Notifications,
                             masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                             mailerClient: MailerClient,
                             masterTransactionPushNotificationTokens: masterTransaction.PushNotificationTokens,
                             wsClient: WSClient,
                             fileResourceManager: utilities.FileResourceManager,
                             masterAccounts: master.Accounts,
                             messagesApi: MessagesApi
                            )
                            (implicit
                             executionContext: ExecutionContext,
                             configuration: Configuration
                            ) {

  private implicit val module: String = constants.Module.UTILITIES_NOTIFICATION

  private implicit val logger: Logger = Logger(this.getClass)

  //val baseUrl = configuration.get[String]("comdex.url")

  val accessToken="eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IjY4MTg1ZmYxLTRlNTEtNGNlOS1hZjFjLTY4OTgxMjIwMzMxNyJ9.eyJUb2tlblR5cGUiOjUsIklzc3VlSW5zdGFudCI6MTU4NzYzMDM1NywiZXhwIjoxNTg3NjU5MTU3LCJVc2VySWQiOiJjMjViNmE3OC1kZDc2LTQwOGEtODllZi0wZjhjMTVmYmZhZjYiLCJzaXRlaWQiOjEsInNjcCI6WyJzaWduYXR1cmUiLCJjbGljay5tYW5hZ2UiLCJvcmdhbml6YXRpb25fcmVhZCIsInJvb21fZm9ybXMiLCJncm91cF9yZWFkIiwicGVybWlzc2lvbl9yZWFkIiwidXNlcl9yZWFkIiwidXNlcl93cml0ZSIsImFjY291bnRfcmVhZCIsImRvbWFpbl9yZWFkIiwiaWRlbnRpdHlfcHJvdmlkZXJfcmVhZCIsImR0ci5yb29tcy5yZWFkIiwiZHRyLnJvb21zLndyaXRlIiwiZHRyLmRvY3VtZW50cy5yZWFkIiwiZHRyLmRvY3VtZW50cy53cml0ZSIsImR0ci5wcm9maWxlLnJlYWQiLCJkdHIucHJvZmlsZS53cml0ZSIsImR0ci5jb21wYW55LnJlYWQiLCJkdHIuY29tcGFueS53cml0ZSJdLCJhdWQiOiJmMGYyN2YwZS04NTdkLTRhNzEtYTRkYS0zMmNlY2FlM2E5NzgiLCJhenAiOiJmMGYyN2YwZS04NTdkLTRhNzEtYTRkYS0zMmNlY2FlM2E5NzgiLCJpc3MiOiJodHRwczovL2FjY291bnQtZC5kb2N1c2lnbi5jb20vIiwic3ViIjoiYzI1YjZhNzgtZGQ3Ni00MDhhLTg5ZWYtMGY4YzE1ZmJmYWY2IiwiYW1yIjpbImludGVyYWN0aXZlIl0sImF1dGhfdGltZSI6MTU4NzYzMDM1NCwicHdpZCI6ImIwZDk2OTU0LTczNmItNDk4Mi04NjNiLTk4YWY5OTNmZTIyMCJ9.PpUUeyPccVWJbKkKw9Whki_RB9r7-wx_u7OnM1MS_mnCnN_LOjh_7wt3rCie9ZiYcg6gHf_1M_O7QwrRapMtzntj4i6j6uDrP2hPjUzEWgjb7JIrNsJqBkLD7NKpiNwMxQUxvHWgJ6rsQuteZxhPnWqAf3mmg16tCAr764yhRoVYPfiOFTRXCLW-rmrlt-6uCPdP2B_Hw6iQj-gFijK45ZJnq35d7NuzsQJl7X5aPi5R6t5ll0mN-Yx9MLUanwvV-1DPRi5jh4vRdJ4F-zofzb5lWNcScPDW20XpjumnU8IlpfJA3Z4HjwSb7IjoCJGimQq59z-wTGEr0LNhOdsfmw"
  val accountID="10371036"
  val signerName="John Mclane"
  val signerEmail="chardinkichandani123@gmail.com"
  val baseUrl="http://192.168.0.105:9000"
  val clientUserID="9052042A09CA3974"
  val authenticationMethod="None"
  val basePath="https://demo.docusign.net/restapi"
  val tokenExpiration= (60.toLong).*(480:Long)

  private def createDocusignEnvelope(id:String,emailAddress:String, documentType:String, fileName: String, trader: Trader)={
    try {
      val fileByteArray = utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile(fileResourceManager.getTraderNegotiationFilePath(documentType), fileName))
      val base64File = new String(Base64Docusign.encode(fileByteArray))

      val document = new Document()
      document.setDocumentBase64(base64File)
      document.setName(constants.File.CONTRACT)
      println("fileName-----------"+fileName)
      println("fileExtension-----------"+utilities.FileOperations.fileExtensionFromName(fileName))
      document.setFileExtension(utilities.FileOperations.fileExtensionFromName(fileName))
      document.setDocumentId("1")

      val signer = new Signer()
      signer.setEmail(emailAddress)
      signer.setName(trader.name)
      signer.clientUserId(trader.id)
      signer.recipientId("1")

      val envelopeDefinition = new EnvelopeDefinition()
      envelopeDefinition.setEmailSubject("Please sign this document")
      envelopeDefinition.setDocuments(Arrays.asList(document))
      val recipients = new Recipients()
      recipients.setSigners(Arrays.asList(signer))

      envelopeDefinition.setRecipients(recipients)
      envelopeDefinition.setStatus(constants.Status.DocuSignEnvelopeStatus.CREATED)

      val apiClient = new ApiClient(basePath)
      apiClient.setAccessToken(accessToken, tokenExpiration)

      val envelopesApi = new EnvelopesApi(apiClient)
      val results = envelopesApi.createEnvelope(accountID, envelopeDefinition)
      val envelopeId = results.getEnvelopeId

      val viewRequest = new ReturnUrlRequest
      viewRequest.setReturnUrl(baseUrl+"/docusignReturn")

      (envelopesApi.createSenderView(accountID, envelopeId, viewRequest).getUrl,envelopeId)

    }catch {
      case apiException: ApiException => logger.error(apiException.getMessage, apiException)
        throw new BaseException(constants.Response.SMS_SEND_FAILED)
    }
  }


  def createEnvelope(id:String,emailAddress:String, documentType:String, fileName: String, trader: Trader)=createDocusignEnvelope(id,emailAddress,documentType,fileName,trader)

  private def createRecipientView22(envelopeID:String,emailAddress:String, trader: Trader)={
    val apiClient = new ApiClient(basePath)
    apiClient.setAccessToken(accessToken, tokenExpiration)

    val envelopesApi = new EnvelopesApi(apiClient)

    val viewRequest = new RecipientViewRequest()
    viewRequest.setReturnUrl(baseUrl + s"/docusignReturn?envelopeId=${envelopeID}")
    viewRequest.setAuthenticationMethod(authenticationMethod)
    viewRequest.setEmail(emailAddress)
    viewRequest.setUserName(trader.name)
    viewRequest.setClientUserId(trader.id)

    envelopesApi.createRecipientView(accountID, envelopeID, viewRequest).getUrl
  }

  def createRecipientView(envelopeID:String,emailAddress:String, trader: Trader)=createRecipientView22(envelopeID,emailAddress,trader)

  def updateSignedDOcuemnt(envelopeID:String,fileName:String)=  fetchAndStoreSignedDocument(envelopeID,fileName)

  def fetchAndStoreSignedDocument(envelopeID:String,fileName:String)={

    val apiClient = new ApiClient(basePath)
    apiClient.setAccessToken(accessToken, tokenExpiration)
    val envelopesApi = new EnvelopesApi(apiClient)
    val result =envelopesApi.getDocument(accountID,envelopeID,"1")
    val newName=List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(result)).toString, utilities.FileOperations.fileExtensionFromName(fileName)).mkString(".")
    val byteArray= utilities.FileOperations.fileExtensionFromName(fileName) match{
      case constants.File.PDF=>None
      case _=>Some(result)
    }
    val file = utilities.FileOperations.newFile(fileResourceManager.getTraderNegotiationFilePath(constants.File.CONTRACT),newName)
    val bos = new BufferedOutputStream(new FileOutputStream(file))
    bos.write(result)
    bos.close()
   // val updateFile= masterTransactionNegotiationFiles.Service.insertOrUpdateOldDocument(NegotiationFile())
    (newName,byteArray)
  }

  private def sendSMS(accountID: String, sms: constants.Notification.SMS, messageParameters: String*)(implicit lang: Lang) = {
    val mobileNumber = masterContacts.Service.tryGetMobileNumber(accountID)
    (for {
      mobileNumber <- mobileNumber
    } yield Message.creator(new PhoneNumber(mobileNumber), "smsFromNumber", messagesApi(sms.message, messageParameters: _*)).create()
      ).recover {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
      case apiException: ApiException => logger.error(apiException.getMessage, apiException)
        throw new BaseException(constants.Response.SMS_SEND_FAILED)
      case apiConnectionException: ApiConnectionException => logger.error(apiConnectionException.getMessage, apiConnectionException)
        throw new BaseException(constants.Response.SMS_SERVICE_CONNECTION_FAILURE)
    }
  }


}
