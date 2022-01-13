package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.SigningInfoResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetSigningInfo @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_SIGNING_INFO

  private implicit val logger: Logger = Logger(this.getClass)

  private val path1 = "slashing/validators"

  private val path2 = "signing_info"

  private val url = constants.Blockchain.RestEndPoint + "/" + path1 + "/"

  private def action(validatorConsAddress: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + validatorConsAddress + path2).get)

  object Service {

    def get(validatorConsAddress: String): Future[Response] = action(validatorConsAddress).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}
