package utilities

import java.io.{BufferedOutputStream, FileOutputStream}
import java.util.Arrays

import com.docusign.esign.api.EnvelopesApi
import com.docusign.esign.client.{ApiClient, ApiException}
import com.docusign.esign.client.auth.OAuth.OAuthToken
import com.docusign.esign.model.{EnvelopeDefinition, RecipientViewRequest, Recipients, Signer, Document => DocusignDocument, ReturnUrlRequest => CallBackURLRequest}
import com.sun.jersey.api.client.ClientHandlerException
import com.sun.jersey.core.util.{Base64 => Base64Docusign}
import controllers.routes
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Document
import models.docusign
import models.docusign.{OAuthToken => DocusignOAuthToken}
import models.master.{Email, Account}
import org.apache.commons.codec.binary.Base64
import play.api.{Configuration, Logger}

import scala.collection.JavaConverters
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Docusign @Inject()(fileResourceManager: utilities.FileResourceManager,
                         docusignOAuthTokens: docusign.OAuthTokens,
                         keyStore: KeyStore
                        )
                        (implicit
                         executionContext: ExecutionContext,
                         configuration: Configuration
                        ) {

  private implicit val module: String = constants.Module.UTILITIES_DOCUSIGN

  private implicit val logger: Logger = Logger(this.getClass)

  private val accountID = keyStore.getPassphrase(constants.KeyStore.DOCUSIGN_ACCOUNT_ID)
  private val authenticationMethod = configuration.get[String]("docusign.authenticationMethod")
  private val basePath = configuration.get[String]("docusign.basePath")
  private val integrationKey = keyStore.getPassphrase(constants.KeyStore.DOCUSIGN_INTEGRATION_KEY)
  private val clientSecret = keyStore.getPassphrase(constants.KeyStore.DOCUSIGN_CLIENT_SECRET)
  private val webAppURL = configuration.get[String]("webApp.url")
  private val apiClient = new ApiClient(basePath)
  private val envelopesApi = new EnvelopesApi(apiClient)

  private def createDocusignEnvelope(emailList: Seq[Email], fileList: Seq[Document[_]], accountList: Seq[Account]): Future[String] = {
    val oauthToken = docusignOAuthTokens.Service.tryGet(accountID)
    (for {
      oauthToken <- oauthToken
    } yield {
      apiClient.setAccessToken(oauthToken.accessToken, (oauthToken.expiresAt - System.currentTimeMillis()) / 1000.toLong)

      val documentList = fileList.zipWithIndex.map { case (file, index) =>
        val document = new DocusignDocument()
        document.setDocumentBase64(new String(Base64Docusign.encode(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile(fileResourceManager.getAccountKYCFilePath(file.documentType), file.fileName)))))
        document.setName(file.fileName.split("""\.""")(0))
        document.setFileExtension(utilities.FileOperations.fileExtensionFromName(file.fileName))
        document.setDocumentId((index + 1).toString)
        document
      }

      val signerList = accountList.zipWithIndex.map { case (account, index) =>
        val signer = new Signer()
        signer.setEmail(emailList.find(_.id == account.id).map(_.emailAddress).getOrElse(""))
        signer.setName(account.id)
        signer.clientUserId(account.address)
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

  def createEnvelope(emailList: Seq[Email], fileList: Seq[Document[_]], accountList: Seq[Account]): Future[String] = createDocusignEnvelope(emailList, fileList, accountList)

  def createSenderViewURL(envelopeID: String): Future[String] = generateSenderViewURL(envelopeID)

  private def generateSenderViewURL(envelopeID: String): Future[String] = {
    val oauthToken = docusignOAuthTokens.Service.tryGet(accountID)
    (for {
      oauthToken <- oauthToken
    } yield {
      apiClient.setAccessToken(oauthToken.accessToken, (oauthToken.expiresAt - System.currentTimeMillis()) / 1000.toLong)
      val viewRequest = new CallBackURLRequest
      viewRequest.setReturnUrl(webAppURL + routes.DocusignController.callBack("", "").url.split("""\?""")(0))
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

  private def createRecipientViewAndGetUrl(envelopeID: String, emailAddress: String, account: Account): Future[String] = {
    val oauthToken = docusignOAuthTokens.Service.tryGet(accountID)
    (for {
      oauthToken <- oauthToken
    } yield {
      apiClient.setAccessToken(oauthToken.accessToken, (oauthToken.expiresAt - System.currentTimeMillis()) / 1000.toLong)
      val viewRequest = new RecipientViewRequest()
      viewRequest.setReturnUrl(webAppURL + routes.DocusignController.callBack(envelopeID, "").url.split("""&""")(0))
      viewRequest.setAuthenticationMethod(authenticationMethod)
      viewRequest.setEmail(emailAddress)
      viewRequest.setUserName(account.id)
      viewRequest.setClientUserId(account.address)

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

  def createRecipientView(envelopeID: String, emailAddress: String, account: Account): Future[String] = createRecipientViewAndGetUrl(envelopeID, emailAddress, account)

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
        val file = utilities.FileOperations.newFile(fileResourceManager.getAccountKYCFilePath(documentType), newFileName)
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
    val response = Future(apiClient.generateAccessToken(integrationKey, clientSecret, code))
    val oauthToken = docusignOAuthTokens.Service.get(accountID)

    def updateOauthToken(response: OAuthToken, oauthToken: Option[DocusignOAuthToken]) = oauthToken match {
      case Some(value) => docusignOAuthTokens.Service.update(accountID, response.getAccessToken, System.currentTimeMillis() + response.getExpiresIn * 1000, response.getRefreshToken)
      case None => docusignOAuthTokens.Service.create(accountID, response.getAccessToken, System.currentTimeMillis() + response.getExpiresIn * 1000, response.getRefreshToken)
    }

    (for {
      response <- response
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
      apiClient.getAuthorizationUri(integrationKey, Arrays.asList(constants.External.Docusign.SIGNATURE_SCOPE), webAppURL + routes.DocusignController.authorizationCallBack("").url.split("""\?""")(0), constants.External.Docusign.CODE).toString
    } catch {
      case clientHandlerException: ClientHandlerException => logger.error(clientHandlerException.getMessage, clientHandlerException)
        throw new BaseException(constants.Response.INVALID_INPUT)
    }
  }
}
