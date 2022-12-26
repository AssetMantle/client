package actors

import akka.actor.ActorRef
import models.common.Fee
import play.api.libs.json.{Json, OWrites, Writes}

object Message {

  case class Email(emailAddress: String, email: constants.Notification.Email, messageParameters: Seq[String])

  case class SMS(mobileNumber: String, sms: constants.Notification.SMS, messageParameters: Seq[String])

  case class PushNotification(token: String, pushNotification: constants.Notification.PushNotification, messageParameters: Seq[String])

  object WebSocket {

    case class AddActor(username: Option[String], actorRef: ActorRef)

    case class RemoveActor(username: Option[String], actorRef: ActorRef)

    abstract class MessageValue

    case class Block(height: Int, time: String, proposer: String)

    implicit val blockWrites: OWrites[Block] = Json.writes[Block]

    case class Tx(hash: String, status: Boolean, numMsgs: Int, messageTypes: String, fees: Fee)

    implicit val txWrites: OWrites[Tx] = Json.writes[Tx]

    case class NewBlock(block: Block, txs: Seq[Tx], averageBlockTime: Double, validators: Seq[String]) extends MessageValue

    implicit val newBlockWrites: OWrites[NewBlock] = Json.writes[NewBlock]

    case class BlockchainConnectionLost(blockchainConnectionLost: Boolean) extends MessageValue

    implicit val blockchainConnectionLostWrites: OWrites[BlockchainConnectionLost] = Json.writes[BlockchainConnectionLost]

    abstract class PrivateMessageContent {
      val toUser: String
    }

    case class Chat(toUser: String, chatID: String) extends PrivateMessageContent

    implicit val chatWrites: OWrites[Chat] = Json.writes[Chat]

    case class Asset(toUser: String, ping: String = constants.Actor.MessageType.PING) extends PrivateMessageContent

    implicit val assetWrites: OWrites[Asset] = Json.writes[Asset]

    implicit val privateMessageContentWrites: Writes[PrivateMessageContent] = {
      case chat: Chat => Json.toJson(chat)
      case asset: Asset => Json.toJson(asset)
    }

    case class PrivateMessage(subject: String, messageContent: PrivateMessageContent) extends MessageValue

    implicit val privateMessageWrites: OWrites[PrivateMessage] = Json.writes[PrivateMessage]

    implicit val messageValueWrites: Writes[MessageValue] = {
      case newBlock: NewBlock => Json.toJson(newBlock)
      case blockchainConnectionLost: BlockchainConnectionLost => Json.toJson(blockchainConnectionLost)
      case privateMessage: PrivateMessage => Json.toJson(privateMessage)
    }

  }

}
