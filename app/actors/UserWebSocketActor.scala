package actors

import actors.Message.WebSocket._
import akka.actor._

object UserWebSocketActor {
  def props(username: Option[String], out: ActorRef) = Props(new UserWebSocketActor(username, out))
}

class UserWebSocketActor(username: Option[String], out: ActorRef) extends Actor {
  def receive = {
    case msg: String => if (msg == "START") actors.Service.appWebSocketActor ! AddActor(username, out)
  }

  override def postStop(): Unit = actors.Service.appWebSocketActor ! RemoveActor(username, out)
}
