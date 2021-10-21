package dbActors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import models.blockchain.Balances

object Service {

  implicit val mainConfig = ConfigFactory.load("clustering/clustering.conf")
  implicit val config = mainConfig.getConfig("masterWithGroupRouterApp").withFallback(mainConfig)

  implicit val system = ActorSystem("blockChainActorSystem", config)

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
         |akka.remote.artery.canonical.port = $port
       """.stripMargin)
      .withFallback(ConfigFactory.load("clustering/clustering.conf"))

    val system = ActorSystem("blockchainActorSystem", config)
    system.actorOf(props, actorName)


  }

  def createMasterNode(props: Props, actorName: String): ActorRef = {
    val actor = system.actorOf(props, actorName)
    actor
  }


}