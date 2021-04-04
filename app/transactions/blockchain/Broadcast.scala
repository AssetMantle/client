package transactions.blockchain

import exceptions.BaseException
import transactions.request.Serializable.StdTx
import play.api.libs.json.{Json, OWrites, Writes}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import transactions.Abstract.BaseRequest

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Broadcast @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_BROADCAST

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.ip")

  private val port = configuration.get[String]("blockchain.restPort")

  private val chainID = configuration.get[String]("blockchain.chainID")

  private val path = "txs"

  private val url = ip + ":" + port + "/" + path

  case class Request(tx:StdTx, mode:String) extends BaseRequest
  implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  private def action(request: Request): Future[WSResponse] = wsClient.url(url).post(Json.toJson(request))

  object Service {

    def post(request: Request): Future[WSResponse] = action(request).recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }

  }

}