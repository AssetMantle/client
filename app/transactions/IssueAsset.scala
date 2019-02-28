package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.Inject
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class IssueAsset @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_ADD_KEY

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "issueAsset"

  private val url = ip + ":" + port + "/" + path

  private def action(request: Request)(implicit executionContext: ExecutionContext): Future[Response] = wsClient.url(url).post(request.json).map { implicit response => new Response() }

  class Response(implicit response: WSResponse) {

    val txHash: String = utilities.JSON.getBCStringResponse("TxHash")

  }


  class Request(from: String, to: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, chainID: String, password: String, gas: Int) {
    val json: JsObject = Json.obj(fields =
      "from" -> from,
      "to" -> to,
      "documentHash" -> documentHash,
      "assetType" -> assetType,
      "assetPrice" -> assetPrice,
      "quantityUnit" -> quantityUnit,
      "assetQuantity" -> assetQuantity,
      "chainID" -> chainID,
      "password" -> password,
      "gas" -> gas
    )
  }

  object Service {
    def post(request: Request)(implicit executionContext: ExecutionContext): Response = try {
      Await.result(action(request), Duration.Inf)
    } catch {
      case connectException: ConnectException =>
        logger.error(constants.Error.CONNECT_EXCEPTION, connectException)
        throw new BaseException(constants.Error.CONNECT_EXCEPTION)
    }
  }

}