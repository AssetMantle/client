package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.ProposalDepositResponse.{Deposit, Response}

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetProposalDeposit @Inject()()(implicit wsClient: WSClient, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_PROPOSAL_DEPOSIT

  private implicit val logger: Logger = Logger(this.getClass)

  private val path1 = "cosmos/gov/v1beta1/proposals"

  private val path2 = "/deposits/"

  private val url = constants.Blockchain.RestEndPoint + "/" + path1 + "/"

  private val DepositorNotFoundRegex = """depositer.*not.found.for.proposal""".r

  private def action(id: String, address: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + id + path2 + address).get)

  object Service {

    def get(id: String, address: String): Future[Response] = action(id = id, address = address).recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
      case baseException: BaseException => if (DepositorNotFoundRegex.findFirstIn(baseException.failure.message).isDefined) {
        Response(Deposit(proposal_id = id, depositor = address, amount = Seq()))
      } else throw baseException
    }
  }

}
