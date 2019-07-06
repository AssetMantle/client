package utilities.actors

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.util.Timeout
import models.blockchain
import play.api.Logger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

object MainAssetActor {
  def props = Props(new MainAssetActor)
}

class MainAssetActor extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(FiniteDuration(1, TimeUnit.SECONDS))

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_MAIN_ASSET

  def receive = {
    case assetCometMessage: blockchain.AssetCometMessage =>
      Actor.system.actorSelection("/user/" + constants.Module.ACTOR_USER_ASSET + assetCometMessage.ownerAddress).resolveOne().onComplete {
        case Success(actorRef) => actorRef ! assetCometMessage
        case Failure(ex) => logger.info(module + ": " + ex.getMessage)
      }
  }

}

object UserAssetActor {
  def props(systemUserActor: ActorRef) = Props(new UserAssetActor(systemUserActor))
}

class UserAssetActor(systemUserActor: ActorRef) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(FiniteDuration(1, TimeUnit.SECONDS))

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_USER_ASSET

  def receive = {
    case assetCometMessage: blockchain.AssetCometMessage => logger.info(module + ": " + assetCometMessage.ownerAddress)
      systemUserActor ! assetCometMessage.message
    case _: ShutdownActorMessage =>
      systemUserActor ! PoisonPill
      context.stop(self)
  }
}