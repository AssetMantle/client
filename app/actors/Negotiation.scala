package actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import akka.util.Timeout
import javax.inject.{Inject, Singleton}
import models.blockchain
import play.api.Logger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

case class CreateNegotiationChildActorMessage(username: String, actorRef: ActorRef)

object MainNegotiationActor {
  def props(actorTimeout: FiniteDuration, actorSystem: ActorSystem) = Props(new MainNegotiationActor(actorTimeout, actorSystem))
}

@Singleton
class MainNegotiationActor @Inject()(actorTimeout: FiniteDuration, actorSystem: ActorSystem) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_MAIN_NEGOTIATION

  def receive: PartialFunction[Any, Unit] = {
    case negotiationCometMessage: blockchain.NegotiationCometMessage =>
      actorSystem.actorSelection("/user/" + constants.Module.ACTOR_MAIN_NEGOTIATION + "/" + negotiationCometMessage.username).resolveOne().onComplete {
        case Success(actorRef) => logger.info(module + " " + negotiationCometMessage.username + ": " + negotiationCometMessage.message)
          actorRef ! negotiationCometMessage
        case Failure(ex) => logger.info(module + ": " + ex.getMessage)
      }
    case createNegotiationChildActorMessage: CreateNegotiationChildActorMessage => context.actorOf(props = UserNegotiationActor.props(createNegotiationChildActorMessage.actorRef, actorTimeout), name = createNegotiationChildActorMessage.username)
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

  def receive = {
    case negotiationCometMessage: blockchain.NegotiationCometMessage => systemUserActor ! negotiationCometMessage.message
    case _: ShutdownActorMessage =>
      systemUserActor ! PoisonPill
      context.stop(self)
  }
}