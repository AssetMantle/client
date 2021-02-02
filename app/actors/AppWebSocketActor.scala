package actors

import actors.Message.WebSocket._
import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.dispatch.{PriorityGenerator, UnboundedStablePriorityMailbox}
import akka.routing.{BroadcastRoutingLogic, Router}
import com.typesafe.config.Config
import javax.inject.Singleton
import play.api.Logger
import play.api.libs.json.{Json, OWrites}

@Singleton
class AppWebSocketActor extends Actor {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_APP_WEB_SOCKET

  case class Message(messageType: String, messageValue: MessageValue) {
    def asJsonString: String = Json.toJson(Message(messageType = messageType, messageValue = messageValue)).toString
  }

  implicit val messageWrites: OWrites[Message] = Json.writes[Message]

  private var publicRouter: Router = {
    val routees = Vector.empty
    Router(BroadcastRoutingLogic(), routees)
  }

  private var privateActorMap = Map[String, ActorRef]()

  private def broadcastToAll(message: Message): Unit = publicRouter.route(message.asJsonString, sender())

  private def broadcastToUser(username: String, privateMessageContent: PrivateMessageContent, subject: String): Unit = privateActorMap.get(username) match {
    case Some(actorRef) => actorRef ! Message(constants.Actor.MessageType.PRIVATE_MESSAGE, PrivateMessage(subject, privateMessageContent)).asJsonString
    case None => logger.info(username + ": " + constants.Actor.ACTOR_NOT_FOUND)
  }

  def receive = {
    case newBlock: NewBlock => broadcastToAll(Message(constants.Actor.MessageType.NEW_BLOCK, newBlock))
    case blockchainConnectionLost: BlockchainConnectionLost => broadcastToAll(Message(constants.Actor.MessageType.BLOCKCHAIN_CONNECTION_LOST, blockchainConnectionLost))
    case chat: Chat => broadcastToUser(username = chat.toUser, privateMessageContent = chat, subject = constants.Actor.MessageType.CHAT)
    case asset: Asset => broadcastToUser(username = asset.toUser, privateMessageContent = asset, subject = constants.Actor.MessageType.ASSET)
    case fiat: Fiat => broadcastToUser(username = fiat.toUser, privateMessageContent = fiat, subject = constants.Actor.MessageType.FIAT)
    case negotiation: Negotiation => broadcastToUser(username = negotiation.toUser, privateMessageContent = negotiation, subject = constants.Actor.MessageType.NEGOTIATION)
    case order: Order => broadcastToUser(username = order.toUser, privateMessageContent = order, subject = constants.Actor.MessageType.ORDER)
    case privateMessage: PrivateMessage => broadcastToUser(username = privateMessage.messageContent.toUser, privateMessageContent = privateMessage.messageContent, subject = privateMessage.subject)
    case addActor: AddActor => {
      publicRouter = publicRouter.addRoutee(addActor.actorRef)
      addActor.username.map(username => privateActorMap += (username -> addActor.actorRef))
    }
    case removeActor: RemoveActor => {
      publicRouter = publicRouter.removeRoutee(removeActor.actorRef)
      removeActor.username.map(username => privateActorMap -= username)
    }
  }
}

class AppWebSocketActorMailBox(settings: ActorSystem.Settings, config: Config) extends UnboundedStablePriorityMailbox(
  PriorityGenerator {
    case _: RemoveActor => 0
    case _: AddActor => 1
    case _ => 2
  })