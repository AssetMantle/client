package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import utilities.MicroLong

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IssueAsset @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_ISSUE_ASSET

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "issueAsset"

  private val url = ip + ":" + port + "/" + path

  private val chainID = configuration.get[String]("blockchain.main.chainID")

  private def action(request: Request): Future[WSResponse] = wsClient.url(url).post(Json.toJson(request))

  case class BaseReq(from: String, chain_id: String = chainID, gas: MicroLong)

  object BaseReq {

    def apply(from: String, chain_id: String, gas: String): BaseReq = new BaseReq(from, chain_id, new MicroLong(gas.toLong))

    def unapply(arg: BaseReq): Option[(String, String, String)] = Option((arg.from, arg.chain_id, arg.gas.toMicroString))

  }

  case class Request(base_req: BaseReq, to: String, documentHash: String, assetType: String, assetPrice: MicroLong, quantityUnit: String, assetQuantity: MicroLong, takerAddress: String, mode: String, password: String, moderated: Boolean) extends BaseRequest

  object Request {

    def apply(base_req: BaseReq, to: String, documentHash: String, assetType: String, assetPrice: String, quantityUnit: String, assetQuantity: String, takerAddress: String, mode: String, password: String, moderated: Boolean): Request = new Request(base_req, to, documentHash, assetType, new MicroLong(assetPrice.toLong), quantityUnit, new MicroLong(assetQuantity.toLong), takerAddress, mode, password, moderated)

    def unapply(arg: Request): Option[(BaseReq, String, String, String, String, String, String, String, String, String, Boolean)] = Option(arg.base_req, arg.to, arg.documentHash, arg.assetType, arg.assetPrice.toMicroString, arg.quantityUnit, arg.assetQuantity.toMicroString, arg.takerAddress, arg.mode, arg.password, arg.moderated)
  }

  private implicit val baseRequestWrites: OWrites[BaseReq] = Json.writes[BaseReq]

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  object Service {

    def post(request: Request): Future[WSResponse] = action(request).recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }

  }

}