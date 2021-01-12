package transactions.memberCheck

import exceptions.BaseException
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import utilities.KeyStore

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CorporateScanResultDecision @Inject()(wsClient: WSClient, keyStore: KeyStore)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_MEMBER_CHECK_CORPORATE_SCAN_RESULT_DECISION

  private implicit val logger: Logger = Logger(this.getClass)

  private val organizationHeaderName = configuration.get[String]("memberCheck.organizationHeaderName")

  private val organizationHeaderValue = configuration.get[String]("memberCheck.organizationHeaderValue")

  private val apiKeyHeaderName = configuration.get[String]("memberCheck.apiKeyHeaderName")

  private val apiHeaderValue = keyStore.getPassphrase(constants.KeyStore.MEMBER_CHECK_API_HEADER_VALUE)

  private val organizationHeader = Tuple2(organizationHeaderName, organizationHeaderValue)

  private val apiKeyHeader = Tuple2(apiKeyHeaderName, apiHeaderValue)

  private val baseURL = configuration.get[String]("memberCheck.url")

  private val endpoint = configuration.get[String]("memberCheck.endpoints.singleCorporateScanResult")

  private val url = baseURL + endpoint

  private def action(id: String, request: Request): Future[WSResponse] = wsClient.url(url + id + "/decisions").withHttpHeaders(organizationHeader, apiKeyHeader).post(Json.toJson(request))

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class Request(matchDecision: String, assessedRisk: String, comment: String) extends BaseRequest

  object Service {

    def post(id: String, request: Request): Future[WSResponse] = action(id, request).recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}