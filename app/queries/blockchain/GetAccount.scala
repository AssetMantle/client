package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.AccountResponse.Response
import queries.responses.common.Accounts.BaseAccount

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetAccount @Inject()()(implicit wsClient: WSClient, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_ACCOUNT

  private implicit val logger: Logger = Logger(this.getClass)

  private val path = "cosmos/auth/v1beta1/accounts"

  private val url = constants.Blockchain.RestEndPoint + "/" + path + "/"

  private val AccountNotFoundRegex = """account.*not.found.*key.not.found""".r

  private def action(request: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + request).get)

  object Service {

    def get(address: String): Future[Response] = action(address).recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
      case baseException: BaseException => if (AccountNotFoundRegex.findFirstIn(baseException.failure.message).isDefined) {
        Response(BaseAccount(address = address, pub_key = None, account_number = "-1", sequence = "0"))
      } else throw baseException
    }
  }
}