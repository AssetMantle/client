package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.MetaResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetMeta @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_META

  private implicit val logger: Logger = Logger(this.getClass)

  private val path = "xprt/metas/metas"

  private val url = constants.Blockchain.RestEndPoint + "/" + path + "/"

  private def action(metaID: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + metaID).get)

  object Service {

    def get(metaID: String): Future[Response] = action(metaID).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}