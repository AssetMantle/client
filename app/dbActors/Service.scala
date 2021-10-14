package dbActors

import actors.Service.actorSystem
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory

object Service {
  implicit val actorSystem: ActorSystem = ActorSystem("blockChainActorSystem",ConfigFactory.load("clustering/clustering.conf"))

  implicit val materializer: Materializer = Materializer(actorSystem)

  def startCluster(ports: List[Int]): Unit = ports.foreach{port =>
    println(s"The port is $port")
    val config = ConfigFactory.parseString(
      s"""
         |akka.remote.artery.canonical.port = $port
         |""".stripMargin)
      .withFallback(ConfigFactory.load("clustering/clustering.conf"))

    ActorSystem("blockChainActorSystem", config)
  }

  def createNode(port: Int, role: String, props: Props, actorName: String): ActorRef = {
    val config = ConfigFactory.parseString(
      s"""
         |akka.cluster.roles = ["$role"]
         |akka.remote.artery.canonical.port = $port
       """.stripMargin)
      .withFallback(ConfigFactory.load("clustering/clustering.conf"))

    val system = ActorSystem("blockChainActorSystem", config)
    system.actorOf(props, actorName)
  }



}
