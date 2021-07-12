package transactions.wallex

import exceptions.BaseException
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import transactions.responses.WallexResponse.{CreateDocumentResponse, DeleteKYCResponse}
import utilities.KeyStore

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteDocument @Inject()(
    wsClient: WSClient,
    keyStore: KeyStore
)(implicit
    configuration: Configuration,
    executionContext: ExecutionContext
) {

  private implicit val module: String =
    constants.Module.TRANSACTIONS_WALLEX_DELETE_DOCUMENT

  private implicit val logger: Logger = Logger(this.getClass)

  private val apiKeyHeaderName =
    configuration.get[String]("wallex.apiKeyHeaderName")

  private val apiKeyHeaderValue =
    keyStore.getPassphrase(constants.KeyStore.WALLEX_API_HEADER_VALUE)

  private val apiKeyHeader = Tuple2(apiKeyHeaderName, apiKeyHeaderValue)

  private val baseURL = configuration.get[String]("wallex.url")

  private val endpoint =
    configuration.get[String]("wallex.endpoints.deleteDocument")

  val apiTokenHeaderName =
    configuration.get[String]("wallex.apiTokenHeaderName")

  private val url = baseURL + endpoint

  private def action(
      authToken: String,
      wallexID: String,
      fileID: String
  ) : Future[DeleteKYCResponse] = {

    val authTokenHeader = Tuple2(apiTokenHeaderName, authToken)

    utilities.JSON
      .getResponseFromJson[DeleteKYCResponse](
        wsClient
          .url(url.replace("userId", wallexID).replace("documentId",fileID))
          .withHttpHeaders(apiKeyHeader, authTokenHeader)
          .delete()
      )
      .recover {
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
      documentType: String,
      documentName: String
  ) extends BaseRequest

  object Service {

    def delete(authToken: String, wallexID: String, fileID: String) =
      action(authToken, wallexID, fileID).recover {
        case connectException: ConnectException =>
          logger.error(
            constants.Response.CONNECT_EXCEPTION.message,
            connectException
          )
          throw new BaseException(constants.Response.CONNECT_EXCEPTION)
      }
  }

}
