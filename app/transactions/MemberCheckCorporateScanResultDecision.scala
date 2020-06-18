package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import utilities.KeyStore

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MemberCheckCorporateScanResultDecision @Inject()(wsClient: WSClient, keyStore: KeyStore)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_MEMBER_CHECK_CORPORATE_SCAN_RESULT_DECISION

  private implicit val logger: Logger = Logger(this.getClass)

  private val organizationHeaderName = configuration.get[String]("memberCheck.organizationHeaderName")

  private val organizationHeaderValue = configuration.get[String]("memberCheck.organizationHeaderValue")

  private val apiKeyHeaderName = configuration.get[String]("memberCheck.apiKeyHeaderName")

  private val organizationHeader = Tuple2(organizationHeaderName, organizationHeaderValue)

  private val baseURL = configuration.get[String]("memberCheck.url")

  private val endpoint = configuration.get[String]("memberCheck.endpoints.singleCorporateScanResult")

  private val url = baseURL + endpoint

  private def action(id: String, request: Request, apiKeyHeader: (String, String)): Future[WSResponse] = wsClient.url(url + id + "/decisions").withHttpHeaders(organizationHeader, apiKeyHeader).post(Json.toJson(request))

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class Request(matchDecision: String, assessedRisk: String, comment: String) extends BaseRequest

  object Service {

    def post(id: String, request: Request): Future[WSResponse] = {
      val apiHeaderValue = Future(keyStore.getPassphrase("memberCheckAPIHeaderValue"))

      (for {
        apiHeaderValue <- apiHeaderValue
        response <- action(id, request, Tuple2(apiKeyHeaderName, apiHeaderValue))
      } yield response
        ).recover {
        case baseException: BaseException => throw baseException
        case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
          throw new BaseException(constants.Response.CONNECT_EXCEPTION)
      }
    }
  }

}