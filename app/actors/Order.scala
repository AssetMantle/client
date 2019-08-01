package actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import akka.util.Timeout
import javax.inject.{Inject, Singleton}
import models.blockchain
import play.api.Logger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

case class CreateOrderChildActorMessage(username: String, actorRef: ActorRef)

object MainOrderActor {
  def props(actorTimeout: FiniteDuration, actorSystem: ActorSystem) = Props(new MainOrderActor(actorTimeout, actorSystem))
}

@Singleton
class MainOrderActor @Inject()(actorTimeout: FiniteDuration,actorSystem: ActorSystem) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_MAIN_ORDER

  def receive = {
    case orderCometMessage: blockchain.OrderCometMessage =>
      actorSystem.actorSelection("/user/" + constants.Module.ACTOR_MAIN_ORDER + "/" + orderCometMessage.username).resolveOne().onComplete {
        case Success(actorRef) => actorRef ! orderCometMessage
        case Failure(ex) => logger.info(module + ": " + ex.getMessage)
      }
    case createOrderChildActorMessage: CreateOrderChildActorMessage => context.actorOf(props = UserOrderActor.props(createOrderChildActorMessage.actorRef, actorTimeout), name = createOrderChildActorMessage.username)
  }

}

object UserOrderActor {
  def props(systemUserActor: ActorRef, actorTimeout: FiniteDuration) = Props(new UserOrderActor(systemUserActor, actorTimeout))
}

class UserOrderActor(systemUserActor: ActorRef, actorTimeout: FiniteDuration) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_USER_ORDER

  def receive = {
    case orderCometMessage: blockchain.OrderCometMessage => systemUserActor ! orderCometMessage.message
    case _: ShutdownActorMessage =>
      systemUserActor ! PoisonPill
      context.stop(self)
  }
}