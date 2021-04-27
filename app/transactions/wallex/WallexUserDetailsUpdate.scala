package transactions.wallex

import exceptions.BaseException
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import transactions.responses.WallexResponse.UpdateUserDetailsResponse
import utilities.KeyStore

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WallexUserDetailsUpdate @Inject() (
    wsClient: WSClient,
    keyStore: KeyStore
)(implicit
    configuration: Configuration,
    executionContext: ExecutionContext
) {

  private implicit val module: String =
    constants.Module.TRANSACTIONS_WALLEX_DETAILS_UPDATE

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
    configuration.get[String]("wallex.endpoints.updateUserDetails")

  private val url = baseURL + endpoint

  private def action(
      request: Request,
      authToken: String,
      userId: String
  ): Future[UpdateUserDetailsResponse] = {
    val authTokenHeader = Tuple2(apiTokenHeaderName, authToken)
    utilities.JSON
      .getResponseFromJson[UpdateUserDetailsResponse](
        wsClient
          .url(url.replace("userId", userId))
          .withHttpHeaders(apiKeyHeader, authTokenHeader)
          .patch(Json.toJson(request))
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
      mobileCountryCode: String,
      mobileNumber: String,
      gender: String,
      countryOfBirth: String,
      nationality: String,
      countryOfResidence: String,
      residentialAddress: String,
      countryCode: String,
      postalCode: String,
      dateOfBirth: String,
      identificationType: String,
      identificationNumber: String,
      issueDate: String,
      expiryDate: String
  ) extends BaseRequest

  object Service {

    def post(
        authToken: String,
        request: Request,
        userId: String
    ): Future[UpdateUserDetailsResponse] =
      action(request, authToken, userId).recover {
        case connectException: ConnectException =>
          logger.error(
            constants.Response.CONNECT_EXCEPTION.message,
            connectException
          )
          throw new BaseException(constants.Response.CONNECT_EXCEPTION)
      }
  }

}
