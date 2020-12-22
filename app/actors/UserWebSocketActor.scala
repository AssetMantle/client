package actors

import actors.Message.WebSocket._
import akka.actor._

object UserWebSocketActor {
  def props(out: ActorRef) = Props(new UserWebSocketActor(out))
}

class UserWebSocketActor(out: ActorRef) extends Actor {
  def receive = {
    case msg: String => if (msg == "START") actors.Service.appWebSocketActor ! AddPublicActor(out)
  }

  override def postStop(): Unit = actors.Service.appWebSocketActor ! RemovePublicActor(out)
}
