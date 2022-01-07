package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.ValidatorsResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetUnbondingValidators @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_UNBONDING_VALIDATORS

  private implicit val logger: Logger = Logger(this.getClass)

  private val restURL = configuration.get[String]("blockchain.restURL")

  private val path = "cosmos/staking/v1beta1/validators?status=BOND_STATUS_UNBONDING"

  private val url = restURL + "/" + path

  private def action(): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url).get)

  object Service {

    def get(): Future[Response] = action().recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}
