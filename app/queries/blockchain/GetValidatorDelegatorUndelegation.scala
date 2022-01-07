package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.ValidatorDelegatorUndelegationResponse.Response
import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetValidatorDelegatorUndelegation @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_VALIDATOR_DELEGATOR_UNDELEGATION

  private implicit val logger: Logger = Logger(this.getClass)

  private val restURL = configuration.get[String]("blockchain.restURL")

  private val path1 = "cosmos/staking/v1beta1/validators"

  private val path2 = "/delegations/"

  private val path3 = "/unbonding_delegation"

  private val url = restURL + "/" + path1 + "/"

  private def action(delegatorAddress: String, validatorAddress: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + validatorAddress + path2 + delegatorAddress + path3).get)

  object Service {

    def get(delegatorAddress: String, validatorAddress: String): Future[Response] = action(delegatorAddress = delegatorAddress, validatorAddress = validatorAddress).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}
