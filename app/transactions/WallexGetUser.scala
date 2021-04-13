package transactions

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.responses.WallexResponse.GetUserResponse
import utilities.{KeyStore, WallexAuthToken}

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WallexGetUser @Inject() (
    wsClient: WSClient,
    keyStore: KeyStore,
    wallexAuthToken: WallexAuthToken
)(implicit
    configuration: Configuration,
    executionContext: ExecutionContext
) {

  private implicit val module: String =
    constants.Module.TRANSACTIONS_WALLEX_GET_USER

  private implicit val logger: Logger = Logger(this.getClass)

  private val apiKeyHeaderName =
    configuration.get[String]("wallex.apiKeyHeaderName")

  private val apiKeyHeaderValue = keyStore.getPassphrase(constants.KeyStore.WALLEX_API_HEADER_VALUE)

  private val apiKeyHeader = Tuple2(apiKeyHeaderName, apiKeyHeaderValue)

  private val apiTokenHeaderName =
    configuration.get[String]("wallex.apiTokenHeaderName")

  private val baseURL = configuration.get[String]("wallex.url")

  private val endpoint =
    configuration.get[String]("wallex.endpoints.getUser")

  private val url = baseURL + endpoint

  private def action(
      userId: String,
      authToken: String
  ): Future[GetUserResponse] = {
    val authTokenHeader = Tuple2(apiTokenHeaderName, authToken)
    utilities.JSON.getResponseFromJson[GetUserResponse](
      wsClient
        .url(url + userId)
        .withHttpHeaders(apiKeyHeader, authTokenHeader)
        .get()
    )
  }

  case class Request()

  object Service {

    def get(userId: String, authToken: String): Future[GetUserResponse] =
      action(userId, authToken).recover {
        case connectException: ConnectException =>
          logger.error(
            constants.Response.CONNECT_EXCEPTION.message,
            connectException
          )
          throw new BaseException(constants.Response.CONNECT_EXCEPTION)
      }
  }

}
