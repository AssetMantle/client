package queries

import java.net.{ConnectException, UnknownHostException}

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetTransactionHashResponse @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_TRANSACTION_HASH_RESPONSE

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "txs"

  private val url = ip + ":" + port + "/" + path + "/"

  private def action(request: String): Future[WSResponse] = wsClient.url(url + request).get

  object Service {

    def get(txHash: String): Future[WSResponse] = action(txHash).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
      case unknownHostException: UnknownHostException => throw new BaseException(constants.Response.UNKNOWN_HOST_EXCEPTION, unknownHostException)
    }
  }

}
