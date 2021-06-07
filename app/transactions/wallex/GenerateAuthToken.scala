package transactions.wallex

import exceptions.BaseException
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.WallexAuthTokenResponse.Response
import transactions.Abstract.BaseRequest
import utilities.KeyStore

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GenerateAuthToken @Inject()(wsClient: WSClient, keyStore: KeyStore)(
    implicit
    configuration: Configuration,
    executionContext: ExecutionContext
) {

  private implicit val module: String =
    constants.Module.TRANSACTIONS_WALLEX_AUTH_TOKEN

  private implicit val logger: Logger = Logger(this.getClass)

  private val apiKeyHeaderName =
    configuration.get[String]("wallex.apiKeyHeaderName")

  private val apiKeyHeaderValue = keyStore.getPassphrase(constants.KeyStore.WALLEX_API_HEADER_VALUE)

  private val apiKeyHeader = Tuple2(apiKeyHeaderName, apiKeyHeaderValue)

  private val baseURL = configuration.get[String]("wallex.url")

  private val endpoint =
    configuration.get[String]("wallex.endpoints.authenticateApi")

  private val url = baseURL + endpoint

  private def action(request: Request): Future[Response] =
    utilities.JSON.getResponseFromJson[Response](
      wsClient.url(url).withHttpHeaders(apiKeyHeader).post(Json.toJson(request))
    )

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class Request(accessKeyId: String, secretAccessKey: String)
      extends BaseRequest

  object Service {

    def post(request: Request): Future[Response] =
      action(request).recover {
        case connectException: ConnectException =>
          logger.error(
            constants.Response.CONNECT_EXCEPTION.message,
            connectException
          )
          throw new BaseException(constants.Response.CONNECT_EXCEPTION)
      }

    def getToken(): Future[String] = {
      val accessKeyId = keyStore.getPassphrase(constants.KeyStore.WALLEX_ACCESS_KEY_VALUE)
      val secretAccessKey = keyStore.getPassphrase(constants.KeyStore.WALLEX_SECRET_ACCESS_VALUE)

      val tokenResponse = post(
        Request(accessKeyId = accessKeyId, secretAccessKey = secretAccessKey)
      )
      (for {
        tokenResponse <- tokenResponse
      } yield tokenResponse.token)
    }

  }
}