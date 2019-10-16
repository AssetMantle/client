package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OWrites, Reads}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseResponse
import transactions.Abstract.BaseRequest

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class ChangePassword @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_CHANGE_PASSWORD

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "updatePassword"

  private val url = ip + ":" + port + "/" + path + "/"

  case class Request(oldPassword: String, newPassword: String, confirmNewPassword: String) extends BaseRequest

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class Response(error: Boolean, message: String) extends BaseResponse

  private implicit val responseReads: Reads[Response] = Json.reads[Response]

  private def action(username: String, request: Request): Future[Response] =  utilities.JSON.getResponseFromJson[Response](wsClient.url(url + username).put(Json.toJson(request)))

  object Service {

    def post(username: String, request: Request):Future[Response]=action(username = username, request = request).recover{
      case connectException: ConnectException =>
        logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}

