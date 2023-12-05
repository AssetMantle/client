package queries.blockchain

import exceptions.BaseException
import play.api.Logger
import play.api.libs.ws.WSClient
import queries.responses.blockchain.TransactionByHashResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetTransactionsByHash @Inject()()(implicit wsClient: WSClient, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_TRANSACTION_BY_HASH

  private implicit val logger: Logger = Logger(this.getClass)

  private val url = constants.Blockchain.RPCEndPoint + "/tx?hash=0x"

  private def action(hash: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + hash).get)

  object Service {
    def get(hash: String): Future[Response] = action(hash).recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
      case baseException: BaseException => logger.error(constants.Response.TRANSACTION_BY_HASH_QUERY_FAILED.logMessage + ": " + hash)
        throw baseException
    }
  }

}
