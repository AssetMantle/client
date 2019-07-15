package utilities.actors

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.util.Timeout
import models.blockchain
import play.api.Logger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

case class CreateAssetChildActorMessage(address: String, actorRef: ActorRef)

object MainAssetActor {
  def props(actorTimeout: FiniteDuration) = Props(new MainAssetActor(actorTimeout))
}

class MainAssetActor(actorTimeout: FiniteDuration) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_MAIN_ASSET

  def receive = {
    case assetCometMessage: blockchain.AssetCometMessage =>
      Actor.system.actorSelection("/user/" + constants.Module.ACTOR_MAIN_ASSET + "/" + assetCometMessage.ownerAddress).resolveOne().onComplete {
        case Success(actorRef) => actorRef ! assetCometMessage
        case Failure(ex) => logger.info(module + ": " + ex.getMessage)
      }
    case createAssetChildActorMessage: CreateAssetChildActorMessage => context.actorOf(props = UserAssetActor.props(createAssetChildActorMessage.actorRef, actorTimeout), name = createAssetChildActorMessage.address)
  }

}

object UserAssetActor {
  def props(systemUserActor: ActorRef, actorTimeout: FiniteDuration) = Props(new UserAssetActor(systemUserActor, actorTimeout))
}

class UserAssetActor(systemUserActor: ActorRef, actorTimeout: FiniteDuration) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_USER_ASSET

  override def postStop(): Unit = log.info(module + ": Actor Stopped")

  def receive = {
    case assetCometMessage: blockchain.AssetCometMessage => systemUserActor ! assetCometMessage.message
    case _: ShutdownActorMessage =>
      systemUserActor ! PoisonPill
      context.stop(self)
  }
}