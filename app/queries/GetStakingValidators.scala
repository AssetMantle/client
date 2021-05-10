package queries

import java.net.{ConnectException, UnknownHostException}

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.StakingValidatorsResponse.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetStakingValidators @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_ABCI_INFO

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "staking/validators"

  private val url = ip + ":" + port + "/" + path

  //Special case because of response type received
  private def action(): Future[Seq[Response]] = wsClient.url(url).get.map { response => utilities.JSON.convertJsonStringToObject[Seq[Response]](response.body) }

  object Service {

    def get(): Future[Seq[Response]] = action().recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
      case unknownHostException: UnknownHostException => throw new BaseException(constants.Response.UNKNOWN_HOST_EXCEPTION, unknownHostException)
    }
  }

}
