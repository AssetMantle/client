package actors

import actors.Message.WebSocket._
import akka.actor.Actor
import akka.routing.{BroadcastRoutingLogic, Router}

class AppWebSocketActor extends Actor {

  var router: Router = {
    val routees = Vector.empty
    Router(BroadcastRoutingLogic(), routees)
  }

  def receive = {
    case message: String => router.route(message, sender())
    case addActor: AddActor => router = router.addRoutee(addActor.actorRef)
    case removeActor: RemoveActor => router = router.removeRoutee(removeActor.actorRef)
  }

}