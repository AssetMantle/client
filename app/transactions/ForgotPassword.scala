package transactions

import java.net.{ConnectException, UnknownHostException}

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OWrites, Reads}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.Abstract.{BaseRequest, BaseResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ForgotPassword @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_FORGOT_PASSWORD

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "forgotPassword"

  private val url = ip + ":" + port + "/" + path + "/"

  private def action(username: String, request: Request): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + username).post(Json.toJson(request)))

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class Request(seed: String, newPassword: String, confirmNewPassword: String) extends BaseRequest

  private implicit val responseReads: Reads[Response] = Json.reads[Response]
  implicit val responseWrites: OWrites[Response] = Json.writes[Response]

  case class Response(error: Boolean, message: String) extends BaseResponse

  object Service {

    def post(username: String, request: Request): Future[Response] = {
      action(username = username, request = request).recover {
        case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
        case unknownHostException: UnknownHostException => throw new BaseException(constants.Response.UNKNOWN_HOST_EXCEPTION, unknownHostException)
      }
    }

  }

}

