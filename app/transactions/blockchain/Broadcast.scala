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
import queries.responses.common.Account.SinglePublicKey

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Broadcast @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_BROADCAST

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.ip")

  private val port = configuration.get[String]("blockchain.restPort")

  private val chainID = configuration.get[String]("blockchain.chainID")

  private val path = "txs"

  private val url = ip + ":" + port + "/" + path


  case class Value(from_address:String,to_address:String, amount:Seq[Coin])
  case class Signature(pub_key: SinglePublicKey, signature:String)
  case class SendCoinMessage(messageType:String,value:Value)
  case class Tx(msg:Seq[SendCoinMessage], fee:Fee, signatures: Seq[Signature], memo:String="")
  case class Request(tx:Tx, mode:String)

  //private def action(request: Request): Future[WSResponse] = wsClient.url(url).post(Json.toJson(request))

  object Service {

  /*  def post(request: Request): Future[WSResponse] = action(request).recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }*/

  }

}