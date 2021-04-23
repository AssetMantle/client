package transactions.wallex

import exceptions.BaseException
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import transactions.responses.WallexResponse.CreateDocumentResponse
import utilities.KeyStore

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WallexCreateDocument @Inject() (
    wsClient: WSClient,
    keyStore: KeyStore,
    wallexAuthToken: WallexAuthToken
)(implicit
    configuration: Configuration,
    executionContext: ExecutionContext
) {

  private implicit val module: String =
    constants.Module.TRANSACTIONS_WALLEX_USER_SIGNUP

  private implicit val logger: Logger = Logger(this.getClass)

  private val apiKeyHeaderName =
    configuration.get[String]("wallex.apiKeyHeaderName")

  private val apiKeyHeaderValue = keyStore.getPassphrase(constants.KeyStore.WALLEX_API_HEADER_VALUE)

  private val apiKeyHeader = Tuple2(apiKeyHeaderName, apiKeyHeaderValue)

  private val baseURL = configuration.get[String]("wallex.url")

  private val endpoint =
    configuration.get[String]("wallex.endpoints.createDocument")

  private val url = baseURL + endpoint

  private def action(
      authToken: String,
      wallexUserID:String,
      request: Request
  ): Future[CreateDocumentResponse] = {

    val apiTokenHeaderName =
      configuration.get[String]("wallex.apiTokenHeaderName")

    val authTokenHeader = Tuple2(apiTokenHeaderName, authToken)

    utilities.JSON.getResponseFromJson[CreateDocumentResponse](
      wsClient
        .url(url.replace("userId",wallexUserID))
        .withHttpHeaders(apiKeyHeader, authTokenHeader)
        .post(Json.toJson(request))
    )
  }

  private def putAction(
      authToken: String,
      docUrl: String,
      file: Array[Byte]
  ) = {
    val apiTokenHeaderName =
      configuration.get[String]("wallex.apiTokenHeaderName")

    val authTokenHeader = Tuple2(apiTokenHeaderName, authToken)
    (
      wsClient
        .url(docUrl)
        .withHttpHeaders(apiKeyHeader, authTokenHeader)
        .put(file)
      )
  }

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class Request(
      documentType: String,
      documentName: String
  ) extends BaseRequest

  object Service {

    def post(
        authToken: String,
        wallexUserID:String,
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

    def put(authToken: String, docUrl: String, file: Array[Byte]) =
      putAction(authToken, docUrl, file).recover {
        case connectException: ConnectException =>
          logger.error(
            constants.Response.CONNECT_EXCEPTION.message,
            connectException
          )
          throw new BaseException(constants.Response.CONNECT_EXCEPTION)
      }
  }

}
