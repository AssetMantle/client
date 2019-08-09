package transactions

import java.net.ConnectException

import exceptions.BlockChainException
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.responses.TransactionResponse.{AsyncResponse, BlockResponse, KafkaResponse, SyncResponse}
import utilities.RequestEntity

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class IssueAsset @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_ISSUE_ASSET

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "issueAsset"

  private val url = ip + ":" + port + "/" + path

  private val chainID = configuration.get[String]("blockchain.main.chainID")

  private def blockAction(request: Request): Future[BlockResponse] = wsClient.url(url).post(Json.toJson(request)).map { response => utilities.JSON.getResponseFromJson[BlockResponse](response) }

  private implicit val baseRequestWrites: OWrites[BaseRequest] = Json.writes[BaseRequest]

  private def kafkaAction(request: Request): Future[KafkaResponse] = wsClient.url(url).post(Json.toJson(request)).map { response => utilities.JSON.getResponseFromJson[KafkaResponse](response) }

  case class BaseRequest(from: String, chain_id: String = chainID)

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class Request(base_req: BaseRequest, to: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, mode: String, password: String, gas: Int, moderated: Boolean) extends RequestEntity

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