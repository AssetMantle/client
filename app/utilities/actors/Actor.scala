package utilities.actors

import akka.actor._
import akka.stream.ActorMaterializer

object Actor {
  val system: ActorSystem = ActorSystem()

  implicit val materializer: ActorMaterializer = ActorMaterializer()(system)
}