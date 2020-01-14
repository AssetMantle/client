package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SetACL @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_SET_ACL

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "defineACL"

  private val url = ip + ":" + port + "/" + path

  private val chainID = configuration.get[String]("blockchain.main.chainID")

  private implicit val baseRequestWrites: OWrites[BaseReq] = Json.writes[BaseReq]

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  private def action(request: Request): Future[WSResponse] = wsClient.url(url).post(Json.toJson(request))

  case class BaseReq(from: String, chain_id: String = chainID, gas: String)

  case class Request(base_req: BaseReq, password: String, aclAddress: String, organizationID: String, zoneID: String, mode: String, issueAsset: String, issueFiat: String, sendAsset: String, sendFiat: String, redeemAsset: String, redeemFiat: String, buyerExecuteOrder: String, sellerExecuteOrder: String, changeBuyerBid: String, changeSellerBid: String, confirmBuyerBid: String, confirmSellerBid: String, negotiation: String, releaseAsset: String) extends BaseRequest

  object Service {
    def post(request: Request): Future[WSResponse] = action(request).recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}