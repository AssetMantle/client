package queries

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.AssetResponse.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetAsset @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_ASSET

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.ip")

  private val port = configuration.get[String]("blockchain.restPort")

  private val path = "assets/assets"

  private val url = ip + ":" + port + "/" + path + "/"

  private def action(assetID: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + assetID).get)

  object Service {

    def get(assetID: String): Future[Response] = action(assetID).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}