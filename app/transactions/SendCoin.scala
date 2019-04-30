package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.Inject
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import transactions.Response.TransactionResponse.{KafkaResponse, Response}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class SendCoin @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_SEND_COIN

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val chainID = configuration.get[String]("blockchain.main.chainID")

  private val path = "sendCoin"

  private val url = ip + ":" + port + "/" + path

  private def action(request: Request)(implicit executionContext: ExecutionContext): Future[Response] = wsClient.url(url).post(Json.toJson(request)).map { response => utilities.JSON.getResponseFromJson[Response](response) }

  private implicit val amountWrites: OWrites[Amount] = Json.writes[Amount]

  private def kafkaAction(request: Request)(implicit executionContext: ExecutionContext): Future[KafkaResponse] = wsClient.url(url).post(Json.toJson(request)).map { response => utilities.JSON.getResponseFromJson[KafkaResponse](response)}

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class Amount(denom: String, amount: String)

  case class Request(from: String, password: String, to: String, amount: Seq[Amount], chainID: String = chainID, gas: Int)

  object Service {
    def post(request: Request)(implicit executionContext: ExecutionContext): Response = try {
      Await.result(action(request), Duration.Inf)
    } catch {
      case connectException: ConnectException =>
        logger.error(constants.Error.CONNECT_EXCEPTION, connectException)
        throw new BaseException(constants.Error.CONNECT_EXCEPTION)
    }

    def kafkaPost(request: Request)(implicit executionContext: ExecutionContext): KafkaResponse = try {
      Await.result(kafkaAction(request), Duration.Inf)
    } catch {
      case connectException: ConnectException =>
        logger.error(constants.Error.CONNECT_EXCEPTION, connectException)
        throw new BaseException(constants.Error.CONNECT_EXCEPTION)
    }

    def getTxHashFromWSResponse(wsResponse: WSResponse): String = utilities.JSON.getResponseFromJson[Response](wsResponse).TxHash

    def getTxFromWSResponse(wsResponse: WSResponse): Response = utilities.JSON.getResponseFromJson[Response](wsResponse)

  }

}