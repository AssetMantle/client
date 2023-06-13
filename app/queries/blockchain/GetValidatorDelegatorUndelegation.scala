package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.ValidatorDelegatorUndelegationResponse.Response
import queries.responses.common.Undelegation

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetValidatorDelegatorUndelegation @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_VALIDATOR_DELEGATOR_UNDELEGATION

  private implicit val logger: Logger = Logger(this.getClass)

  private val path1 = "cosmos/staking/v1beta1/validators"

  private val path2 = "/delegations/"

  private val path3 = "/unbonding_delegation"

  private val url = constants.Blockchain.RestEndPoint + "/" + path1 + "/"

  private val UndelegationNotFoundRegex = """unbonding.delegation.with.delegator.*not.found.for.validator.*""".r

  private def action(delegatorAddress: String, validatorAddress: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + validatorAddress + path2 + delegatorAddress + path3).get)

  object Service {

    def get(delegatorAddress: String, validatorAddress: String): Future[Response] = action(delegatorAddress = delegatorAddress, validatorAddress = validatorAddress).recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
      case baseException: BaseException => if (UndelegationNotFoundRegex.findFirstIn(baseException.failure.message).isDefined) {
        Response(Undelegation.Result(delegator_address = delegatorAddress, validator_address = validatorAddress, entries = Seq()))
      } else throw baseException
    }
  }

}
