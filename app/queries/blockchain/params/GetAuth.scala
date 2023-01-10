package queries.blockchain.params

import exceptions.BaseException
import play.api.Configuration
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.ws.WSClient
import queries.responses.blockchain.params.AuthResponse.Response
import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetAuth @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_PARAMS_AUTH

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val path = "cosmos/auth/v1beta1/params"

  private val url = constants.Blockchain.RestEndPoint + "/" + path

  private def action(): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url).get)

  object Service {
    def get(): Future[Response] = action().recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}
