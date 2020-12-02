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
class OrderDefine @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_ORDER_DEFINE

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.ip")

  private val port = configuration.get[String]("blockchain.restPort")

  private val chainID = configuration.get[String]("blockchain.chainID")

  private val path = "xprt/orders/define"

  private val url = ip + ":" + port + "/" + path

  case class BaseReq(from: String, chain_id: String = chainID, gas: MicroNumber)

  private implicit val baseRequestWrites: Writes[BaseReq] = (baseReq: BaseReq) => Json.obj(
    "from" -> baseReq.from,
    "chain_id" -> baseReq.chain_id,
    "gas" -> baseReq.gas.toMicroString
  )

  case class Message(baseReq: BaseReq, fromID: String, immutableMetaTraits: Seq[Property.Data], immutableTraits: Seq[Property.Data], mutableMetaTraits: Seq[Property.Data], mutableTraits: Seq[Property.Data])

  private implicit val messageWrites: Writes[Message] = (message: Message) => Json.obj(
    "baseReq" -> Json.toJson(message.baseReq),
    "fromID" -> message.fromID,
    "immutableMetaTraits" -> message.immutableMetaTraits.map(_.toRequestString).mkString(constants.Blockchain.RequestPropertiesSeparator),
    "immutableTraits" -> message.immutableTraits.map(_.toRequestString).mkString(constants.Blockchain.RequestPropertiesSeparator),
    "mutableMetaTraits" -> message.mutableMetaTraits.map(_.toRequestString).mkString(constants.Blockchain.RequestPropertiesSeparator),
    "mutableTraits" -> message.mutableTraits.map(_.toRequestString).mkString(constants.Blockchain.RequestPropertiesSeparator)
  )

  case class Request(value: Message) extends BaseRequest

  private implicit val requestWrites: Writes[Request] = (request: Request) => Json.obj(
    "type" -> constants.Blockchain.TransactionRequest.ORDER_DEFINE,
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