package dbActors

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object Service {

  val config1 = ConfigFactory.parseString(
    s"""
       |akka.remote.artery.canonical.port = 2551
       |""".stripMargin)
    .withFallback(ConfigFactory.load("clustering/clustering.conf"))
  val config2 = ConfigFactory.parseString(
    s"""
       |akka.remote.artery.canonical.port = 2552
       |""".stripMargin)
    .withFallback(ConfigFactory.load("clustering/clustering.conf"))

  implicit val actorSystem1: ActorSystem = ActorSystem("blockChainActorSystem",config1)
  implicit val actorSystem: ActorSystem = ActorSystem("blockChainActorSystem",config1)

  implicit val actorSystem2: ActorSystem = ActorSystem("blockChainActorSystem",config2)


  val routerActor = actorSystem2.actorOf(Props[RouterActor](), "routerActor")

  val masterActor = actorSystem1.actorOf(Props[Master](), "master")


}