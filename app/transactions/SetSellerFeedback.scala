package transactions

import java.net.ConnectException

import exceptions.BlockChainException
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.responses.TransactionResponse.{AsyncResponse, BlockResponse, KafkaResponse, SyncResponse}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class SetSellerFeedback @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_SET_SELLER_FEEDBACK

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "submitSellerFeedback"

  private val url = ip + ":" + port + "/" + path

  private val chainID = configuration.get[String]("blockchain.main.chainID")

  private def blockAction(request: Request): Future[BlockResponse] = wsClient.url(url).post(Json.toJson(request)).map { response => utilities.JSON.getResponseFromJson[BlockResponse](response) }

  private implicit val baseRequestWrites: OWrites[BaseRequest] = Json.writes[BaseRequest]

  private def kafkaAction(request: Request): Future[KafkaResponse] = wsClient.url(url).post(Json.toJson(request)).map { response => utilities.JSON.getResponseFromJson[KafkaResponse](response) }

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class BaseRequest(from: String, chain_id: String = chainID)

  case class Request(base_req: BaseRequest, password: String, to: String, pegHash: String, rating: Int, mode: String, gas: Int)

  private def asyncAction(request: Request): Future[AsyncResponse] = wsClient.url(url).post(Json.toJson(request)).map { response => utilities.JSON.getResponseFromJson[AsyncResponse](response) }

  private def syncAction(request: Request): Future[SyncResponse] = wsClient.url(url).post(Json.toJson(request)).map { response => utilities.JSON.getResponseFromJson[SyncResponse](response) }

  object Service {
    def blockPost(request: Request): BlockResponse = try {
      Await.result(blockAction(request), Duration.Inf)
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

    def asyncPost(request: Request): AsyncResponse = try {
      Await.result(asyncAction(request), Duration.Inf)
    } catch {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BlockChainException(constants.Response.CONNECT_EXCEPTION)
    }

    def syncPost(request: Request): SyncResponse = try {
      Await.result(syncAction(request), Duration.Inf)
    } catch {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BlockChainException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}
