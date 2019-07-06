package utilities.actors

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.util.Timeout
import models.blockchain
import play.api.Logger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

object MainFiatActor {
  def props = Props(new MainFiatActor)
}

class MainFiatActor extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(FiniteDuration(1, TimeUnit.SECONDS))

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_MAIN_FIAT

  def receive = {
    case fiatCometMessage: blockchain.FiatCometMessage =>
      Actor.system.actorSelection("/user/" + constants.Module.ACTOR_USER_FIAT + fiatCometMessage.ownerAddress).resolveOne().onComplete {
        case Success(actorRef) => actorRef ! fiatCometMessage
        case Failure(ex) => logger.info(module + ": " + ex.getMessage)
      }
  }

}

object UserFiatActor {
  def props(systemUserActor: ActorRef) = Props(new UserFiatActor(systemUserActor))
}

class UserFiatActor(systemUserActor: ActorRef) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(FiniteDuration(1, TimeUnit.SECONDS))

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_USER_FIAT

  def receive = {
    case fiatCometMessage: blockchain.FiatCometMessage => logger.info(module + ": " + fiatCometMessage.ownerAddress)
      logger.info("\n \n \n \n" + fiatCometMessage.message + "\n \n \n \n")
      systemUserActor ! fiatCometMessage.message
    case _: ShutdownActorMessage =>
      systemUserActor ! PoisonPill
      context.stop(self)
  }
}
