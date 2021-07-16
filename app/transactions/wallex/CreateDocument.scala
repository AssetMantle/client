package transactions.wallex

import exceptions.BaseException
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import transactions.responses.WallexResponse.{CreateDocumentResponse, DeleteKYCResponse}
import utilities.KeyStore

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateDocument @Inject()(
    wsClient: WSClient,
    keyStore: KeyStore
)(implicit
    configuration: Configuration,
    executionContext: ExecutionContext
) {

  private implicit val module: String =
    constants.Module.TRANSACTIONS_WALLEX_CREATE_DOCUMENT

  private implicit val logger: Logger = Logger(this.getClass)

  private val apiKeyHeaderName =
    configuration.get[String]("wallex.apiKeyHeaderName")

  private val apiKeyHeaderValue =
    keyStore.getPassphrase(constants.KeyStore.WALLEX_API_HEADER_VALUE)

  private val apiKeyHeader = Tuple2(apiKeyHeaderName, apiKeyHeaderValue)

  private val storageClassHeaderName =
    configuration.get[String]("wallex.storageClassHeader")

  private val storageClassValue = constants.External.Wallex.STANDARD
  private val storageHeader = Tuple2(storageClassHeaderName, storageClassValue)

  private val contentTypeHeaderName =
    configuration.get[String]("wallex.contentType")

  private val contentTypeHeaderValue = constants.External.Wallex.APPLICATION_TYPE

  private val contentTypeHeader = Tuple2(contentTypeHeaderName, contentTypeHeaderValue)

  private val baseURL = configuration.get[String]("wallex.url")

  private val endpoint =
    configuration.get[String]("wallex.endpoints.createDocument")

  val apiTokenHeaderName =
    configuration.get[String]("wallex.apiTokenHeaderName")

  private val url = baseURL + endpoint

  private def action(
      authToken: String,
      wallexUserID: String,
      request: Request
  ): Future[CreateDocumentResponse] = {

    val authTokenHeader = Tuple2(apiTokenHeaderName, authToken)

    utilities.JSON
      .getResponseFromJson[CreateDocumentResponse](
        wsClient
          .url(url.replace("userId", wallexUserID))
          .withHttpHeaders(apiKeyHeader, authTokenHeader)
          .post(Json.toJson(request))
      )
      .recover {
        case baseException: BaseException =>
          logger.error(
            constants.Response.WALLEX_EXCEPTION.message,
            baseException
          )
          throw new BaseException(constants.Response.WALLEX_EXCEPTION)
      }
  }

  private def putAction(
      documentUrl: String,
      file: Array[Byte]
  ) = {
    val apiTokenHeaderName =
      configuration.get[String]("wallex.apiTokenHeaderName")

      wsClient
        .url(documentUrl)
        .withHttpHeaders(storageHeader, contentTypeHeader)
        .put(file)
  }

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class Request(
      documentType: String,
      documentName: String
  ) extends BaseRequest

  object Service {

    def post(
        authToken: String,
        wallexUserID: String,
        request: Request
    ): Future[CreateDocumentResponse] =
      action(authToken, wallexUserID, request).recover {
        case connectException: ConnectException =>
          logger.error(
            constants.Response.CONNECT_EXCEPTION.message,
            connectException
          )
          throw new BaseException(constants.Response.CONNECT_EXCEPTION)
      }

    def put(documentUrl: String, file: Array[Byte]) =
      putAction(documentUrl, file).recover {
        case connectException: ConnectException =>
          logger.error(
            constants.Response.CONNECT_EXCEPTION.message,
            connectException
          )
          throw new BaseException(constants.Response.CONNECT_EXCEPTION)
      }
  }

}
