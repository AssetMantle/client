package actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import akka.util.Timeout
import javax.inject.{Inject, Singleton}
import models.masterTransaction
import play.api.Logger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

case class CreateChatChildActorMessage(username: String, actorRef: ActorRef)

object MainChatActor {
  def props(actorTimeout: FiniteDuration, actorSystem: ActorSystem) = Props(new MainChatActor(actorTimeout, actorSystem))
}

@Singleton
class MainChatActor @Inject()(actorTimeout: FiniteDuration, actorSystem: ActorSystem) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_MAIN_CHAT

  def receive: PartialFunction[Any, Unit] = {
    case chatCometMessage: masterTransaction.ChatCometMessage =>
      actorSystem.actorSelection("/user/" + constants.Module.ACTOR_MAIN_CHAT + "/" + chatCometMessage.username).resolveOne().onComplete {
        case Success(actorRef) => logger.info(module + " " + chatCometMessage.username + ": " + chatCometMessage.message)
          actorRef ! chatCometMessage
        case Failure(ex) => logger.info(module + ": " + ex.getMessage)
      }
    case createChatChildActorMessage: CreateChatChildActorMessage => context.actorOf(props = UserChatActor.props(createChatChildActorMessage.actorRef, actorTimeout), name = createChatChildActorMessage.username)
  }

}

object UserChatActor {
  def props(systemUserActor: ActorRef, actorTimeout: FiniteDuration) = Props(new UserChatActor(systemUserActor, actorTimeout))
}

class UserChatActor(systemUserActor: ActorRef, actorTimeout: FiniteDuration) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_USER_CHAT

  def receive = {
    case chatCometMessage: masterTransaction.ChatCometMessage => systemUserActor ! chatCometMessage.message
    case _: ShutdownActorMessage =>
      systemUserActor ! PoisonPill
      context.stop(self)
  }
}