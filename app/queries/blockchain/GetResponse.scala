package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetResponse @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_RESPONSE

  private implicit val logger: Logger = Logger(this.getClass)

  private val path = "response"

  private val url = constants.Blockchain.RestEndPoint + "/" + path + "/"

  private def action(request: String): Future[WSResponse] = wsClient.url(url + request).get

  object Service {

    def get(ticketID: String): Future[WSResponse] = action(ticketID).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }

  }

}
