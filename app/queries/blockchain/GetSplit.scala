package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.SplitResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetSplit @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_SPLIT

  private implicit val logger: Logger = Logger(this.getClass)

  private val restURL = configuration.get[String]("blockchain.restURL")

  private val path = "splits/splits"

  private val url = restURL + "/" + path + "/"

  private def action(ownerID: String, ownableID: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + ownerID + "|" + ownableID).get)

  object Service {

    def get(ownerID: String, ownableID: String): Future[Response] = action(ownerID = ownerID, ownableID = ownableID).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}