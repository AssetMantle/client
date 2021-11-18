package actors.models

import akka.actor.{ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

case class StartActor(actorRef: ActorRef)

object Service {

  implicit val actorSystem: ActorSystem = ActorSystem("client",ConfigFactory.load("clustering/clustering.conf"))

}