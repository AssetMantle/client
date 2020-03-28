package actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import akka.util.Timeout
import javax.inject.{Inject, Singleton}
import models.masterTransaction
import play.api.Logger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

case class CreateMessageChildActorMessage(username: String, actorRef: ActorRef)

object MainMessageActor {
  def props(actorTimeout: FiniteDuration, actorSystem: ActorSystem) = Props(new MainMessageActor(actorTimeout, actorSystem))
}

@Singleton
class MainMessageActor @Inject()(actorTimeout: FiniteDuration, actorSystem: ActorSystem) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_MAIN_MESSAGE

  def receive: PartialFunction[Any, Unit] = {
    case chatCometMessage: masterTransaction.MessageCometMessage =>
      actorSystem.actorSelection("/user/" + constants.Module.ACTOR_MAIN_MESSAGE + "/" + chatCometMessage.username).resolveOne().onComplete {
        case Success(actorRef) => logger.info(module + " " + chatCometMessage.username + ": " + chatCometMessage.message)
          actorRef ! chatCometMessage
        case Failure(ex) => logger.info(module + ": " + ex.getMessage)
      }
    case createChatChildActorMessage: CreateMessageChildActorMessage => context.actorOf(props = UserChatActor.props(createChatChildActorMessage.actorRef, actorTimeout), name = createChatChildActorMessage.username)
  }

}

object UserChatActor {
  def props(systemUserActor: ActorRef, actorTimeout: FiniteDuration) = Props(new UserChatActor(systemUserActor, actorTimeout))
}

class UserChatActor(systemUserActor: ActorRef, actorTimeout: FiniteDuration) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_USER_MESSAGE

  def receive = {
    case chatCometMessage: masterTransaction.MessageCometMessage => systemUserActor ! chatCometMessage.message
    case _: ShutdownActorMessage =>
      systemUserActor ! PoisonPill
      context.stop(self)
  }
}