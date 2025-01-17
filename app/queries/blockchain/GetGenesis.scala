package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.Configuration
import play.api.Logger
import queries.responses.blockchain.GenesisResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetGenesis @Inject()()(implicit wsClient: WSClient, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_GENESIS

  private implicit val logger: Logger = Logger(this.getClass)

  private val path = "genesis"

  private val url = constants.Blockchain.RPCEndPoint + "/" + path

  private def action: Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url).withMethod("GET").stream())

  object Service {
    def get: Future[Response] = action.recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
    }
  }

}
