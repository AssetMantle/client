package transactions.wallex

import exceptions.BaseException
import play.api.libs.json.{Json, OWrites, Reads}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import transactions.responses.WallexResponse.CreateSimplePaymentResponse
import utilities.KeyStore

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FundSimplePayment @Inject()(
    wsClient: WSClient,
    keyStore: KeyStore
)(implicit
    configuration: Configuration,
    executionContext: ExecutionContext
) {

  private implicit val module: String =
    constants.Module.TRANSACTIONS_WALLEX_CREATE_BENEFICIARY

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
    configuration.get[String]("wallex.endpoints.simplePaymentFund")

  private val url = baseURL + endpoint

  private def action(
      request: Request,
      authToken: String
  ): Future[CreateSimplePaymentResponse] =
    utilities.JSON.getResponseFromJson[CreateSimplePaymentResponse] {
      val authTokenHeader = Tuple2(apiTokenHeaderName, authToken)

      wsClient
        .url(url)
        .withHttpHeaders(apiKeyHeader, authTokenHeader)
        .post(Json.toJson(request))
    }

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]
  implicit val requestReads: Reads[Request] = Json.reads[Request]
  case class Request(fundingId: String,resource: String,status: String) extends BaseRequest

  object Service {

    def post(
        authToken: String,
        request: Request
    ): Future[CreateSimplePaymentResponse] =
      action(request, authToken).recover {
        case connectException: ConnectException =>
          logger.error(
            constants.Response.CONNECT_EXCEPTION.message,
            connectException
          )
          throw new BaseException(constants.Response.CONNECT_EXCEPTION)
      }
  }

}
