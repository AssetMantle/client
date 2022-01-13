package transactions.blockchain

import exceptions.BaseException
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.responses.blockchain.KeyResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddKey @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_ADD_KEY

  private implicit val logger: Logger = Logger(this.getClass)

  private val path = "keys/add"

  private val url = constants.Blockchain.RestEndPoint + "/" + path

  private def action(request: Request): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url).post(Json.toJson(request)))

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class Request(name: String, mnemonic: String)

  object Service {

    def post(request: Request): Future[Response] = action(request).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
      case baseException: BaseException => throw baseException
      case e: Exception => logger.error(e.getMessage)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION, e)
    }
  }

}