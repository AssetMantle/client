package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import transactions.responses.MemberCheckCorporateScanResponse.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MemberCheckCorporateScan @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_MEMBER_CHECK_CORPORATE_SCAN

  private implicit val logger: Logger = Logger(this.getClass)

  private val organizationHeaderName = configuration.get[String]("memberCheck.organizationHeaderName")

  private val organizationHeaderValue = configuration.get[String]("memberCheck.organizationHeaderValue")

  private val apiKeyHeaderName = configuration.get[String]("memberCheck.apiKeyHeaderName")

  private val apiHeaderValue = configuration.get[String]("memberCheck.apiHeaderValue")

  private val organizationHeader = Tuple2(organizationHeaderName, organizationHeaderValue)

  private val apiKeyHeader = Tuple2(apiKeyHeaderName, apiHeaderValue)

  private val baseURL = configuration.get[String]("memberCheck.url")

  private val endpoint = configuration.get[String]("memberCheck.endpoints.singleCorporateScan")

  private val testURL = constants.Test.BASE_URL+"/loopBack/memberCheckCorporateScan"

  //private val url = baseURL + endpoint

  private def action(request: Request): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(testURL).withHttpHeaders(organizationHeader, apiKeyHeader).post(Json.toJson(request)))

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]
  case class Request(matchType: String = "Exact", whitelist: String = "Apply", companyName: String, idNumber: Option[String] = None, entityNumber: String, address: Option[String] = None, updateMonitoringList: Boolean = true) extends BaseRequest

  object Service {

    def post(request: Request): Future[Response] = action(request).recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}