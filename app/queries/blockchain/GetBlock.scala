package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.BlockResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetBlock @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_BLOCK

  private implicit val logger: Logger = Logger(this.getClass)

  private val path = "block?height="

  private val url = constants.Blockchain.RPCEndPoint + "/" + path

  private def action(height: Int): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + height).get)

  object Service {

    def get(height: Int): Future[Response] = action(height).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
      case baseException: BaseException => logger.error(constants.Response.BLOCK_QUERY_FAILED.logMessage + ": " + height)
        throw baseException
    }
  }

}
