package queries

import java.net.{ConnectException, UnknownHostException}

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.MemberCheckMemberScanResultResponse.ResponsePart1
import queries.responses.MemberCheckMemberScanResultResponse.ResponsePart2
import queries.responses.MemberCheckMemberScanResultResponse.Response
import queries.responses.MemberCheckMemberScanResultResponse
import utilities.KeyStore

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetMemberCheckMemberScanResult @Inject()(wsClient: WSClient, keyStore: KeyStore)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_MEMBER_CHECK_MEMBER_SCAN_RESULT

  private implicit val logger: Logger = Logger(this.getClass)

  private val organizationHeaderName = configuration.get[String]("memberCheck.organizationHeaderName")

  private val organizationHeaderValue = configuration.get[String]("memberCheck.organizationHeaderValue")

  private val apiKeyHeaderName = configuration.get[String]("memberCheck.apiKeyHeaderName")

  private val apiHeaderValue = keyStore.getPassphrase(constants.KeyStore.MEMBER_CHECK_API_HEADER_VALUE)

  private val organizationHeader = Tuple2(organizationHeaderName, organizationHeaderValue)

  private val apiKeyHeader = Tuple2(apiKeyHeaderName, apiHeaderValue)

  private val baseURL = configuration.get[String]("memberCheck.url")

  private val endpoint = configuration.get[String]("memberCheck.endpoints.singleMemberScanResult")

  private val url = baseURL + endpoint

  private def action(request: String): Future[Response] = {
    val response = wsClient.url(url + request).withHttpHeaders(organizationHeader, apiKeyHeader).get
    val responsePart1 = utilities.JSON.getResponseFromJson[ResponsePart1](response)
    val responsePart2 = utilities.JSON.getResponseFromJson[ResponsePart2](response)
    for {
      responsePart1 <- responsePart1
      responsePart2 <- responsePart2
    } yield Response(responsePart1.id, MemberCheckMemberScanResultResponse.entity(responsePart1.person, responsePart2.person))
  }

  object Service {

    def get(resultID: String): Future[Response] = action(resultID).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
      case unknownHostException: UnknownHostException => throw new BaseException(constants.Response.UNKNOWN_HOST_EXCEPTION, unknownHostException)
    }
  }

}