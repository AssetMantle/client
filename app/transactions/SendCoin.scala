package transactions

import java.net.ConnectException

import exceptions.BlockChainException
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.responses.TransactionResponse.{BlockResponse, KafkaResponse}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class SendCoin @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_SEND_COIN

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val chainID = configuration.get[String]("blockchain.main.chainID")

  private val path = "sendCoin"

  private val url = ip + ":" + port + "/" + path

  private def action(request: Request): Future[BlockResponse] = wsClient.url(url).post(Json.toJson(request)).map { response => utilities.JSON.getResponseFromJson[BlockResponse](response) }

  private implicit val amountWrites: OWrites[Amount] = Json.writes[Amount]

  private def kafkaAction(request: Request): Future[KafkaResponse] = wsClient.url(url).post(Json.toJson(request)).map { response => utilities.JSON.getResponseFromJson[KafkaResponse](response) }

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class Amount(denom: String, amount: String)

  case class BaseRequest(from: String, chain_id: String = chainID)

  private implicit val baseRequestWrites: OWrites[BaseRequest] = Json.writes[BaseRequest]

  case class Request(base_req: BaseRequest, password: String, to: String, amount: Seq[Amount], mode: String, gas: Int)

  object Service {
    def post(request: Request): BlockResponse = try {
      Await.result(action(request), Duration.Inf)
    } catch {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BlockChainException(constants.Response.CONNECT_EXCEPTION)
    }

    def kafkaPost(request: Request): KafkaResponse = try {
      Await.result(kafkaAction(request), Duration.Inf)
    } catch {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BlockChainException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}