package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.DelegatorRewardsResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetDelegatorRewards @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_DELEGATOR_REWARDS

  private implicit val logger: Logger = Logger(this.getClass)

  private val path1 = "cosmos/distribution/v1beta1/delegators"

  private val path2 = "/rewards"

  private val url = constants.Blockchain.RestEndPoint + "/" + path1 + "/"

  private def action(delegatorAddress: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + delegatorAddress + path2).get)

  object Service {

    def get(delegatorAddress: String): Future[Response] = action(delegatorAddress).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}
