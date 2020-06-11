package utilities

import java.util.Arrays

import com.docusign.esign.api.EnvelopesApi
import com.docusign.esign.client.ApiClient
import com.docusign.esign.model.{EnvelopeDefinition, RecipientViewRequest, Recipients, Signer, Document => DocusignDocument, ReturnUrlRequest => CallBackURLRequest}
import com.sun.jersey.core.util.{Base64 => Base64Docusign}
import com.docusign.esign.client.ApiException
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.{Email, Trader}
import models.master
import play.api.i18n.{Lang, MessagesApi}
import play.api.{Configuration, Logger}
import java.io.{BufferedOutputStream, FileOutputStream}

import models.docusign.{OAuthToken => DocusignOAuthToken}
import com.docusign.esign.client.auth.OAuth.OAuthToken
import com.sun.jersey.api.client.ClientHandlerException
import controllers.routes
import models.Trait.Document
import org.apache.commons.codec.binary.Base64
import models.docusign
import services.KeyStore

import scala.collection.JavaConverters
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Docusign @Inject()(fileResourceManager: utilities.FileResourceManager,
                         masterAccounts: master.Accounts,
                         messagesApi: MessagesApi,
                         docusignOAuthTokens: docusign.OAuthTokens,
                         keyStore: KeyStore
                        )
                        (implicit
                         executionContext: ExecutionContext,
                         configuration: Configuration
                        ) {

  private implicit val module: String = constants.Module.UTILITIES_DOCUSIGN

  private implicit val logger: Logger = Logger(this.getClass)

  private val accountID = configuration.get[String]("docusign.accountID")
  private val authenticationMethod = configuration.get[String]("docusign.authenticationMethod")
  private val basePath = configuration.get[String]("docusign.basePath")
  private val comdexURL = configuration.get[String]("comdex.url")
  private val apiClient = new ApiClient(basePath)
  private val envelopesApi = new EnvelopesApi(apiClient)

  private def createDocusignEnvelope(emailList: Seq[Email], fileList: Seq[Document[_]], traderList: Seq[Trader]): Future[String] = {
    val oauthToken = docusignOAuthTokens.Service.tryGet(accountID)
    (for {
      oauthToken <- oauthToken
    } yield {
      apiClient.setAccessToken(oauthToken.accessToken, (oauthToken.expiresAt - System.currentTimeMillis()) / 1000.toLong)

      val documentList = fileList.zipWithIndex.map { case (file, index) =>
        val document = new DocusignDocument()
        document.setDocumentBase64(new String(Base64Docusign.encode(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile(fileResourceManager.getNegotiationFilePath(file.documentType), file.fileName)))))
        document.setName(file.fileName.split("""\.""")(0))
        document.setFileExtension(utilities.FileOperations.fileExtensionFromName(file.fileName))
        document.setDocumentId((index + 1).toString)
        document
      }

      val signerList = traderList.zipWithIndex.map { case (trader, index) =>
        val signer = new Signer()
        signer.setEmail(emailList.find(_.id == trader.accountID).map(_.emailAddress).getOrElse(""))
        signer.setName(trader.accountID)
        signer.clientUserId(trader.id)
        signer.recipientId((index + 1).toString)
        signer
      }

      val envelopeDefinition = new EnvelopeDefinition()
      envelopeDefinition.setEmailSubject(constants.View.PLEASE_SIGN_THIS_DOCUMENT)
      envelopeDefinition.setDocuments(JavaConverters.seqAsJavaList(documentList))

      val recipients = new Recipients()
      recipients.setSigners(JavaConverters.seqAsJavaList(signerList))

      envelopeDefinition.setRecipients(recipients)
      envelopeDefinition.setStatus(constants.External.Docusign.Status.CREATED)

      envelopesApi.createEnvelope(accountID, envelopeDefinition).getEnvelopeId
    }).recover {
      case baseException: BaseException =>
        logger.error(baseException.failure.message, baseException)
        throw baseException
      case apiException: ApiException => logger.error(apiException.getMessage, apiException)
        throw new BaseException(constants.Response.ENVELOPE_CREATION_FAILED)
      case clientHandlerException: ClientHandlerException => logger.error(clientHandlerException.getMessage, clientHandlerException)
        throw new BaseException(constants.Response.INVALID_INPUT)
    }
  }

  def createEnvelope(emailList: Seq[Email], fileList: Seq[Document[_]], traderList: Seq[Trader]): Future[String] = createDocusignEnvelope(emailList, fileList, traderList)

  def createSenderViewURL(envelopeID: String): Future[String] = generateSenderViewURL(envelopeID)

  private def generateSenderViewURL(envelopeID: String): Future[String] = {
    val oauthToken = docusignOAuthTokens.Service.tryGet(accountID)
    (for {
      oauthToken <- oauthToken
    } yield {
      apiClient.setAccessToken(oauthToken.accessToken, (oauthToken.expiresAt - System.currentTimeMillis()) / 1000.toLong)
      val viewRequest = new CallBackURLRequest
      viewRequest.setReturnUrl(comdexURL + routes.DocusignController.callBack("", "").url.split("""\?""")(0))
      envelopesApi.createSenderView(accountID, envelopeID, viewRequest).getUrl
    }).recover {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
      case apiException: ApiException => logger.error(apiException.getMessage, apiException)
        throw new BaseException(constants.Response.SENDER_VIEW_CREATION_FAILED)
      case clientHandlerException: ClientHandlerException => logger.error(clientHandlerException.getMessage, clientHandlerException)
        throw new BaseException(constants.Response.INVALID_INPUT)
    }
  }

  private def createRecipientViewAndGetUrl(envelopeID: String, emailAddress: String, trader: Trader): Future[String] = {
    val oauthToken = docusignOAuthTokens.Service.tryGet(accountID)
    (for {
      oauthToken <- oauthToken
    } yield {
      apiClient.setAccessToken(oauthToken.accessToken, (oauthToken.expiresAt - System.currentTimeMillis()) / 1000.toLong)
      val viewRequest = new RecipientViewRequest()
      viewRequest.setReturnUrl(comdexURL + routes.DocusignController.callBack(envelopeID, "").url.split("""&""")(0))
      viewRequest.setAuthenticationMethod(authenticationMethod)
      viewRequest.setEmail(emailAddress)
      viewRequest.setUserName(trader.accountID)
      viewRequest.setClientUserId(trader.id)

      envelopesApi.createRecipientView(accountID, envelopeID, viewRequest).getUrl
    }).recover {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
      case apiException: ApiException => logger.error(apiException.getMessage, apiException)
        throw new BaseException(constants.Response.RECEPIENT_VIEW_CREATION_FAILED)
      case clientHandlerException: ClientHandlerException => logger.error(clientHandlerException.getMessage, clientHandlerException)
        throw new BaseException(constants.Response.INVALID_INPUT)
    }
  }

  def createRecipientView(envelopeID: String, emailAddress: String, trader: Trader): Future[String] = createRecipientViewAndGetUrl(envelopeID, emailAddress, trader)

  def updateSignedDocumentList(envelopeID: String, documentTypeList: Seq[String]): Future[Seq[String]] = fetchAndStoreSignedDocumentList(envelopeID, documentTypeList)

  def fetchAndStoreSignedDocumentList(envelopeID: String, documentTypeList: Seq[String]) = {
    val oauthToken = docusignOAuthTokens.Service.tryGet(accountID)
    (for {
      oauthToken <- oauthToken
    } yield {
      apiClient.setAccessToken(oauthToken.accessToken, (oauthToken.expiresAt - System.currentTimeMillis()) / 1000.toLong)
      documentTypeList.zipWithIndex.map { case (documentType, index) =>
        val fileByteArray = envelopesApi.getDocument(accountID, envelopeID, (index + 1).toString)
        val newFileName = List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(fileByteArray)).toString, constants.File.PDF).mkString(".")
        val file = utilities.FileOperations.newFile(fileResourceManager.getNegotiationFilePath(documentType), newFileName)
        val bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file))
        bufferedOutputStream.write(fileByteArray)
        bufferedOutputStream.close()
        newFileName
      }
    }).recover {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
      case apiException: ApiException => logger.error(apiException.getMessage, apiException)
        throw new BaseException(constants.Response.FAILED_TO_FETCH_SIGNED_DOCUMENT)
      case clientHandlerException: ClientHandlerException => logger.error(clientHandlerException.getMessage, clientHandlerException)
        throw new BaseException(constants.Response.INVALID_INPUT)
    }
  }

  def updateAccessToken(code: String): Future[Unit] = {
    try {
      generateAndUpdateAccessToken(code)
    } catch {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
    }
  }

  private def generateAndUpdateAccessToken(code: String): Future[Unit] = {
    val docusignIntegrationKey = Future(keyStore.getPassphrase("docusignIntegrationKey"))
    val docusignClientSecret = Future(keyStore.getPassphrase("docusignClientSecret"))
    val oauthToken = docusignOAuthTokens.Service.get(accountID)

    def response(docusignIntegrationKey: String, docusignClientSecret: String) = Future(apiClient.generateAccessToken(docusignIntegrationKey, docusignClientSecret, code))

    def updateOauthToken(response: OAuthToken, oauthToken: Option[DocusignOAuthToken]) = oauthToken match {
      case Some(value) => docusignOAuthTokens.Service.update(accountID, response.getAccessToken, System.currentTimeMillis() + response.getExpiresIn * 1000, response.getRefreshToken)
      case None => docusignOAuthTokens.Service.create(accountID, response.getAccessToken, System.currentTimeMillis() + response.getExpiresIn * 1000, response.getRefreshToken)
    }

    (for {
      docusignIntegrationKey <- docusignIntegrationKey
      docusignClientSecret <- docusignClientSecret
      response <- response(docusignIntegrationKey, docusignClientSecret)
      oauthToken <- oauthToken
      _ <- updateOauthToken(response, oauthToken)
    } yield apiClient.setAccessToken(response.getAccessToken, response.getExpiresIn)
      ).recover {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
      case apiException: ApiException => logger.error(apiException.getMessage, apiException)
        throw new BaseException(constants.Response.ACCESS_TOKEN_GENERATION_FAILED)
      case clientHandlerException: ClientHandlerException => logger.error(clientHandlerException.getMessage, clientHandlerException)
        throw new BaseException(constants.Response.INVALID_INPUT)
    }
  }

  def getAuthorizationURI: String = {
    try {
      fetchAuthorizationURI
    } catch {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
    }
  }

  private def fetchAuthorizationURI: String = {
    try {
      val integrationKey = keyStore.getPassphrase("docusignIntegrationKey")
      apiClient.getAuthorizationUri(integrationKey, Arrays.asList(constants.External.Docusign.SIGNATURE_SCOPE), comdexURL + routes.DocusignController.authorizationCallBack("").url.split("""\?""")(0), constants.External.Docusign.CODE).toString
    } catch {
      case clientHandlerException: ClientHandlerException => logger.error(clientHandlerException.getMessage, clientHandlerException)
        throw new BaseException(constants.Response.INVALID_INPUT)
    }
  }
}
