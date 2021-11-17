package queries.blockchain.params

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.params.GovResponse._

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetGov @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_PARAMS_GOV

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.ip")

  private val port = configuration.get[String]("blockchain.restPort")

  private val path1 = "gov/parameters/voting"

  private val path2 = "gov/parameters/deposit"

  private val path3 = "gov/parameters/tallying"

  private val url1 = ip + ":" + port + "/" + path1

  private val url2 = ip + ":" + port + "/" + path2

  private val url3 = ip + ":" + port + "/" + path3

  private def votingAction(): Future[VotingResponse] = utilities.JSON.getResponseFromJson[VotingResponse](wsClient.url(url1).get)

  private def depositAction(): Future[DepositResponse] = utilities.JSON.getResponseFromJson[DepositResponse](wsClient.url(url2).get)

  private def tallyingAction(): Future[TallyResponse] = utilities.JSON.getResponseFromJson[TallyResponse](wsClient.url(url3).get)

  object Service {
    def get(): Future[Response] = {
      val votingResponse = votingAction()
      val depositResponse = depositAction()
      val tallyingResponse = tallyingAction()

      (for {
        votingResponse <- votingResponse
        depositResponse <- depositResponse
        tallyingResponse <- tallyingResponse
      } yield Response(voting_params = votingResponse.result, deposit_params = depositResponse.result, tally_params = tallyingResponse.result)).recover {
        case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
      }
    }
  }

}
