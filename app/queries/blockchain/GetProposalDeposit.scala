package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.ProposalDepositResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetProposalDeposit @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_PROPOSAL_DEPOSIT

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.ip")

  private val port = configuration.get[String]("blockchain.restPort")

  private val path1 = "gov/proposals"

  private val path2 = "/deposits/"

  private val url = ip + ":" + port + "/" + path1 + "/"

  private def action(id: String, address: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + id + path2 + address).get)

  object Service {

    def get(id: String, address: String): Future[Response] = action(id = id, address = address).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}
