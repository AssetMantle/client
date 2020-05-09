package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import transactions.responses.MemberCheckMemberScanResponse.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MemberCheckMemberScanResultDecision @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_MEMBER_CHECK_MEMBER_SCAN_RESULT_DECISION

  private implicit val logger: Logger = Logger(this.getClass)

  private val organizationHeaderName = configuration.get[String]("memberCheck.organizationHeaderName")

  private val organizationHeaderValue = configuration.get[String]("memberCheck.organizationHeaderValue")

  private val apiKeyHeaderName = configuration.get[String]("memberCheck.apiKeyHeaderName")

  private val apiHeaderValue = configuration.get[String]("memberCheck.apiHeaderValue")

  private val organizationHeader = Tuple2(organizationHeaderName, organizationHeaderValue)

  private val apiKeyHeader = Tuple2(apiKeyHeaderName, apiHeaderValue)

  private val baseURL = configuration.get[String]("memberCheck.url")

  private val endpoint = configuration.get[String]("memberCheck.endpoints.singleMemberScanResult")

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