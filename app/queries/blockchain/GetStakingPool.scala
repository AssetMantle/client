package queries.blockchain

import play.api.Logger
import play.api.libs.ws.WSClient
import queries.responses.blockchain.StakingPoolResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetStakingPool @Inject()()(implicit wsClient: WSClient, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_STAKING_POOL

  private implicit val logger: Logger = Logger(this.getClass)

  private val url = constants.Blockchain.RestEndPoint + "/cosmos/staking/v1beta1/pool"

  private def action: Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url).get)

  object Service {

    def get: Future[Response] = action.recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
    }
  }

}
