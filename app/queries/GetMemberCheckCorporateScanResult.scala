package queries

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.MemberCheckCorporateScanResultResponse.Response
import services.KeyStore

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetMemberCheckCorporateScanResult @Inject()(wsClient: WSClient, keyStore: KeyStore)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_MEMBER_CHECK_CORPORATE_SCAN_RESULT

  private implicit val logger: Logger = Logger(this.getClass)

  private val organizationHeaderName = configuration.get[String]("memberCheck.organizationHeaderName")

  private val organizationHeaderValue = configuration.get[String]("memberCheck.organizationHeaderValue")

  private val apiKeyHeaderName = configuration.get[String]("memberCheck.apiKeyHeaderName")

  private val organizationHeader = Tuple2(organizationHeaderName, organizationHeaderValue)

  private val baseURL = configuration.get[String]("memberCheck.url")

  private val endpoint = configuration.get[String]("memberCheck.endpoints.singleCorporateScanResult")

  private val url = baseURL + endpoint

  private def action(request: String, apiKeyHeader: (String, String)): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + request).withHttpHeaders(organizationHeader, apiKeyHeader).get)

  object Service {
    def get(resultID: String): Future[Response] = {
      val apiHeaderValue = Future(keyStore.getPassphrase("memberCheckAPIHeaderValue"))

      (for {
        apiHeaderValue <- apiHeaderValue
        response <- action(resultID, Tuple2(apiKeyHeaderName, apiHeaderValue))
      } yield response
        ).recover {
        case baseException: BaseException => throw baseException
        case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
          throw new BaseException(constants.Response.CONNECT_EXCEPTION)
      }
    }
  }

}