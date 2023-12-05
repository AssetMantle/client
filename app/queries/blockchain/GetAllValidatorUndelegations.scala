package queries.blockchain

import play.api.Logger
import play.api.libs.ws.WSClient
import queries.responses.blockchain.AllValidatorUndelegationsResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetAllValidatorUndelegations @Inject()()(implicit wsClient: WSClient, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_ALL_VALIDATOR_UNDELEGATIONS

  private implicit val logger: Logger = Logger(this.getClass)

  private val path1 = "cosmos/staking/v1beta1/validators"

  private val path2 = "/unbonding_delegations"

  private val url = constants.Blockchain.RestEndPoint + "/" + path1 + "/"

  private def action(validatorAddress: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + validatorAddress + path2).get)

  object Service {

    def get(validatorAddress: String): Future[Response] = action(validatorAddress).recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
    }
  }

}
