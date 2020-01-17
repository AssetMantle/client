package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddKey @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_ADD_KEY

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "keys"

  private val url = ip + ":" + port + "/" + path

  private def action(request: Request): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url).post(Json.toJson(request)))

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class Request(name: String, password: String, seed:String)

  private implicit val responseReads: Reads[Response] = Json.reads[Response]

  case class Response(name: String, address: String, pubkey: String, mnemonic: String) extends BaseResponse

  object Service {

    def post(request: Request): Future[Response] = action(request).recover {
      case connectException: ConnectException =>
        logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
      case e:Exception=>
        logger.error(e.getMessage)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

}