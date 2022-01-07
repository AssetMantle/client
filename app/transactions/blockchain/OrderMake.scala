package transactions.blockchain

import exceptions.BaseException
import models.common.Serializable._
import play.api.libs.json.{Json, Writes}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import utilities.MicroNumber

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrderMake @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_ORDER_MAKE

  private implicit val logger: Logger = Logger(this.getClass)

  private val restURL = configuration.get[String]("blockchain.restURL")

  private val chainID = configuration.get[String]("blockchain.chainID")

  private val path = "xprt/orders/make"

  private val url = restURL + "/" + path

  case class BaseReq(from: String, chain_id: String = chainID, gas: MicroNumber)

  private implicit val baseRequestWrites: Writes[BaseReq] = (baseReq: BaseReq) => Json.obj(
    "from" -> baseReq.from,
    "chain_id" -> baseReq.chain_id,
    "gas" -> baseReq.gas.toMicroString
  )

  case class Message(baseReq: BaseReq, fromID: String, classificationID: String, makerOwnableID: String, takerOwnableID: String, expiresIn: Int, makerOwnableSplit: BigDecimal, immutableMetaProperties: Seq[BaseProperty], immutableProperties: Seq[BaseProperty], mutableMetaProperties: Seq[BaseProperty], mutableProperties: Seq[BaseProperty])

  private implicit val messageWrites: Writes[Message] = (message: Message) => Json.obj(
    "baseReq" -> Json.toJson(message.baseReq),
    "fromID" -> message.fromID,
    "classificationID" -> message.classificationID,
    "makerOwnableID" -> message.makerOwnableID,
    "takerOwnableID" -> message.takerOwnableID,
    "expiresIn" -> message.expiresIn.toString,
    "makerOwnableSplit" -> message.makerOwnableSplit.bigDecimal.toPlainString,
    "immutableMetaProperties" -> message.immutableMetaProperties.map(_.toRequestString).mkString(constants.Blockchain.RequestPropertiesSeparator),
    "immutableProperties" -> message.immutableProperties.map(_.toRequestString).mkString(constants.Blockchain.RequestPropertiesSeparator),
    "mutableMetaProperties" -> message.mutableMetaProperties.map(_.toRequestString).mkString(constants.Blockchain.RequestPropertiesSeparator),
    "mutableProperties" -> message.mutableProperties.map(_.toRequestString).mkString(constants.Blockchain.RequestPropertiesSeparator)
  )

  case class Request(value: Message) extends BaseRequest

  private implicit val requestWrites: Writes[Request] = (request: Request) => Json.obj(
    "type" -> constants.Blockchain.TransactionRequest.ORDER_MAKE,
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