package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.Configuration
import org.slf4j.{Logger, LoggerFactory}

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetSeed @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_MNEMONIC

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val path = "keys/seed"

  private val url = constants.Blockchain.RestEndPoint + "/" + path

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