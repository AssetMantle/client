package transactions.blockchain

import exceptions.BaseException
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import utilities.MicroNumber

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SendCoin @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_SEND_COIN

  private implicit val logger: Logger = Logger(this.getClass)

  private val restURL = configuration.get[String]("blockchain.restURL")

  private val chainID = configuration.get[String]("blockchain.chainID")

  private val path1 = "bank/accounts/"

  private val path2 = "/transfers"

  private val url = restURL + "/" + path1

  case class Amount(denom: String, amount: MicroNumber)

  object Amount {

    def apply(denom: String, amount: String): Amount = new Amount(denom, new MicroNumber(BigInt(amount)))

    def unapply(arg: Amount): Option[(String, String)] = Option(arg.denom, arg.amount.toMicroString)

  }

  case class BaseReq(from: String, chain_id: String = chainID, gas: MicroNumber)

  object BaseReq {

    def apply(from: String, chain_id: String, gas: String): BaseReq = new BaseReq(from, chain_id, new MicroNumber(BigInt(gas)))

    def unapply(arg: BaseReq): Option[(String, String, String)] = Option((arg.from, arg.chain_id, arg.gas.toMicroString))

  }

  private implicit val amountWrites: OWrites[Amount] = Json.writes[Amount]

  private implicit val baseRequestWrites: OWrites[BaseReq] = Json.writes[BaseReq]

  case class Request(base_req: BaseReq, password: String, to: String, amount: Seq[Amount], mode: String) extends BaseRequest

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  private def action(request: Request): Future[WSResponse] = wsClient.url(url + request.to + path2).post(Json.toJson(request))

  object Service {

    def post(request: Request): Future[WSResponse] = action(request).recover {
      case connectException: ConnectException =>
        logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }

  }

}