package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.ProposalResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetProposal @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_PROPOSAL

  private implicit val logger: Logger = Logger(this.getClass)

  private val path = "gov/proposals"

  private val url = constants.Blockchain.RestEndPoint + "/" + path + "/"

  private def action(id: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + id).get)

  object Service {

    def get(id: Int): Future[Response] = action(id.toString).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}
