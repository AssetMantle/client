package queries

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.ValidatorResponse.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetValidator @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_ABCI_INFO

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "staking/validators"

  private val url = ip + ":" + port + "/" + path + "/"

  private def action(operatorAddress: String): Future[Response] = wsClient.url(url + operatorAddress).get.map { response => utilities.JSON.convertJsonStringToObject[Response](response.body) }

  object Service {

    def get(operatorAddress: String): Future[Response] = action(operatorAddress).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}
