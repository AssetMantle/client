package queries

import java.net.ConnectException

import exceptions.BlockChainException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.NegotiationIdResponse.Response

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class GetNegotiationID @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_GET_ACCOUNT

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "negotiationID"

  private val url = ip + ":" + port + "/" + path + "/"

  private def action(request: String): Future[Response] = wsClient.url(url + request).get.map { response => utilities.JSON.getResponseFromJson[Response](response) }

  object Service {

    def get(buyerAddress: String, sellerAddress: String, pegHash: String): Response = try {
      Await.result(action(buyerAddress + "/" + sellerAddress + "/" + pegHash), Duration.Inf)
    } catch {
      case connectException: ConnectException =>
        logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BlockChainException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}