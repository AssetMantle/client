package queries.blockchain.params

import play.api.Logger
import play.api.libs.ws.WSClient
import queries.responses.blockchain.params.SplitResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetSplit @Inject()()(implicit wsClient: WSClient, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_PARAMS_SPLIT

  private implicit val logger: Logger = Logger(this.getClass)

  private val path = "splits/parameters"

  private val url = constants.Blockchain.RestEndPoint + "/" + path

  private def action(): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url).get)

  object Service {
    def get(): Future[Response] = {
      (for {
        response <- action()
      } yield response).recover {
        case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
      }
    }
  }

}
