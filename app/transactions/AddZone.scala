package transactions

import java.net.ConnectException

import exceptions.BlockChainException
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class AddZone @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_ADD_ZONE

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "defineZone"

  private val url = ip + ":" + port + "/" + path

  private val chainID = configuration.get[String]("blockchain.main.chainID")

  private implicit val baseRequestWrites: OWrites[BaseReq] = Json.writes[BaseReq]

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  private def action(request: Request): Future[WSResponse] = wsClient.url(url).post(Json.toJson(request))

  case class BaseReq(from: String, chain_id: String = chainID, gas: String)

  case class Request(base_req: BaseReq, to: String, zoneID: String, password: String, mode: String) extends BaseRequest

  object Service {
    def post(request: Request): WSResponse = try {
      Await.result(action(request), Duration.Inf)
    } catch {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BlockChainException(constants.Response.CONNECT_EXCEPTION)
    }

  }

}