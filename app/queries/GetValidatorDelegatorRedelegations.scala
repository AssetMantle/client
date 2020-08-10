package queries

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.ValidatorDelegatorRedelegationsResponse.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetValidatorDelegatorRedelegations @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_ABCI_INFO

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path1 = "staking/redelegations"

  private val url = ip + ":" + port + "/" + path1

  private def action: Future[Response] = wsClient.url(url).get.map { response => utilities.JSON.convertJsonStringToObject[Response](response.body) }

  object Service {

    def get: Future[Response] = action.recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}
