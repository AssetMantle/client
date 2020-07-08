package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OWrites, Reads}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import utilities.MicroNumber

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IssueFiat @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_ISSUE_FIAT

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "issueFiat"

  private val url = ip + ":" + port + "/" + path

  private val chainID = configuration.get[String]("blockchain.main.chainID")

  private def action(request: Request): Future[WSResponse] = wsClient.url(url).post(Json.toJson(request))

  case class BaseReq(from: String, chain_id: String = chainID, gas: MicroNumber)

  object BaseReq {

    def apply(from: String, chain_id: String, gas: String): BaseReq = new BaseReq(from, chain_id, new MicroNumber(BigInt(gas)))

    def unapply(arg: BaseReq): Option[(String, String, String)] = Option(arg.from, arg.chain_id, arg.gas.toMicroString)

  }

  case class Request(base_req: BaseReq, to: String, transactionID: String, transactionAmount: MicroNumber, mode: String, password: String) extends BaseRequest

  object Request {

    def apply(base_req: BaseReq, to: String, transactionID: String, transactionAmount: String, mode: String, password: String): Request = new Request(base_req, to, transactionID, new MicroNumber(BigInt(transactionAmount)), mode, password)

    def unapply(arg: Request): Option[(BaseReq, String, String, String, String, String)] = Option((arg.base_req, arg.to, arg.transactionID, arg.transactionAmount.toMicroString, arg.mode, arg.password))
  }

  private implicit val baseRequestWrites: OWrites[BaseReq] = Json.writes[BaseReq]
  implicit val baseRequestReads: Reads[BaseReq] = Json.reads[BaseReq]

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]
  implicit val requestReads: Reads[Request] = Json.reads[Request]

  object Service {

    def post(request: Request): Future[WSResponse] = action(request).recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}