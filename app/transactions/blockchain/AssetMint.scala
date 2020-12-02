package transactions.blockchain

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, Writes}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import utilities.MicroNumber
import views.companion.common._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AssetMint @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_ASSET_MINT

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.ip")

  private val port = configuration.get[String]("blockchain.restPort")

  private val chainID = configuration.get[String]("blockchain.chainID")

  private val path = "xprt/assets/mint"

  private val url = ip + ":" + port + "/" + path

  case class BaseReq(from: String, chain_id: String = chainID, gas: MicroNumber)

  private implicit val baseRequestWrites: Writes[BaseReq] = (baseReq: BaseReq) => Json.obj(
    "from" -> baseReq.from,
    "chain_id" -> baseReq.chain_id,
    "gas" -> baseReq.gas.toMicroString
  )

  case class Message(baseReq: BaseReq, fromID: String, toID: String, classificationID: String, immutableMetaProperties: Seq[Property.Data], immutableProperties: Seq[Property.Data], mutableMetaProperties: Seq[Property.Data], mutableProperties: Seq[Property.Data])

  private implicit val messageWrites: Writes[Message] = (message: Message) => Json.obj(
    "baseReq" -> Json.toJson(message.baseReq),
    "fromID" -> message.fromID,
    "toID" -> message.toID,
    "classificationID" -> message.classificationID,
    "immutableMetaProperties" -> message.immutableMetaProperties.map(_.toRequestString).mkString(constants.Blockchain.RequestPropertiesSeparator),
    "immutableProperties" -> message.immutableProperties.map(_.toRequestString).mkString(constants.Blockchain.RequestPropertiesSeparator),
    "mutableMetaProperties" -> message.mutableMetaProperties.map(_.toRequestString).mkString(constants.Blockchain.RequestPropertiesSeparator),
    "mutableProperties" -> message.mutableProperties.map(_.toRequestString).mkString(constants.Blockchain.RequestPropertiesSeparator)
  )

  case class Request(value: Message) extends BaseRequest

  private implicit val requestWrites: Writes[Request] = (request: Request) => Json.obj(
    "type" -> constants.Blockchain.TransactionRequest.ASSET_MINT,
    "value" -> Json.toJson(request.value)
  )

  private def action(request: Request): Future[WSResponse] = wsClient.url(url).post(Json.toJson(request))

  object Service {

    def post(request: Request): Future[WSResponse] = action(request).recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }

  }

}