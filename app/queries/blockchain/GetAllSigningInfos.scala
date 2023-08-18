package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.Configuration
import play.api.Logger
import queries.responses.blockchain.AllSigningInfosResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetAllSigningInfos @Inject()()(implicit wsClient: WSClient, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_ALL_SIGNING_INFOS

  private implicit val logger: Logger = Logger(this.getClass)

  private val path = "slashing/signing_infos"

  private val url = constants.Blockchain.RestEndPoint + "/" + path

  private def action: Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url).get)

  object Service {

    def get: Future[Response] = action.recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
    }
  }

}
