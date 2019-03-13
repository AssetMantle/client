package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.Inject
import play.api.libs.json.{JsObject, Json, OWrites}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class SendCoin @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_ADD_KEY

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val chainID = configuration.get[String]("blockchain.main.chainID")

  private val path = "sendCoin"

  private val url = ip + ":" + port + "/" + path

  case class Amount(denom: String, amount: String)

  private implicit val amountWrites: OWrites[Amount] = Json.writes[Amount]

  case class Request(from: String, password: String, to: String, amount: Seq[Amount], gas: Int)

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  private def action(request: Request)(implicit executionContext: ExecutionContext): Future[Response] = wsClient.url(url).post(Json.toJson(request)).map { implicit response => new Response() }

  class Response(implicit response: WSResponse) {

    val txHash: String = utilities.JSON.getBCStringResponse("TxHash")

  }

  object Service {
    def post(request: Request)(implicit executionContext: ExecutionContext): Response = try {
      Await.result(action(request), Duration.Inf)
    } catch {
      case connectException: ConnectException =>
        logger.error(constants.Error.CONNECT_EXCEPTION, connectException)
        throw new BaseException(constants.Error.CONNECT_EXCEPTION)
    }

    def getTxHashFromWSResponse(wsResponse: WSResponse): String  = new Response()(wsResponse).txHash
  }

}