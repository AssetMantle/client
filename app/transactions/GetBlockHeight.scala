package transactions

import java.net.ConnectException

import exceptions.BlockChainException
import javax.inject.Inject
import play.api.{Configuration, Logger}
import play.api.libs.ws.WSClient
import transactions.Response.BlockHeightResponse.Response

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class GetBlockHeight @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_GET_BLOCK_HEIGHT

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.abciPort")

  private val path = "block?height="

  private val url = ip + ":" + port + "/" + path

  private def action(request: Int)(implicit executionContext: ExecutionContext): Future[Response] = wsClient.url(url + request).get.map { response => utilities.JSON.getResponseFromJson[Response](response)}

  object Service {

    def get(blockHeight: Int)(implicit executionContext: ExecutionContext): Response = try {
      Await.result(action(blockHeight), Duration.Inf)
    } catch {
      case connectException: ConnectException =>
        logger.error(constants.Error.CONNECT_EXCEPTION, connectException)
        throw new BlockChainException(constants.Error.CONNECT_EXCEPTION)
    }
  }

}
