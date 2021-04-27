package transactions.wallex

import exceptions.BaseException
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import transactions.responses.WallexResponse.WalletToWalletXferResponse
import utilities.KeyStore

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WallexWalletTransfer @Inject() (
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
    configuration.get[String]("wallex.endpoints.createTransfer")

  private val url = baseURL + endpoint

  private def action(
      request: Request,
      authToken: String
  ): Future[WalletToWalletXferResponse] = {
    val authTokenHeader = Tuple2(apiTokenHeaderName, authToken)
    utilities.JSON
      .getResponseFromJson[WalletToWalletXferResponse](
        wsClient
          .url(url)
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
      onBehalfOf: String,
      receiverAccountId: String,
      amount: Double,
      currency: String,
      purposesOfTransfer: String,
      reference: String,
      remarks: String,
      supportingDocuments: Seq[String]
  ) extends BaseRequest

  object Service {

    def post(
        authToken: String,
        request: Request
    ): Future[WalletToWalletXferResponse] =
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
