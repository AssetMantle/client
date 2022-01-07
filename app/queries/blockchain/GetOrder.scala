package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.OrderResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetOrder @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_ORDER

  private implicit val logger: Logger = Logger(this.getClass)

  private val restURL = configuration.get[String]("blockchain.restURL")

  private val path = "orders/orders"

  private val url = restURL + "/" + path + "/"

  private def action(orderID: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + orderID).get)

  object Service {

    def get(orderID: String): Future[Response] = action(orderID).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}