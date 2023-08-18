package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.Configuration
import play.api.Logger
import queries.responses.blockchain.ValidatorsResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetUnbondedValidators @Inject()()(implicit wsClient: WSClient, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_UNBONDED_VALIDATORS

  private implicit val logger: Logger = Logger(this.getClass)

  private val path = "cosmos/staking/v1beta1/validators?status=BOND_STATUS_UNBONDED"

  private val url = constants.Blockchain.RestEndPoint + "/" + path

  private def action(): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url).get)

  object Service {

    def get(): Future[Response] = {
      action().recover {
        case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
      }
    }
  }

}
