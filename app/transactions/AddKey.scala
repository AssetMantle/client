package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.Inject
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class AddKey @Inject()(configuration: Configuration, wsClient: WSClient, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN

  private val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.port")

  private val path = "keys"

  private val url = ip + ":" + port + "/" + path

  private def action(request: Request): Future[Response] = wsClient.url(url).post(request.json).map { response => new Response(response) }(executionContext)

  class Response(response: WSResponse) {
    val accountAddress: String = response.json("address").as[String]
    val publicKey: String = response.json("pub_key").as[String]
    val body: String = response.body.toString
  }

  private class Request(name: String, password: String, seed: String) {
    val json: JsObject = Json.obj(
      "name" -> name,
      "password" -> password,
      "seed" -> seed)
  }

  object Service {
    def post(name: String, password: String, seed: String): Response = try {
      Await.result(action(new Request(name, password, seed)), Duration.Inf)
    } catch {
      case connectException: ConnectException =>
        logger.error(constants.Error.CONNECT_EXCEPTION, connectException)
        throw new BaseException(constants.Error.CONNECT_EXCEPTION)
    }
  }

}