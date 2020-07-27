package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import utilities.MicroNumber

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddZone @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_ADD_ZONE

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "defineZone"

  private val url = ip + ":" + port + "/" + path

  private val chainID = configuration.get[String]("blockchain.main.chainID")

  private def action(request: Request): Future[WSResponse] = wsClient.url(url).post(Json.toJson(request))

  case class BaseReq(from: String, chain_id: String = chainID, gas: MicroNumber)

  object BaseReq {

    def apply(from: String, chain_id: String, gas: String): BaseReq = new BaseReq(from, chain_id, new MicroNumber(BigInt(gas)))

    def unapply(arg: BaseReq): Option[(String, String, String)] = Option((arg.from, arg.chain_id, arg.gas.toMicroString))

  }

  private implicit val baseRequestWrites: OWrites[BaseReq] = Json.writes[BaseReq]

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class Request(base_req: BaseReq, to: String, zoneID: String, password: String, mode: String) extends BaseRequest

  object Service {
    def post(request: Request): Future[WSResponse] = action(request).recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}