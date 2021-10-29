package dbActors

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object Service {

  implicit val actorSystem: ActorSystem = ActorSystem("client",ConfigFactory.load("clustering/clustering.conf"))

}