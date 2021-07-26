package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.TransactionByHeightResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetTransactionsByHeight @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_TRANSACTION_BY_HEIGHT

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.ip")

  private val port = configuration.get[String]("blockchain.abciPort")

  private val url = ip + ":" + port + "/"

  private def action(height: Int, page: Int, perPage: Int): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + s"""tx_search?query="tx.height=${height}"&page=${page}&per_page=${perPage}""").get)

  object Service {
    def get(height: Int, perPage: Int, page: Int): Future[Response] = action(height = height, page = page, perPage = perPage).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
      case baseException: BaseException => logger.error(constants.Response.TRANSACTION_BY_HEIGHT_QUERY_FAILED.logMessage + ": " + height)
        throw baseException
    }
  }

}
