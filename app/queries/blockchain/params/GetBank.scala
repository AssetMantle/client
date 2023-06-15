package queries.blockchain.params

import play.api.{Configuration, Logger}
import play.api.libs.ws.WSClient
import queries.responses.blockchain.params.BankResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetBank @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_PARAMS_BANK

  private implicit val logger: Logger = Logger(this.getClass)

  private val path = "cosmos/bank/v1beta1/params"

  private val url = constants.Blockchain.RestEndPoint + "/" + path

  private def action(): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url).get)

  object Service {
    def get(): Future[Response] = action().recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
      case illegalStateException: IllegalStateException => constants.Response.ILLEGAL_STATE_EXCEPTION.throwBaseException(illegalStateException)
    }
  }

}
