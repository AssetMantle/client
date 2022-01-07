package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.AccountResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetAccount @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_ACCOUNT

  private implicit val logger: Logger = Logger(this.getClass)

  private val restURL = configuration.get[String]("blockchain.restURL")

  private val path = "cosmos/auth/v1beta1/accounts"

  private val url = restURL + "/" + path + "/"

  private def action(request: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + request).get)

  object Service {

    def get(address: String): Future[Response] = action(address).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}