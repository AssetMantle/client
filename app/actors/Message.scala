package actors

import akka.actor.{ActorPath, ActorRef}
import models.common.Serializable.Fee
import play.api.libs.json.{JsValue, Json, OWrites, Writes}

object Message {

  private case class message(messageType: String, messageContent: JsValue)

  private implicit val messageWrites: OWrites[message] = Json.writes[message]

  case class CometMessage(username: String, message: JsValue)

  def makeCometMessage[T](username: String, messageType: String, messageContent: T)(implicit writes: OWrites[T]): CometMessage = {
    CometMessage(username, Json.toJson(message(messageType, Json.toJson(messageContent))))
  }

  case class Account(ping: String = constants.Comet.PING)

  implicit val accountWrites: OWrites[Account] = Json.writes[Account]

  case class Fiat(ping: String = constants.Comet.PING)

  implicit val fiatWrites: OWrites[Fiat] = Json.writes[Fiat]

  case class Asset(ping: String = constants.Comet.PING)

  implicit val assetWrites: OWrites[Asset] = Json.writes[Asset]

  case class Order(ping: String = constants.Comet.PING)

  implicit val orderWrites: OWrites[Order] = Json.writes[Order]

  case class Negotiation(id: String)

  implicit val negotiationWrites: OWrites[Negotiation] = Json.writes[Negotiation]

  //For CHAT/ MESSAGE -> Takes message directly from masterTransaction.Message.scala

  case class KeepAlive(ping: String = constants.Comet.PING)

  implicit val keepAliveWrites: OWrites[KeepAlive] = Json.writes[KeepAlive]

  case class UpdateUsernameActorRef(username: String, actorRef: ActorRef)

  case class ShutdownCometUserActor(username: String)

  case class Email(emailAddress: String, email: constants.Notification.Email, messageParameters: Seq[String])

  case class SMS(mobileNumber: String, sms: constants.Notification.SMS, messageParameters: Seq[String])

  case class PushNotification(token: String, pushNotification: constants.Notification.PushNotification, messageParameters: Seq[String])

  object WebSocket {

    case class AddActor(actorRef: ActorRef)

    case class RemoveActor(actorRef: ActorRef)

    case class Block(height: Int, time: String, proposer: String)

    implicit val blockWrites: OWrites[Block] = Json.writes[Block]

    case class Tx(hash: String, status: Boolean, numMsgs: Int, fees: Fee)

    implicit val txWrites: OWrites[Tx] = Json.writes[Tx]

    case class NewBlock(block: Block, txs: Seq[Tx], averageBlockTime: Double, validators: Seq[String])

    implicit val newBlockWrites: OWrites[NewBlock] = Json.writes[NewBlock]

    case class BlockchainConnectionLost(blockchainConnectionLost: Boolean)

    implicit val blockchainConnectionLostWrites: OWrites[BlockchainConnectionLost] = Json.writes[BlockchainConnectionLost]
  }

}
