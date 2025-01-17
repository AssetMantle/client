package queries.blockchain

import exceptions.BaseException
import play.api.{Configuration, Logger}
import play.api.libs.ws.WSClient
import queries.responses.blockchain.BlockCommitResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetBlockCommit @Inject()()(implicit wsClient: WSClient, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_BLOCK_COMMIT

  private implicit val logger: Logger = Logger(this.getClass)

  private val path = "commit?height="

  private val url = constants.Blockchain.RPCEndPoint + "/" + path

  private def action(height: Int): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + height).get)

  object Service {
    def get(height: Int): Future[Response] = action(height).recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
      case baseException: BaseException => if (baseException.failure == constants.Response.JSON_UNMARSHALLING_ERROR) {
        logger.error(constants.Response.BLOCK_QUERY_FAILED.logMessage + ": " + height)
        constants.Response.BLOCK_QUERY_FAILED.throwBaseException()
      } else throw baseException
    }
  }

}
