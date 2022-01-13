package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.DelegatorRedelegationsResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetDelegatorRedelegations @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_DELEGATOR_REDELEGATIONS

  private implicit val logger: Logger = Logger(this.getClass)

  private val path = "staking/redelegations"

  private val url = constants.Blockchain.RestEndPoint + "/" + path

  private def action: Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url).get)

  object Service {

    def getAll(delegatorAddress: String): Future[Response] = action.map(x => Response(result = x.result.filter(_.delegator_address == delegatorAddress))).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }

    def getWithSourceValidator(delegatorAddress: String, sourceValidatorAddress: String): Future[Response] = action.map(x => Response(result = x.result.filter(y => y.delegator_address == delegatorAddress && y.validator_src_address == sourceValidatorAddress))).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }

    def getWithDestinationValidator(delegatorAddress: String, destinationValidatorAddress: String): Future[Response] = action.map(x => Response(result = x.result.filter(y => y.delegator_address == delegatorAddress && y.validator_dst_address == destinationValidatorAddress))).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }

    def getWithSourceAndDestinationValidator(delegatorAddress: String, sourceValidatorAddress: String, destinationValidatorAddress: String): Future[Response] = {
      action.map(x => Response(result = x.result.filter(y => y.delegator_address == delegatorAddress && y.validator_src_address == sourceValidatorAddress && y.validator_dst_address == destinationValidatorAddress))).recover {
        case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
      }
    }
  }

}
