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
class ConfirmSellerBid @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_CONFIRM_SELLER_BID

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "confirmSellerBid"

  private val url = ip + ":" + port + "/" + path

  private val chainID = configuration.get[String]("blockchain.main.chainID")

  case class BaseReq(from: String, chain_id: String = chainID, gas: MicroLong)

  object BaseReq {

    def apply(from: String, chain_id: String, gas: String): BaseReq = new BaseReq(from, chain_id, new MicroLong(gas.toLong))

    def unapply(arg: BaseReq): Option[(String, String, String)] = Option(arg.from, arg.chain_id, arg.gas.toMicroString)

  }

  private implicit val baseRequestWrites: OWrites[BaseReq] = Json.writes[BaseReq]

  case class Request(base_req: BaseReq, to: String, bid: MicroLong, time: String, pegHash: String, sellerContractHash: String, mode: String, password: String) extends BaseRequest

  object Request {

    def apply(base_req: BaseReq, to: String, bid: String, time: String, pegHash: String, sellerContractHash: String, mode: String, password: String): Request = new Request(base_req, to, new MicroLong(bid.toLong), time, pegHash, sellerContractHash, mode, password)

    def unapply(arg: Request): Option[(BaseReq, String, String, String, String, String, String, String)] = Option((arg.base_req, arg.to, arg.bid.toMicroString, arg.time, arg.pegHash, arg.sellerContractHash, arg.mode, arg.password))

  }

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  private def action(request: Request): Future[WSResponse] = wsClient.url(url).post(Json.toJson(request))

  object Service {

    def post(request: Request): Future[WSResponse] = action(request).recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}