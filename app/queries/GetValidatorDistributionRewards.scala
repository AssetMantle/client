package queries

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.ValidatorDistributionResponse.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetValidatorDistributionRewards @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_ABCI_INFO

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "distribution/validators"

  private val url = ip + ":" + port + "/" + path + "/"

  private def action(validatorAddress: String): Future[Response] = wsClient.url(url + validatorAddress).get.map { response => utilities.JSON.convertJsonStringToObject[Response](response.body) }

  object Service {

    def get(validatorAddress: String): Future[Response] = action(validatorAddress).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}
