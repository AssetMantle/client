package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import utilities.MicroLong

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SendFiat @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_SEND_FIAT

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "sendFiat"

  private val url = ip + ":" + port + "/" + path

  private val chainID = configuration.get[String]("blockchain.main.chainID")

  private def action(request: Request): Future[WSResponse] = wsClient.url(url).post(Json.toJson(request))

  case class BaseReq(from: String, chain_id: String = chainID, gas: MicroLong)

  object BaseReq {

    def apply(from: String, chain_id: String, gas: String): BaseReq = new BaseReq(from, chain_id, new MicroLong(gas.toLong))

    def unapply(arg: BaseReq): Option[(String, String, String)] = Option((arg.from, arg.chain_id, arg.gas.toMicroString))

  }

  case class Request(base_req: BaseReq, to: String, amount: MicroLong, pegHash: String, mode: String, password: String) extends BaseRequest

  object Request {

    def apply(base_req: BaseReq, to: String, amount: String, pegHash: String, mode: String, password: String): Request = new Request(base_req, to, new MicroLong(amount.toLong), pegHash, mode, password)

    def unapply(arg: Request): Option[(BaseReq, String, String, String, String, String)] = Option(arg.base_req, arg.to, arg.amount.toMicroString, arg.pegHash, arg.mode, arg.password)
  }

  private implicit val baseRequestWrites: OWrites[BaseReq] = Json.writes[BaseReq]

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  object Service {

    def post(request: Request): Future[WSResponse] = action(request).recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}