package queries.blockchain.params

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.Configuration
import play.api.Logger
import queries.responses.blockchain.params.DistributionResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetDistribution @Inject()()(implicit wsClient: WSClient, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_PARAMS_DISTRIBUTION

  private implicit val logger: Logger = Logger(this.getClass)

  private val path = "cosmos/distribution/v1beta1/params"

  private val url = constants.Blockchain.RestEndPoint + "/" + path

  private def action(): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url).get)

  object Service {
    def get(): Future[Response] = action().recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
      case illegalStateException: IllegalStateException => constants.Response.ILLEGAL_STATE_EXCEPTION.throwBaseException(illegalStateException)
    }
  }

}
