package dbActors

import akka.actor.{ActorSystem, Props}
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.ConfigFactory

object Service {


  implicit val actorSystem: ActorSystem = ActorSystem("client",ConfigFactory.load("clustering/clustering.conf"))

  implicit val actorSystem1: ActorSystem = ActorSystem("client",ConfigFactory.load("clustering/clustering.conf"))

  implicit val actorSystem2: ActorSystem = ActorSystem("client",ConfigFactory.load("clustering/clustering.conf"))


  val routerActor = actorSystem1.actorOf(Props[RouterActor](), "routerActor")

  val masterActor = actorSystem1.actorOf(Props[Master](), "master")

  def startAkka() = {
    AkkaManagement(actorSystem1).start()
    ClusterBootstrap(actorSystem1).start()
  }



}