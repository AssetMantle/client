package utilities.actors

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import models.blockchain

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

object Asset {
  def props(system: ActorSystem) = Props(new Asset(system))
}

class Asset(system: ActorSystem) extends Actor with ActorLogging {

  private implicit val timeout = Timeout(FiniteDuration(1, TimeUnit.SECONDS))

  private implicit val ec: ExecutionContext = context.dispatcher

  def receive = {
    case assetMessage: blockchain.AssetMessage =>
      system.actorSelection("/user/"  + assetMessage.ownerAddress).resolveOne().onComplete {
        case Success(actorRef) => println("*************************************************")
          actorRef ! assetMessage
        case Failure(ex) => println("//////////////////////////////////////////////")
      }
  }

}


object ChildAsset {
  def props(test: ActorRef) = Props(new ChildAsset(test))
}

class ChildAsset(test:ActorRef) extends Actor with ActorLogging {

  private implicit val timeout = Timeout(FiniteDuration(1, TimeUnit.SECONDS))

  private implicit val ec: ExecutionContext = context.dispatcher

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  //val queueSource = queue.to(Sink.foreach(println)).run()

  def receive = {
    case assetMessage: blockchain.AssetMessage => println("????????????????????????????????????")
      test ! assetMessage.assetsJsValue
  }
}
