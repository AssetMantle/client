package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class GetSeed @Inject()(configuration: Configuration, wsClient: WSClient, executionContext: ExecutionContext) {

  implicit val module: String = constants.Module.BLOCKCHAIN

  private val logger: Logger = Logger(this.getClass())

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.port")

  private val path = "keys/seed"

  private val url = ip + ":" + port + "/" + path

  private def action(): Future[Response] = wsClient.url(url).get.map { response => new Response(response) }(executionContext)

  class Response(response: WSResponse) {
    val body: String = response.body
  }

  object Service {

    def get(): Response = try {
      Await.result(action(), Duration.Inf)
    } catch {
      case connectException: ConnectException =>
        logger.error(constants.Error.CONNECT_EXCEPTION, connectException)
        throw new BaseException(constants.Error.CONNECT_EXCEPTION)
    }

  }

}