package queries.blockchain.params

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.Configuration
import org.slf4j.{Logger, LoggerFactory}
import queries.responses.blockchain.params.DistributionResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetDistribution @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_PARAMS_DISTRIBUTION

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val path = "cosmos/distribution/v1beta1/params"

  private val url = constants.Blockchain.RestEndPoint + "/" + path

  private def action(): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url).get)

  object Service {
    def get(): Future[Response] = action().recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
      case illegalStateException: IllegalStateException => throw new BaseException(constants.Response.ILLEGAL_STATE_EXCEPTION, illegalStateException)
    }
  }

}
