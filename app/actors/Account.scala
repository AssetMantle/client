package actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import akka.util.Timeout
import javax.inject.{Inject, Singleton}
import models.blockchain
import play.api.Logger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

case class CreateAccountChildActorMessage(address: String, actorRef: ActorRef)

object MainAccountActor {
  def props(actorTimeout: FiniteDuration, actorSystem: ActorSystem) = Props(new MainAccountActor(actorTimeout, actorSystem))
}

@Singleton
class MainAccountActor @Inject()(actorTimeout: FiniteDuration, actorSystem: ActorSystem) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_MAIN_ACCOUNT

  def receive = {
    case accountCometMessage: blockchain.AccountCometMessage =>
      actorSystem.actorSelection("/user/" + constants.Module.ACTOR_MAIN_ACCOUNT + "/" + accountCometMessage.ownerAddress).resolveOne().onComplete {
        case Success(actorRef) => actorRef ! accountCometMessage
        case Failure(ex) => logger.info(module + ": " + ex.getMessage)
      }
    case createAccountChildActorMessage: CreateAccountChildActorMessage => context.actorOf(props = UserAccountActor.props(createAccountChildActorMessage.actorRef, actorTimeout), name = createAccountChildActorMessage.address)
  }

}

object UserAccountActor {
  def props(systemUserActor: ActorRef, actorTimeout: FiniteDuration) = Props(new UserAccountActor(systemUserActor, actorTimeout))
}

class UserAccountActor(systemUserActor: ActorRef, actorTimeout: FiniteDuration) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_USER_ACCOUNT

  override def postStop(): Unit = log.info(module + ": Actor Stopped")

  def receive = {
    case accountCometMessage: blockchain.AccountCometMessage => systemUserActor ! accountCometMessage.message
    case _: ShutdownActorMessage =>
      systemUserActor ! PoisonPill
      context.stop(self)
  }
}