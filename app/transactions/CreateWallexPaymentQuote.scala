package transactions

import exceptions.BaseException
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import transactions.responses.WallexResponse.CreatePaymentQuoteResponse
import utilities.KeyStore

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateWallexPaymentQuote @Inject() (
    wsClient: WSClient,
    keyStore: KeyStore
)(implicit
    configuration: Configuration,
    executionContext: ExecutionContext
) {

  private implicit val module: String =
    constants.Module.TRANSACTIONS_WALLEX_CREATE_PAYMENT_QUOTE

  private implicit val logger: Logger = Logger(this.getClass)

  private val apiKeyHeaderName =
    configuration.get[String]("wallex.apiKeyHeaderName")

  private val apiKeyHeaderValue =
    keyStore.getPassphrase(constants.KeyStore.WALLEX_API_HEADER_VALUE)

  private val apiKeyHeader = Tuple2(apiKeyHeaderName, apiKeyHeaderValue)

  private val baseURL = configuration.get[String]("wallex.url")

  private val endpoint =
    configuration.get[String]("wallex.endpoints.createPaymentQuote")

  private val url = baseURL + endpoint

  private def action(
      authToken: String,
      request: Request
  ): Future[CreatePaymentQuoteResponse] = {

    val apiTokenHeaderName =
      configuration.get[String]("wallex.apiTokenHeaderName")

    val authTokenHeader = Tuple2(apiTokenHeaderName, authToken)

    utilities.JSON.getResponseFromJson[CreatePaymentQuoteResponse](
      wsClient
        .url(url)
        .withHttpHeaders(apiKeyHeader, authTokenHeader)
        .post(Json.toJson(request))
    )
  }

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class Request(
      sellCurrency: String,
      buyCurrency: String,
      amount: Double,
      beneficiaryId: String
  ) extends BaseRequest

  object Service {

    def post(
        authToken: String,
        request: Request
    ): Future[CreatePaymentQuoteResponse] =
      action(authToken, request).recover {
        case connectException: ConnectException =>
          logger.error(
            constants.Response.CONNECT_EXCEPTION.message,
            connectException
          )
          throw new BaseException(constants.Response.CONNECT_EXCEPTION)
      }
  }

}
