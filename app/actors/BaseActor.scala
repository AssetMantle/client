package actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.Source
import akka.util.Timeout
import javax.inject.{Inject, Singleton}
import models.blockchain
import play.api.{Configuration, Logger}
import play.api.libs.json.JsValue

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

case class CreateAccountChildActorMessage(username: String, actorRef: ActorRef)

object MainActor {
  def props(actorTimeout: FiniteDuration, actorSystem: ActorSystem) = Props(new MainActor(actorTimeout, actorSystem))
}

@Singleton
class MainActor @Inject()(actorTimeout: FiniteDuration, actorSystem: ActorSystem) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_MAIN

  def receive: PartialFunction[Any, Unit] = {
    case cometMessage: Message.CometMessage =>
      actorSystem.actorSelection("/user/" + constants.Module.ACTOR_MAIN + "/" + cometMessage.username).resolveOne().onComplete {
        case Success(actorRef) => logger.info(module + " " + cometMessage.username + ": " + cometMessage.message)

          actorRef ! cometMessage
        case Failure(ex) => logger.info(module + ": " + ex.getMessage)
      }
    case createAccountChildActorMessage: CreateAccountChildActorMessage => context.actorOf(props = UserActor.props(createAccountChildActorMessage.actorRef, actorTimeout), name = createAccountChildActorMessage.username)
  }

}

object UserActor {
  def props(systemUserActor: ActorRef, actorTimeout: FiniteDuration) = Props(new UserActor(systemUserActor, actorTimeout))
}

class UserActor(systemUserActor: ActorRef, actorTimeout: FiniteDuration) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_USER


  def receive = {
    case cometMessage: Message.CometMessage => systemUserActor ! cometMessage.message

    case _: ShutdownActorMessage =>
      systemUserActor ! PoisonPill
      context.stop(self)
  }
}