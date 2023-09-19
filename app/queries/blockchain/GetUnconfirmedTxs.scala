package queries.blockchain

import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.UnconfirmedTxsResponse

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetUnconfirmedTxs @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  implicit val module: String = constants.Module.QUERIES_GET_UNCONFIRMED_TXS

  implicit val logger: Logger = Logger(this.getClass)

  private val path1 = "/unconfirmed_txs"

  private val path2 = "/unconfirmed_txs?limit="

  private val url1 = constants.Blockchain.RPCEndPoint + path1

  private val url2 = constants.Blockchain.RPCEndPoint + path2

  private def action(): Future[UnconfirmedTxsResponse.Response] = utilities.JSON.getResponseFromJson[UnconfirmedTxsResponse.Response](wsClient.url(url1).get)

  private def actionWithLimit(limit: Int): Future[UnconfirmedTxsResponse.Response] = utilities.JSON.getResponseFromJson[UnconfirmedTxsResponse.Response](wsClient.url(url2 + limit.toString).get)

  object Service {
    def get(): Future[UnconfirmedTxsResponse.Response] = action().recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
    }

    def get(limit: Int): Future[UnconfirmedTxsResponse.Response] = actionWithLimit(limit).recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
    }
  }

}
