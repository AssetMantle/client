package utilities.actors

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.util.Timeout
import models.blockchain
import play.api.Logger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

case class CreateFiatChildActorMessage(address: String, actorRef: ActorRef)

object MainFiatActor {
  def props(actorTimeout: FiniteDuration) = Props(new MainFiatActor(actorTimeout))
}

class MainFiatActor(actorTimeout: FiniteDuration) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_MAIN_FIAT

  def receive = {
    case fiatCometMessage: blockchain.FiatCometMessage =>
      Actor.system.actorSelection("/user/" + constants.Module.ACTOR_MAIN_FIAT + "/" + fiatCometMessage.ownerAddress).resolveOne().onComplete {
        case Success(actorRef) => actorRef ! fiatCometMessage
        case Failure(ex) => logger.info(module + ": " + ex.getMessage)
      }
    case createFiatChildActorMessage: CreateFiatChildActorMessage => context.actorOf(props = UserFiatActor.props(createFiatChildActorMessage.actorRef, actorTimeout), name = createFiatChildActorMessage.address)
  }

}

object UserFiatActor {
  def props(systemUserActor: ActorRef, actorTimeout: FiniteDuration) = Props(new UserFiatActor(systemUserActor, actorTimeout))
}

class UserFiatActor(systemUserActor: ActorRef, actorTimeout: FiniteDuration) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_USER_FIAT

  def receive = {
    case fiatCometMessage: blockchain.FiatCometMessage => systemUserActor ! fiatCometMessage.message
    case _: ShutdownActorMessage =>
      systemUserActor ! PoisonPill
      context.stop(self)
  }
}
