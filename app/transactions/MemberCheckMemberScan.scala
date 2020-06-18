package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json, OWrites}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import responses.MemberCheckMemberScanResponse.Response
import utilities.KeyStore

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MemberCheckMemberScan @Inject()(wsClient: WSClient, keyStore: KeyStore)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_MEMBER_CHECK_MEMBER_SCAN

  private implicit val logger: Logger = Logger(this.getClass)

  private val organizationHeaderName = configuration.get[String]("memberCheck.organizationHeaderName")

  private val organizationHeaderValue = configuration.get[String]("memberCheck.organizationHeaderValue")

  private val apiKeyHeaderName = configuration.get[String]("memberCheck.apiKeyHeaderName")

  private val organizationHeader = Tuple2(organizationHeaderName, organizationHeaderValue)

  private val baseURL = configuration.get[String]("memberCheck.url")

  private val endpoint = configuration.get[String]("memberCheck.endpoints.singleMemberScan")

  private val url = baseURL + endpoint

  private def action(request: Request, apiKeyHeader: (String, String)): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url).withHttpHeaders(organizationHeader, apiKeyHeader).post(Json.toJson(request)))

  // Either of originalScriptName or firstName + lastName are necessary. To specify a mononym (single name), enter a dash (-) in firstName parameter and the mononym in lastName.
  //dob field requires DD/MM/YYYY
  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]
  case class Request(matchType: String = "Exact", closeMatchRateThreshold: Option[Int] = None, whitelist: String = "Apply", residence: String = "ApplyPEP", pepJurisdiction: String = "Apply", memberNumber: String, firstName: String, middleName: Option[String] = None, lastName: String, originalScriptName: Option[String] = None, gender: Option[String] = None, dob: Option[String] = None, address: Option[String] = None, updateMonitoringList: Boolean = true) extends BaseRequest

  object Service {

    def post(request: Request): Future[Response] = {
      val apiHeaderValue = Future(keyStore.getPassphrase("memberCheckAPIHeaderValue"))

      (for {
        apiHeaderValue <- apiHeaderValue
        response <- action(request, Tuple2(apiKeyHeaderName, apiHeaderValue))
      } yield response
        ).recover {
        case baseException: BaseException => throw baseException
        case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
          throw new BaseException(constants.Response.CONNECT_EXCEPTION)
      }
    }
  }

}