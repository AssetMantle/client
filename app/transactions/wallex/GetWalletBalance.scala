package transactions.wallex

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.responses.WallexResponse.GetBalanceResponse
import utilities.KeyStore

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetWalletBalance @Inject()(
    wsClient: WSClient,
    keyStore: KeyStore,
)(implicit
    configuration: Configuration,
    executionContext: ExecutionContext
) {

  private implicit val module: String =
    constants.Module.TRANSACTIONS_WALLEX_WALLET_BALANCE

  private implicit val logger: Logger = Logger(this.getClass)

  private val apiKeyHeaderName =
    configuration.get[String]("wallex.apiKeyHeaderName")

  private val apiKeyHeaderValue = keyStore.getPassphrase(constants.KeyStore.WALLEX_API_HEADER_VALUE)

  private val apiKeyHeader = Tuple2(apiKeyHeaderName, apiKeyHeaderValue)

  private val apiTokenHeaderName =
    configuration.get[String]("wallex.apiTokenHeaderName")

  private val userIdHeaderName =
    configuration.get[String]("wallex.userIdHeaderName")

  private val baseURL = configuration.get[String]("wallex.url")

  private val endpoint =
    configuration.get[String]("wallex.endpoints.getBalance")

  private val url = baseURL + endpoint

  private def action(
      authToken: String,
      userId:String
  ): Future[GetBalanceResponse] = {
    val authTokenHeader = Tuple2(apiTokenHeaderName, authToken)
    val userIdHeader = Tuple2(userIdHeaderName, userId)

    utilities.JSON.getResponseFromJson[GetBalanceResponse](
      wsClient
        .url(url)
        .withHttpHeaders(apiKeyHeader, authTokenHeader,userIdHeader)
        .get()
    )
  }

  case class Request()

  object Service {

    def post(authToken: String,userId: String): Future[GetBalanceResponse] =
      action(authToken,userId).recover {
        case connectException: ConnectException =>
          logger.error(
            constants.Response.CONNECT_EXCEPTION.message,
            connectException
          )
          throw new BaseException(constants.Response.CONNECT_EXCEPTION)
      }
  }

}