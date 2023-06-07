package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.DelegatorRedelegationsResponse.Response
import queries.responses.common.Redelegation

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetDelegatorRedelegations @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_DELEGATOR_REDELEGATIONS

  private implicit val logger: Logger = Logger(this.getClass)

  private val path1 = "cosmos/staking/v1beta1/delegators"

  private val path2 = "/redelegations"

  private val path3 = "src_validator_addr="

  private val path4 = "dst_validator_addr="

  private val url = constants.Blockchain.RestEndPoint + "/" + path1 + "/"

  private val RedelegationNotFoundRegex = """redelegation.not.found.for.delegator.address.*from.validator.address.*""".r

  private def action(url: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url).get)

  object Service {

    def getAll(delegatorAddress: String): Future[Response] = action(url + delegatorAddress + path2).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }

    def getWithSourceValidator(delegatorAddress: String, sourceValidatorAddress: String): Future[Response] = action(url + delegatorAddress + path2 + "?" + path3 + sourceValidatorAddress).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }

    def getWithDestinationValidator(delegatorAddress: String, destinationValidatorAddress: String): Future[Response] = action(url + delegatorAddress + path2 + "?" + path4 + destinationValidatorAddress).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }

    def getWithSourceAndDestinationValidator(delegatorAddress: String, sourceValidatorAddress: String, destinationValidatorAddress: String): Future[Response] = action(url + delegatorAddress + path2 + "?" + path3 + sourceValidatorAddress + "&" + path4 + destinationValidatorAddress).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
      case baseException: BaseException => if (RedelegationNotFoundRegex.findFirstIn(baseException.failure.message).isDefined) {
        Response(Seq())
      } else throw baseException
    }
  }

}
