package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.ValidatorOutstandingRewards.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetValidatorOutstandingRewards @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_VALIDATOR_OUTSTANDING_REWARDS

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.ip")

  private val port = configuration.get[String]("blockchain.restPort")

  private val path1 = "distribution/validators"

  private val path2 = "/outstanding_rewards"

  private val url = ip + ":" + port + "/" + path1 + "/"

  private def action(validatorAddress: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + validatorAddress + path2).get)

  object Service {

    def get(validatorAddress: String): Future[Response] = action(validatorAddress).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}
