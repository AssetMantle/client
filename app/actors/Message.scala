package actors

import akka.actor.{ActorPath, ActorRef}
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

  case class Negotiation(ping: String = constants.Comet.PING)

  implicit val negotiationWrites: OWrites[Negotiation] = Json.writes[Negotiation]

  //For CHAT/ MESSAGE -> Takes message directly from masterTransaction.Message.scala

  case class UpdateUsernameActorRef(username: String, actorRef: ActorRef)

  case class ShutdownCometUserActor(username: String)

  case class Email(emailAddress: String, email: constants.Notification.Email, messageParameters: Seq[String])

  case class SMS(mobileNumber: String, sms: constants.Notification.SMS, messageParameters: Seq[String])

  case class PushNotification(token: String, pushNotification: constants.Notification.PushNotification, messageParameters: Seq[String])

}
