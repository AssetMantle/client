package actors

import akka.actor._
import actors.Message.WebSocket._

object UserWebSocketActor {
  def props(out: ActorRef) = Props(new UserWebSocketActor(out))
}

class UserWebSocketActor(out: ActorRef) extends Actor {
  def receive = {
    case msg: String => if (msg == "START") actors.Service.appWebSocketActor ! AddActor(out)
  }

  override def postStop(): Unit = {
    actors.Service.appWebSocketActor ! RemoveActor(out)
  }
}
