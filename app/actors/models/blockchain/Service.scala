package actors.models.blockchain

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.config.ConfigFactory

case class StartActor(actorRef: ActorRef)

object Service {

  implicit val actorSystem: ActorSystem = ActorSystem("client",ConfigFactory.load("clustering/clustering.conf"))

}