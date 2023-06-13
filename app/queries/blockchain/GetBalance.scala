package queries.blockchain

import exceptions.BaseException
import play.api.{Configuration, Logger}
import play.api.libs.ws.WSClient
import queries.responses.blockchain.BalanceResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetBalance @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_BALANCE

  private implicit val logger: Logger = Logger(this.getClass)

  private val path = "/cosmos/bank/v1beta1/balances/"

  private val url = constants.Blockchain.RestEndPoint + path

  private def action(request: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + request).get)

  object Service {

    def get(address: String): Future[Response] = action(address).recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
    }
  }

}