package queries

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.ValidatorDelegatorUndelegationsResponse.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetValidatorDelegatorUndelegations @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_ABCI_INFO

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path1 = "staking/delegators"

  private val path2 = "/unbonding_delegations/"

  private val url = ip + ":" + port + "/" + path1 + "/"

  private def action(delegatorAddress: String, validatorAddress: String): Future[Response] = wsClient.url(url + delegatorAddress + path2 + validatorAddress).get.map { response => utilities.JSON.convertJsonStringToObject[Response](response.body) }

  object Service {

    def get(delegatorAddress: String, validatorAddress: String): Future[Response] = action(delegatorAddress = delegatorAddress, validatorAddress = validatorAddress).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}
