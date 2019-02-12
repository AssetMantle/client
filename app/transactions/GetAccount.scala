package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.Inject
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class GetAccount @Inject()(configuration: Configuration, wsClient: WSClient, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_GET_SEED

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.port")

  private val path = "accounts"

  private val url = ip + ":" + port + "/" + path

  private def action()(implicit executionContext: ExecutionContext): Future[Response] = wsClient.url(url).get.map { response => new Response(response) }

  class Response(response: WSResponse) {
    val body: String = response.body
  }

  object Service {

    def get()(implicit executionContext: ExecutionContext): Response = try {
      Await.result(action(), Duration.Inf)
    } catch {
      case connectException: ConnectException =>
        logger.error(constants.Error.CONNECT_EXCEPTION, connectException)
        throw new BaseException(constants.Error.CONNECT_EXCEPTION)
    }
  }

}