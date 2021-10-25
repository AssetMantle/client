package dbActors

import akka.actor.{Actor, ActorRef}
import akka.routing.{BroadcastRoutingLogic, Router}

class RouterActor extends Actor{
  private var privateActorMap = Map[String, ActorRef]()

  private var router: Router = {
    val routees = Vector.empty
    Router(BroadcastRoutingLogic(), routees)
  }

  override def receive: Receive = {
    case "Start" => {
      println("Starting")
      println(self)
    }
    case TryGet(address) =>
      router.route(Get(address), sender())
    case addActor: AddActor => {
      router = router.addRoutee(addActor.actorRef)
      addActor.username.map(username => privateActorMap += (username -> addActor.actorRef))
    }
    case removeActor: RemoveActor => {
      router = router.removeRoutee(removeActor.actorRef)
      removeActor.username.map(username => privateActorMap -= username)
    }
  }

}

case class AddActor(username: Option[String], actorRef: ActorRef)

case class RemoveActor(username: Option[String], actorRef: ActorRef)