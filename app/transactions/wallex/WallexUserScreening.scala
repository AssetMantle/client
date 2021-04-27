package transactions.wallex

import exceptions.BaseException
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import transactions.responses.WallexResponse.ScreeningResponse
import utilities.KeyStore

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WallexUserScreening @Inject() (
    wsClient: WSClient,
    keyStore: KeyStore
)(implicit
    configuration: Configuration,
    executionContext: ExecutionContext
) {

  private implicit val module: String =
    constants.Module.TRANSACTIONS_WALLEX_USER_SCREENING

  private implicit val logger: Logger = Logger(this.getClass)

  private val apiKeyHeaderName =
    configuration.get[String]("wallex.apiKeyHeaderName")

  private val apiKeyHeaderValue =
    keyStore.getPassphrase(constants.KeyStore.WALLEX_API_HEADER_VALUE)

  private val apiKeyHeader = Tuple2(apiKeyHeaderName, apiKeyHeaderValue)

  private val apiTokenHeaderName =
    configuration.get[String]("wallex.apiTokenHeaderName")

  private val baseURL = configuration.get[String]("wallex.url")

  private val endpoint =
    configuration.get[String]("wallex.endpoints.kycScreening")

  private val url = baseURL + endpoint

  private def action(
      userID: String,
      authToken: String,
      request: Request
  ): Future[ScreeningResponse] = {
    val authTokenHeader = Tuple2(apiTokenHeaderName, authToken)

    utilities.JSON
      .getResponseFromJson[ScreeningResponse](
        wsClient
          .url(url.replace("userId", userID))
          .withHttpHeaders(apiKeyHeader, authTokenHeader)
          .post(Json.toJson(request))
      ).recover {
        case baseException: BaseException =>
          logger.error(
            constants.Response.WALLEX_EXCEPTION.message,
            baseException
          )
          throw new BaseException(constants.Response.WALLEX_EXCEPTION)
      }
  }

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class Request(
      userId: String
  ) extends BaseRequest

  object Service {

    def post(
        userID: String,
        authToken: String,
        request: Request
    ): Future[ScreeningResponse] =
      action(userID, authToken, request).recover {
        case connectException: ConnectException =>
          logger.error(
            constants.Response.CONNECT_EXCEPTION.message,
            connectException
          )
          throw new BaseException(constants.Response.CONNECT_EXCEPTION)
      }
  }

}
