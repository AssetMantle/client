package actors

import actors.Message.WebSocket._
import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.dispatch.{PriorityGenerator, UnboundedStablePriorityMailbox}
import akka.routing.{BroadcastRoutingLogic, Router}
import com.typesafe.config.Config
import play.api.Logger
import play.api.libs.json.{Json, OWrites}

import javax.inject.Singleton

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
    case privateMessage: PrivateMessage => broadcastToUser(username = privateMessage.messageContent.toUser, privateMessageContent = privateMessage.messageContent, subject = privateMessage.subject)
    case addPublicActor: AddPublicActor => publicRouter = publicRouter.addRoutee(addPublicActor.actorRef)
    case addPrivateActor: AddPrivateActor => privateActorMap += (addPrivateActor.username -> addPrivateActor.actorRef)
    case removePublicActor: RemovePublicActor => publicRouter = publicRouter.removeRoutee(removePublicActor.actorRef)
    case removePrivateActor: RemovePrivateActor => privateActorMap -= removePrivateActor.username
  }

}

class AppWebSocketActorMailBox(settings: ActorSystem.Settings, config: Config) extends UnboundedStablePriorityMailbox(
  PriorityGenerator {
    case _: RemovePublicActor => 0
    case _: RemovePrivateActor => 1
    case _: AddPublicActor => 2
    case _: AddPrivateActor => 3
    case _ => 4
  })