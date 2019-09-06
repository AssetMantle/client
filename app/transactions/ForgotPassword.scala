package transactions

import java.net.ConnectException

import exceptions.BlockChainException
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OWrites, Reads}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseResponse
import transactions.Abstract.BaseRequestEntity

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class ForgotPassword @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_FORGOT_PASSWORD

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "forgotPassword"

  private val url = ip + ":" + port + "/" + path + "/"

  case class Request(seed: String, newPassword: String, confirmNewPassword: String) extends BaseRequestEntity

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class Response(error: Boolean, message: String) extends BaseResponse

  private implicit val responseReads: Reads[Response] = Json.reads[Response]

  private def action(username: String, request: Request): Future[Response] = wsClient.url(url + username).post(Json.toJson(request)).map { response => utilities.JSON.getResponseFromJson[Response](response) }

  object Service {
    def post(username: String, request: Request): Response = try {
      Await.result(action(username = username, request = request), Duration.Inf)
    } catch {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BlockChainException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}

