package queries

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.MemberCheckMemberScanResponse.Response
import utilities.KeyStore

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetMemberCheckMemberScan @Inject()(wsClient: WSClient, keyStore: KeyStore)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_MEMBER_CHECK_MEMBER_SCAN

  private implicit val logger: Logger = Logger(this.getClass)

  private val organizationHeaderName = configuration.get[String]("memberCheck.organizationHeaderName")

  private val organizationHeaderValue = configuration.get[String]("memberCheck.organizationHeaderValue")

  private val apiKeyHeaderName = configuration.get[String]("memberCheck.apiKeyHeaderName")

  private val apiHeaderValue = keyStore.getPassphrase(constants.KeyStore.MEMBER_CHECK_API_HEADER_VALUE)

  private val organizationHeader = Tuple2(organizationHeaderName, organizationHeaderValue)

  private val apiKeyHeader = Tuple2(apiKeyHeaderName, apiHeaderValue)

  private val baseURL = configuration.get[String]("memberCheck.url")

  private val endpoint = configuration.get[String]("memberCheck.endpoints.singleMemberScan")

  private val url = baseURL + endpoint + "/"

  private def action(request: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + request).withHttpHeaders(organizationHeader, apiKeyHeader).get)

  object Service {

    def get(scanID: String): Future[Response] = action(scanID).recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}