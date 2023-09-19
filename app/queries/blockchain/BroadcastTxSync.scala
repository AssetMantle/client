package queries.blockchain

import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.BroadcastTxSyncResponse

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BroadcastTxSync @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  implicit val module: String = constants.Module.BROADCAST_TX_SYNC

  implicit val logger: Logger = Logger(this.getClass)

  private val url = constants.Blockchain.RPCEndPoint + "/broadcast_tx_sync?tx=0x"

  private def action(txRawHex: String) = utilities.JSON.getSuccessErrorResponseFromJson[BroadcastTxSyncResponse.Response, BroadcastTxSyncResponse.ErrorResponse](wsClient.url(url + txRawHex).get)

  object Service {
    def get(txRawHex: String): Future[(Option[BroadcastTxSyncResponse.Response], Option[BroadcastTxSyncResponse.ErrorResponse])] = action(txRawHex).recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
    }
  }

}
