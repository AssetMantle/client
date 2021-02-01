package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.ValidatorDelegatorRedelegationsResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetValidatorDelegatorRedelegations @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_VALIDATOR_DELEGATOR_REDELEGATIONS

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.ip")

  private val port = configuration.get[String]("blockchain.restPort")

  private val path1 = "staking/redelegations"

  private val url = ip + ":" + port + "/" + path1

  private def action: Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url).get)

  object Service {

    def get: Future[Response] = action.recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}
