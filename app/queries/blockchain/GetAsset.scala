package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.AssetResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetAsset @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_ASSET

  private implicit val logger: Logger = Logger(this.getClass)

  private val path = "xprt/assets/assets"

  private val url = constants.Blockchain.RestEndPoint + "/" + path + "/"

  private def action(assetID: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + assetID).get)

  object Service {

    def get(assetID: String): Future[Response] = action(assetID).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}