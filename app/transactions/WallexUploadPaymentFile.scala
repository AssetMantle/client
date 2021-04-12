package transactions

import exceptions.BaseException
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import transactions.responses.WallexResponse.PaymentFileUploadResponse
import utilities.KeyStore

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WallexUploadPaymentFile @Inject() (
    wsClient: WSClient,
    keyStore: KeyStore
)(implicit
    configuration: Configuration,
    executionContext: ExecutionContext
) {

  private implicit val module: String =
    constants.Module.TRANSACTIONS_WALLEX_PAYMENT_URL

  private implicit val logger: Logger = Logger(this.getClass)

  private val apiKeyHeaderName =
    configuration.get[String]("wallex.apiKeyHeaderName")

  private val apiKeyHeaderValue =
    keyStore.getPassphrase(constants.KeyStore.WALLEX_API_HEADER_VALUE)

  private val apiKeyHeader = Tuple2(apiKeyHeaderName, apiKeyHeaderValue)

  private val baseURL = configuration.get[String]("wallex.url")

  private val endpoint =
    configuration.get[String]("wallex.endpoints.paymentFileUpload")

  private val url = baseURL + endpoint

  private val storageClassHeaderName =
    configuration.get[String]("wallex.storageClassHeader")

  private val storageClassValue = "STANDARD"
  private val storageHeader = Tuple2(storageClassHeaderName, storageClassValue)

  private val contentTypeHeaderName =
    configuration.get[String]("wallex.contentType")

  private val contentTypeHeaderValue = "application/pdf"

  private val contentTypeHeader = Tuple2(contentTypeHeaderName, contentTypeHeaderValue)



  private def action(
      authToken: String,
      request: Request
  ): Future[PaymentFileUploadResponse] = {

    val apiTokenHeaderName =
      configuration.get[String]("wallex.apiTokenHeaderName")

    val authTokenHeader = Tuple2(apiTokenHeaderName, authToken)

    utilities.JSON.getResponseFromJson[PaymentFileUploadResponse](
      wsClient
        .url(url)
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
        .withHttpHeaders(storageHeader, contentTypeHeader)
        .put(file)
      )
  }

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class Request(
      fileName: String
  ) extends BaseRequest

  object Service {

    def post(
        authToken: String,
        request: Request
    ): Future[PaymentFileUploadResponse] =
      action(authToken, request).recover {
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
