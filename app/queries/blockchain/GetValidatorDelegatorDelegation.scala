package queries.blockchain

import exceptions.BaseException
import play.api.Logger
import play.api.libs.ws.WSClient
import queries.responses.blockchain.ValidatorDelegatorDelegationResponse.Response
import queries.responses.common.Delegation

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetValidatorDelegatorDelegation @Inject()()(implicit wsClient: WSClient, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_VALIDATOR_DELEGATOR_DELEGATIONS

  private implicit val logger: Logger = Logger(this.getClass)

  private val path1 = "cosmos/staking/v1beta1/validators"

  private val path2 = "/delegations/"

  private val url = constants.Blockchain.RestEndPoint + "/" + path1 + "/"

  private val DelegationNotFoundRegex = """delegation.with.delegator.*not.found.for.validator.*""".r

  private def action(delegatorAddress: String, validatorAddress: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + validatorAddress + path2 + delegatorAddress).get)

  object Service {

    def get(delegatorAddress: String, validatorAddress: String): Future[Response] = action(delegatorAddress = delegatorAddress, validatorAddress = validatorAddress).recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
      case baseException: BaseException => if (DelegationNotFoundRegex.findFirstIn(baseException.failure.message).isDefined) {
        Response(Delegation.Result(Delegation(delegator_address = delegatorAddress, validator_address = validatorAddress, shares = 0)))
      } else throw baseException
    }
  }

}
