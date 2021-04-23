package transactions.wallex

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.responses.WallexResponse.DeleteBeneficiaryResponse
import utilities.KeyStore

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WallexDeleteBeneficiary @Inject() (
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
    configuration.get[String]("wallex.endpoints.deleteBeneficiary")

  private val url = baseURL + endpoint

  private def action(
      authToken: String,
      beneficiaryId: String
  ): Future[DeleteBeneficiaryResponse] = {
    val authTokenHeader = Tuple2(apiTokenHeaderName, authToken)
    utilities.JSON.getResponseFromJson[DeleteBeneficiaryResponse](
      wsClient
        .url(url + beneficiaryId)
        .withHttpHeaders(apiKeyHeader, authTokenHeader)
        .delete()
    )
  }

  object Service {

    def delete(
        authToken: String,
        beneficiaryId: String
    ): Future[DeleteBeneficiaryResponse] =
      action(authToken, beneficiaryId).recover {
        case connectException: ConnectException =>
          logger.error(
            constants.Response.CONNECT_EXCEPTION.message,
            connectException
          )
          throw new BaseException(constants.Response.CONNECT_EXCEPTION)
      }
  }

}
