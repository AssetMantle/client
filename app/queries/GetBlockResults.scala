package queries

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.BlockResultResponse.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetBlockResults @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_BLOCK_RESULTS

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.ip")

  private val port = configuration.get[String]("blockchain.abciPort")

  private val path = "block_results?height="

  private val url = ip + ":" + port + "/" + path

  private def action(height: Int): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + height).get)

  object Service {

    def get(height: Int): Future[Response] = action(height).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}
