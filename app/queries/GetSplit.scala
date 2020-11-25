package queries

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.SplitResponse.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetSplit @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_SPLIT

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.ip")

  private val port = configuration.get[String]("blockchain.restPort")

  private val path = "splits/splits"

  private val url = ip + ":" + port + "/" + path + "/"

  private def action(ownerID: String, ownableID: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + ownerID + "|" + ownableID).get)

  object Service {

    def get(ownerID: String, ownableID: String): Future[Response] = action(ownerID = ownerID, ownableID = ownableID).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}