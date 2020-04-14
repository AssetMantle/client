package actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import akka.util.Timeout
import javax.inject.{Inject, Singleton}
import models.master
import play.api.Logger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

case class CreateNegotiationTermsChildActorMessage(username: String, actorRef: ActorRef)


object MainNegotiationTermsActor {
  def props(actorTimeout: FiniteDuration, actorSystem: ActorSystem) = Props(new MainNegotiationTermsActor(actorTimeout, actorSystem))
}

@Singleton
class MainNegotiationTermsActor @Inject()(actorTimeout: FiniteDuration, actorSystem: ActorSystem) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_MAIN_NEGOTIATION_TERMS

  def receive: PartialFunction[Any, Unit] = {
    case negotiationTermsCometMessage: master.NegotiationTermsCometMessage =>
      actorSystem.actorSelection("/user/" + constants.Module.ACTOR_MAIN_NEGOTIATION_TERMS + "/" + negotiationTermsCometMessage.username).resolveOne().onComplete {
        case Success(actorRef) => logger.info(module + " " + negotiationTermsCometMessage.username + ": " + negotiationTermsCometMessage.message)
          actorRef ! negotiationTermsCometMessage
        case Failure(ex) => logger.info(module + ": " + ex.getMessage)
      }
    case createNegotiationTermsChildActorMessage: CreateNegotiationTermsChildActorMessage =>
      println("creating actor---"+createNegotiationTermsChildActorMessage.username)
      context.actorOf(props = UserNegotiationTermsActor.props(createNegotiationTermsChildActorMessage.actorRef, actorTimeout), name = createNegotiationTermsChildActorMessage.username)
  }

}

object UserNegotiationTermsActor {
  def props(systemUserActor: ActorRef, actorTimeout: FiniteDuration) = Props(new UserNegotiationTermsActor(systemUserActor, actorTimeout))
}

class UserNegotiationTermsActor(systemUserActor: ActorRef, actorTimeout: FiniteDuration) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_USER_NEGOTIATION_TERMS

  def receive = {
    case negotiationTermsCometMessage: master.NegotiationTermsCometMessage =>
      systemUserActor ! negotiationTermsCometMessage.message
    case _: ShutdownActorMessage =>
      systemUserActor ! PoisonPill
      context.stop(self)
  }
}