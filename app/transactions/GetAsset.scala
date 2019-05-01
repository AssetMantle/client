package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.Response.AssetResponse.Response

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class GetAsset @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_GET_ASSET

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "asset"

  private val url = ip + ":" + port + "/" + path + "/"

  private def action(request: String)(implicit executionContext: ExecutionContext): Future[Response] = wsClient.url(url + request).get.map { response => utilities.JSON.getResponseFromJson[Response](response) }

  object Service {

    def get(assetPegHash: String)(implicit executionContext: ExecutionContext): Response = try {
      Await.result(action(assetPegHash), Duration.Inf)
    } catch {
      case connectException: ConnectException =>
        logger.error(constants.Error.CONNECT_EXCEPTION, connectException)
        throw new BaseException(constants.Error.CONNECT_EXCEPTION)
    }
  }

}