package dbActors

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.routing.{ RoundRobinRoutingLogic, Router}



class Master extends Actor with ActorLogging {
  private var privateActorMap = Map[String, ActorRef]()

  private var router: Router = {
    val routees = Vector.empty
    Router(RoundRobinRoutingLogic(), routees)
  }

  override def receive: Receive = {
    case "Start" => println("Master Starting")
    case Get(address) => {
      router.route(TryGet(address), sender())
    }
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