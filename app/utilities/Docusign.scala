package utilities

import java.util.Arrays

import com.docusign.esign.api.AuthenticationApi
import com.docusign.esign.api.EnvelopesApi
import com.docusign.esign.client.ApiClient
import com.docusign.esign.client.auth.OAuth
import com.docusign.esign.model.{EnvelopeDefinition, RecipientViewRequest, Recipients, ReturnUrlRequest, Signer, Document => DocusignDocument}
import com.docusign.esign.model.OauthAccess
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

import scala.concurrent.duration._
import akka.actor.ActorSystem
import com.sun.jersey.api.client.{Client, ClientHandlerException}
import controllers.routes
import models.Trait.Document
import models.masterTransaction.NegotiationFile
import org.apache.commons.codec.binary.Base64
import org.apache.oltu.oauth2.client.OAuthClient
import org.apache.oltu.oauth2.client.request.OAuthClientRequest.TokenRequestBuilder

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Docusign @Inject()(masterTransactionNotifications: masterTransaction.Notifications,
                         masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                         mailerClient: MailerClient,
                         actorSystem: ActorSystem,
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

  private val accountID = configuration.get[String]("docusign.accountID")
  private val authenticationMethod = configuration.get[String]("docusign.authenticationMethod")
  private val basePath = configuration.get[String]("docusign.basePath")
  private val integrationKey = configuration.get[String]("docusign.integrationKey")
  private val clientSecret = configuration.get[String]("docusign.clientSecret")
  private val comdexURL = configuration.get[String]("comdex.url")
  private val apiClient = new ApiClient(basePath)
  private val envelopesApi = new EnvelopesApi(apiClient)

  private def createDocusignEnvelope(emailAddress: String, file: Document[_], trader: Trader)(implicit language: Lang) = {
    try {
      val path = file.documentType match {
        case constants.File.OBL | constants.File.COO | constants.File.COA => fileResourceManager.getTraderAssetFilePath(file.documentType)
        case constants.File.CONTRACT | constants.File.INVOICE | constants.File.BILL_OF_EXCHANGE => fileResourceManager.getTraderNegotiationFilePath(file.documentType)
      }

      val document = new DocusignDocument()
      document.setDocumentBase64(new String(Base64Docusign.encode(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile(path, file.fileName)))))
      document.setName(file.fileName.split("""\.""")(0))
      document.setFileExtension(utilities.FileOperations.fileExtensionFromName(file.fileName))
      document.setDocumentId(constants.View.DOCUMENT_INDEX)

      val signer = new Signer()
      signer.setEmail(emailAddress)
      signer.setName(trader.name)
      signer.clientUserId(trader.id)
      signer.recipientId(constants.View.RECIPIENT_INDEX)

      val envelopeDefinition = new EnvelopeDefinition()
      envelopeDefinition.setEmailSubject(messagesApi(constants.View.PLEASE_SIGN_THIS_DOCUMENT))
      envelopeDefinition.setDocuments(Arrays.asList(document))

      val recipients = new Recipients()
      recipients.setSigners(Arrays.asList(signer))

      envelopeDefinition.setRecipients(recipients)
      envelopeDefinition.setStatus(constants.Status.DocuSignEnvelopeStatus.CREATED)

      envelopesApi.createEnvelope(accountID, envelopeDefinition).getEnvelopeId
    } catch {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
      case apiException: ApiException => logger.error(apiException.getMessage, apiException)
        throw new BaseException(constants.Response.ENVELOPE_CREATION_FAILED)
      case clientHandlerException: ClientHandlerException => logger.error(clientHandlerException.getMessage, clientHandlerException)
        throw new BaseException(constants.Response.INVALID_INPUT)
    }
  }

  def createSenderViewURL(envelopeID: String) = createSenderViewURL2(envelopeID)

  private def createSenderViewURL2(envelopeID: String) = {
    val viewRequest = new ReturnUrlRequest
    viewRequest.setReturnUrl(comdexURL + routes.DocusignController.docusignReturn(None,None).url)
    envelopesApi.createSenderView(accountID, envelopeID, viewRequest).getUrl
  }

  def createEnvelope(emailAddress: String, file: Document[_], trader: Trader) = {
    val language = masterAccounts.Service.tryGetLanguage(trader.accountID)
    for {
      language <- language
    } yield createDocusignEnvelope(emailAddress, file, trader)(Lang(language))
  }

  private def createRecipientView22(envelopeID: String, emailAddress: String, trader: Trader) = {
    try {
      val viewRequest = new RecipientViewRequest()
      viewRequest.setReturnUrl(comdexURL +routes.DocusignController.docusignReturn(Option(envelopeID),None).url)
      viewRequest.setAuthenticationMethod(authenticationMethod)
      viewRequest.setEmail(emailAddress)
      viewRequest.setUserName(trader.name)
      viewRequest.setClientUserId(trader.id)

      envelopesApi.createRecipientView(accountID, envelopeID, viewRequest).getUrl
    } catch {
      case apiException: ApiException => logger.error(apiException.getMessage, apiException)
        throw new BaseException(constants.Response.ENVELOPE_CREATION_FAILED)
      case clientHandlerException: ClientHandlerException => logger.error(clientHandlerException.getMessage, clientHandlerException)
        throw new BaseException(constants.Response.INVALID_INPUT)
    }
  }

  def createRecipientView(envelopeID: String, emailAddress: String, trader: Trader) = createRecipientView22(envelopeID, emailAddress, trader)

  def updateSignedDOcuemnt(envelopeID: String, fileName: String) = fetchAndStoreSignedDocument(envelopeID, fileName)

  def fetchAndStoreSignedDocument(envelopeID: String, fileName: String) = {
    val result = envelopesApi.getDocument(accountID, envelopeID, constants.View.DOCUMENT_INDEX)
    val newFileName = List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(result)).toString, constants.File.PDF).mkString(".")
    val file = utilities.FileOperations.newFile(fileResourceManager.getTraderNegotiationFilePath(constants.File.CONTRACT), newFileName)
    val bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file))
    bufferedOutputStream.write(result)
    bufferedOutputStream.close()
    newFileName
  }

  def updateAccessToken(code: String) = {
    try {
      generateAndUpdateAccessToken(code)
    } catch {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
    }
  }

  private def generateAndUpdateAccessToken(code: String) = {
    try {
      val oauthToken = apiClient.generateAccessToken(integrationKey, clientSecret, code)
      apiClient.setAccessToken(oauthToken.getAccessToken, oauthToken.getExpiresIn)
    } catch {
      case apiException: ApiException => logger.error(apiException.getMessage, apiException)
        throw new BaseException(constants.Response.ENVELOPE_CREATION_FAILED)
      case clientHandlerException: ClientHandlerException => logger.error(clientHandlerException.getMessage, clientHandlerException)
        throw new BaseException(constants.Response.INVALID_INPUT)
    }
  }

  def getAuthorizationUri = {
    try {
      fetchAuthorizationUri
    } catch {
      case apiException: ApiException => logger.error(apiException.getMessage, apiException)
        throw new BaseException(constants.Response.ENVELOPE_CREATION_FAILED)
      case clientHandlerException: ClientHandlerException => logger.error(clientHandlerException.getMessage, clientHandlerException)
        throw new BaseException(constants.Response.INVALID_INPUT)
    }
  }

  private def fetchAuthorizationUri = {
    try {
      apiClient.getAuthorizationUri(integrationKey, Arrays.asList(constants.View.SIGNATURE_SCOPE), comdexURL +routes.DocusignController.authorizationReturn(None).url, constants.View.CODE).toString
    } catch {
      case apiException: ApiException => logger.error(apiException.getMessage, apiException)
        throw new BaseException(constants.Response.ENVELOPE_CREATION_FAILED)
      case clientHandlerException: ClientHandlerException => logger.error(clientHandlerException.getMessage, clientHandlerException)
        throw new BaseException(constants.Response.INVALID_INPUT)
    }
  }

  object Utility {
    def regenerateAccessToken() = {
      //TODO regenrate access token based on refresh token
    }
  }

  actorSystem.scheduler.schedule(initialDelay = 60.seconds, interval = 2000.seconds) {
    Utility.regenerateAccessToken()
  }
}
