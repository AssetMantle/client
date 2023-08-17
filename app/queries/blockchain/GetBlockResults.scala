package queries.blockchain

import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.BlockResultResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetBlockResults @Inject()()(implicit wsClient: WSClient, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_BLOCK_RESULTS

  private implicit val logger: Logger = Logger(this.getClass)

  private val path = "block_results?height="

  private val url = constants.Blockchain.RPCEndPoint + "/" + path

  private def action(height: Int): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + height).get)

  object Service {

    def get(height: Int): Future[Response] = action(height).recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
    }
  }

}
