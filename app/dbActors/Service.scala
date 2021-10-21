package dbActors

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object Service {
  implicit val actorSystem: ActorSystem = ActorSystem("blockChainActorSystem",ConfigFactory.load("clustering/clustering.conf"))

  val routerActor = actorSystem.actorOf(Props[RouterActor](), "routerActor")

}
