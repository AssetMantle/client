package transactions

import java.net.ConnectException
import exceptions.BlockChainException
import javax.inject.Inject
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class AddKey @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_ADD_KEY

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "keys"

  private val url = ip + ":" + port + "/" + path

  case class Request(name: String, password: String, seed: String)

  private implicit val requestWrites: OWrites[Request] =  Json.writes[Request]

  case class Response(name: String, address: String, pub_key: String, seed: String)

  private implicit val responseReads: Reads[Response] = Json.reads[Response]

  private def action(request: Request)(implicit executionContext: ExecutionContext): Future[Response] = wsClient.url(url).post(Json.toJson(request)).map { response => utilities.JSON.getResponseFromJson[Response](response) }

  object Service {

    def post(request: Request)(implicit executionContext: ExecutionContext): Response = try {
      Await.result(action(request), Duration.Inf)
    } catch {
      case connectException: ConnectException =>
        logger.error(constants.Error.CONNECT_EXCEPTION, connectException)
        throw new BlockChainException(constants.Error.CONNECT_EXCEPTION)
    }
  }

}