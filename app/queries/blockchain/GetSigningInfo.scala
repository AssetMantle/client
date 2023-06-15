package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.Configuration
import play.api.Logger
import queries.responses.blockchain.SigningInfoResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetSigningInfo @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_SIGNING_INFO

  private implicit val logger: Logger = Logger(this.getClass)

  private val path = "cosmos/slashing/v1beta1/signing_infos"

  private val url = constants.Blockchain.RestEndPoint + "/" + path + "/"

  private def action(validatorConsAddress: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + validatorConsAddress).get)

  object Service {

    def get(validatorConsAddress: String): Future[Response] = action(validatorConsAddress).recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
    }
  }

}
