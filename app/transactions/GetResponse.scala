package transactions

import java.net.ConnectException

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import play.api.{Configuration, Logger}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class GetResponse @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_GET_RESPONSE

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "response"

  private val url = ip + ":" + port + "/" + path + "/"

  private def action(request: String)(implicit executionContext: ExecutionContext):Future[WSResponse] = wsClient.url(request).get

  object Service {

    def get(ticketID: String): WSResponse = {
      try{
        val request = url + ticketID
        Await.result(action(request), Duration.Inf)
      } catch {
        case connectException: ConnectException => logger.error(constants.Error.CONNECT_EXCEPTION, connectException)
          throw new BlockChainException(constants.Error.CONNECT_EXCEPTION)
      }
    }
  }
}
