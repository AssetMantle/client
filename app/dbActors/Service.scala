package dbActors

import actors.Service.actorSystem
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory

object Service {
  implicit val actorSystem: ActorSystem = ActorSystem("blockChainActorSystem",ConfigFactory.load)

  implicit val materializer: Materializer = Materializer(actorSystem)

}
