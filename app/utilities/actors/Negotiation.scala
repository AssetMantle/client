package utilities.actors

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.util.Timeout
import models.blockchain
import play.api.Logger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

case class CreateNegotiationChildActorMessage(address: String, actorRef: ActorRef)

object MainNegotiationActor {
  def props(actorTimeout: FiniteDuration) = Props(new MainNegotiationActor(actorTimeout))
}

class MainNegotiationActor(actorTimeout: FiniteDuration) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_MAIN_NEGOTIATION

  def receive = {
    case negotiationCometMessage: blockchain.NegotiationCometMessage =>
      Actor.system.actorSelection("/user/" + constants.Module.ACTOR_MAIN_NEGOTIATION + "/" + negotiationCometMessage.ownerAddress).resolveOne().onComplete {
        case Success(actorRef) => actorRef ! negotiationCometMessage
        case Failure(ex) => logger.info(module + ": " + ex.getMessage)
      }
    case createNegotiationChildActorMessage: CreateNegotiationChildActorMessage => context.actorOf(props = UserNegotiationActor.props(createNegotiationChildActorMessage.actorRef, actorTimeout), name = createNegotiationChildActorMessage.address)
  }

}

object UserNegotiationActor {
  def props(systemUserActor: ActorRef, actorTimeout: FiniteDuration) = Props(new UserNegotiationActor(systemUserActor, actorTimeout))
}

class UserNegotiationActor(systemUserActor: ActorRef, actorTimeout: FiniteDuration) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_USER_NEGOTIATION

  override def postStop(): Unit = log.info(module + ": Actor Stopped")

  def receive = {
    case negotiationCometMessage: blockchain.NegotiationCometMessage => systemUserActor ! negotiationCometMessage.message
    case _: ShutdownActorMessage =>
      systemUserActor ! PoisonPill
      context.stop(self)
  }
}