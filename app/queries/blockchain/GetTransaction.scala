package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import queries.responses.blockchain.TransactionResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetTransaction @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_TRANSACTION

  private implicit val logger: Logger = Logger(this.getClass)

  private val path = "txs"

  private val url = constants.Blockchain.RestEndPoint + "/" + path + "/"

  private def action(txHash: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + txHash).get)

  private def actionResponseAsWSResponse(txHash: String): Future[WSResponse] = wsClient.url(url + txHash).get

  object Service {

    def get(txHash: String): Future[Response] = action(txHash).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
      case baseException: BaseException => logger.error(constants.Response.TRANSACTION_HASH_QUERY_FAILED.logMessage + ": " + txHash)
        throw baseException
    }

    def getAsWSResponse(txHash: String): Future[WSResponse] = actionResponseAsWSResponse(txHash).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
      case baseException: BaseException => logger.error(constants.Response.TRANSACTION_HASH_QUERY_FAILED.logMessage + ": " + txHash)
        throw baseException
    }
  }

}
