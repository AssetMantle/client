package queries

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.ZoneResponse.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetZone @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_ZONE

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.ip")

  private val port = configuration.get[String]("blockchain.restPort")

  private val path = "zone"

  private val url = ip + ":" + port + "/" + path + "/"

  private def action(request: String): Future[Response] = wsClient.url(url + request).get.map { response => new Response(response) }

  object Service {

    def get(zoneID: String): Future[Response] = action(zoneID).recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}