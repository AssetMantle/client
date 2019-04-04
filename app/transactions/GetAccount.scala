package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.Inject
import transactions.Response.AccountResponse.Response
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class GetAccount @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_GET_ACCOUNT

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "accounts"

  private val url = ip + ":" + port + "/" + path + "/"

  private def action(request: String)(implicit executionContext: ExecutionContext): Future[Response] = wsClient.url(url + request).get.map { response => utilities.JSON.getResponseFromJson[Response](response)}

  object Service {

    def get(address: String)(implicit executionContext: ExecutionContext): Response = try {
      Await.result(action(address), Duration.Inf)
    } catch {
      case connectException: ConnectException =>
        logger.error(constants.Error.CONNECT_EXCEPTION, connectException)
        throw new BaseException(constants.Error.CONNECT_EXCEPTION)
    }
  }
}