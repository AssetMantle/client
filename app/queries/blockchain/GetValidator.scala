package queries.blockchain

import play.api.Logger
import play.api.libs.ws.WSClient
import queries.responses.blockchain.ValidatorResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetValidator @Inject()()(implicit wsClient: WSClient, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_VALIDATOR

  private implicit val logger: Logger = Logger(this.getClass)

  private val path = "cosmos/staking/v1beta1/validators"

  private val url = constants.Blockchain.RestEndPoint + "/" + path + "/"

  private def action(operatorAddress: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + operatorAddress).get)

  object Service {

    def get(operatorAddress: String): Future[Response] = action(operatorAddress).recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
    }
  }

}
