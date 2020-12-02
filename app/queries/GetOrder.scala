package queries

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.OrderResponse.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetOrder @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_ORDER

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.ip")

  private val port = configuration.get[String]("blockchain.restPort")

  private val path = "orders/orders"

  private val url = ip + ":" + port + "/" + path + "/"

  private def action(orderID: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + orderID).get)

  object Service {

    def get(orderID: String): Future[Response] = action(orderID).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}