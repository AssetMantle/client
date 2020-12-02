package queries

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetSeed @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_MNEMONIC

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.ip")

  private val port = configuration.get[String]("blockchain.restPort")

  private val path = "keys/seed"

  private val url = ip + ":" + port + "/" + path

  private def action(): Future[Response] = wsClient.url(url).get.map { response => new Response(response) }

  class Response(response: WSResponse) {
    val body: String = response.body
  }

  object Service {
    def get(): Future[Response] = action().recover {
      case connectException: ConnectException =>
        logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}