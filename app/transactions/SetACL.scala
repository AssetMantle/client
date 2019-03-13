package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.Inject
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class SetACL @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_ADD_KEY

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "defineACL"

  private val url = ip + ":" + port + "/" + path

  private def action(request: Request)(implicit executionContext: ExecutionContext): Future[Response] = wsClient.url(url).post(request.json).map { implicit response => new Response() }

  class Response(implicit response: WSResponse) {

    val txHash: String = utilities.JSON.getBCStringResponse("TxHash")

  }


  class Request(from: String, password: String, aclAddress: String, organizationID: String, zoneID: String, chainID: String, issueAsset: Boolean, issueFiat: Boolean, sendAsset: Boolean, sendFiat: Boolean, redeemAsset: Boolean, redeemFiat: Boolean, sellerExecuteOrder: Boolean, buyerExecuteOrder: Boolean, changeBuyerBid: Boolean, changeSellerBid: Boolean, confirmBuyerBid: Boolean, confirmSellerBid: Boolean, negotiation: Boolean, releaseAssets: Boolean) {
    val json: JsObject = Json.obj(fields =
      "from" -> from,
      "password" -> password,
      "aclAddress" -> aclAddress,
      "organizationID" -> organizationID,
      "zoneID" -> zoneID,
      "chainID" -> chainID,
      "issueAsset" -> issueAsset,
      "issueFiat" -> issueFiat,
      "sendAsset" -> sendAsset,
      "sendFiat" -> sendFiat,
      "redeemAsset" -> redeemAsset,
      "redeemFiat" -> redeemFiat,
      "sellerExecuteOrder" -> sellerExecuteOrder,
      "buyerExecuteOrder" -> buyerExecuteOrder,
      "changeBuyerBid" -> changeBuyerBid,
      "changeSellerBid" -> changeSellerBid,
      "confirmBuyerBid" -> confirmBuyerBid,
      "confirmSellerBid" -> confirmSellerBid,
      "negotiation" -> negotiation,
      "releaseAsset" -> releaseAssets


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