package transactions.blockchain

import exceptions.BaseException
import play.api.libs.json.{Json, OWrites, Reads}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.Abstract.{BaseRequest, BaseResponse}

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChangePassword @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_CHANGE_PASSWORD

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.ip")

  private val port = configuration.get[String]("blockchain.restPort")

  private val path = "updatePassword"

  private val url = ip + ":" + port + "/" + path + "/"

  private def action(username: String, request: Request): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + username).post(Json.toJson(request)))

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class Request(oldPassword: String, newPassword: String, confirmNewPassword: String) extends BaseRequest

  private implicit val responseReads: Reads[Response] = Json.reads[Response]
  implicit val responseWrites: OWrites[Response] = Json.writes[Response]

  case class Response(error: Boolean, message: String) extends BaseResponse

  object Service {

    def post(username: String, request: Request): Future[Response] = action(username = username, request = request).recover {
      case connectException: ConnectException =>
        logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}

