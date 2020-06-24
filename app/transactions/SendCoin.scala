package transactions

import java.net.ConnectException

import controllers.routes
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OWrites, Reads}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SendCoin @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_SEND_COIN

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val chainID = configuration.get[String]("blockchain.main.chainID")

  private val path1 = "bank/accounts/"

  private val path2 = "/transfers"

  private val url = ip + ":" + port + "/" + path1

  //testURL
  //private val testURL = constants.Test.BASE_URL+"/loopback"+ "/" + "bank/accounts"

  private def action(request: Request): Future[WSResponse] = wsClient.url(url + request.to + path2).post(Json.toJson(request))

  case class Amount(denom: String, amount: String)

  case class BaseReq(from: String, chain_id: String = chainID, gas: String)

  private implicit val baseRequestWrites: OWrites[BaseReq] = Json.writes[BaseReq]
  implicit val baseRequestReads: Reads[BaseReq] = Json.reads[BaseReq]

  private implicit val amountWrites: OWrites[Amount] = Json.writes[Amount]
  implicit val amountReads: Reads[Amount] = Json.reads[Amount]

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]
  implicit val requestReads: Reads[Request] = Json.reads[Request]

  case class Request(base_req: BaseReq, password: String, to: String, amount: Seq[Amount], mode: String) extends BaseRequest

  object Service {

    def post(request: Request): Future[WSResponse] = action(request).recover {
      case connectException: ConnectException =>
        logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }

  }

}