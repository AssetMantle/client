package actors

import play.api.libs.json.{JsValue, Json, OWrites, Writes}
object ActorMessage {

  //account, asset,fiat, negotiation, order, chat

  case class message(messageType: String, messageContent: JsValue)

  private implicit val messageWrites: OWrites[message] = Json.writes[message]

  case class CometMessage(username: String, message: JsValue)

  def makeCometMessage[T](username: String, messageType: String, messageContent: T)(implicit writes: OWrites[T]) = {
    CometMessage(username, Json.toJson(message(messageType, Json.toJson(messageContent))))
  }

  case class Account()
  private implicit val accountWrites: OWrites[Account] = Json.writes[Account]


  case class Fiat()
  private implicit val fiatWrites: OWrites[Fiat] = Json.writes[Fiat]

  case class Asset()
  private implicit val assetWrites: OWrites[Asset] = Json.writes[Asset]

  case class Order()
  private implicit val orderWrites: OWrites[Order] = Json.writes[Order]

  case class Negotiation()
  private implicit val negotiationWrites: OWrites[Negotiation] = Json.writes[Negotiation]

  case class Chat()
  private implicit val chatWrites: OWrites[Chat] = Json.writes[Chat]

}
