package queries

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.AllSigningInfosResponse.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetAllSigningInfos @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_ALL_SIGNING_INFOS

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "slashing/signing_infos"

  private val url = ip + ":" + port + "/" + path

  private def action: Future[Response] = wsClient.url(url).get.map { response => utilities.JSON.convertJsonStringToObject[Response](response.body) }

  object Service {

    def get: Future[Response] = action.recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}
