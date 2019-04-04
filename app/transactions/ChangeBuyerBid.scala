package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.Inject
import play.api.libs.json.{JsObject, Json, OWrites}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import transactions.Response.TransactionResponse.{KafkaResponse, Response}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class ChangeBuyerBid @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_CHANGE_BUYER_BID

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "changeBuyerBid"

  private val url = ip + ":" + port + "/" + path

  private val chainID = configuration.get[String]("blockchain.main.chainID")

  case class Request(from: String, password: String, to: String, bid: Int, time: Int, pegHash: String, chainID: String = chainID, gas: Int)

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  private def action(request: Request)(implicit executionContext: ExecutionContext): Future[Response] = wsClient.url(url).post(Json.toJson(request)).map { response => utilities.JSON.getResponseFromJson[Response](response) }

  private def kafkaAction(request: Request)(implicit executionContext: ExecutionContext): Future[KafkaResponse] = wsClient.url(url).post(Json.toJson(request)).map { response => utilities.JSON.getResponseFromJson[KafkaResponse](response)}

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
  }
}