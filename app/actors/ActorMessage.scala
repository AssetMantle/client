package actors

import play.api.libs.json.{JsValue, Json, OWrites, Writes}
object ActorMessage {
  //account, asset,fiat, negotiation, order, chat

  case class message[T](messageType: String, messageContent: T)

  case class CometMessage(username: String, message: JsValue)

  def makeCometMessage[T](username:String, messageType: String, messageContent:T)(implicit writes: OWrites[message[T]])={
    CometMessage(username, Json.toJson(message(messageType, messageContent)))
  }

  case class Account()
  private implicit val accountWrites: OWrites[Account] = Json.writes[Account]
  def accountMessage(username: String, account: Account) = makeCometMessage[Account](username, message(constants.Comet.ACCOUNT, account))

  case class Fiat()
  private implicit val fiatWrites: OWrites[Fiat] = Json.writes[Fiat]

}
