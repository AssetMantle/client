package actors

import actors.Message._
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem}
import play.api.Logger
import akka.dispatch.PriorityGenerator
import akka.dispatch.UnboundedStablePriorityMailbox
import com.typesafe.config.Config
import javax.inject.Singleton

import scala.concurrent.ExecutionContext

@Singleton
class CometActor() extends Actor with ActorLogging {

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_COMET

  private var usernameActorPathMap = Map[String, ActorRef]()

  def receive: PartialFunction[Any, Unit] = {
    case cometMessage: CometMessage =>
      usernameActorPathMap.get(cometMessage.username) match {
        case Some(value) => logger.info(constants.Actor.USER_COMET_ACTOR + ": " + cometMessage.username + "-" + cometMessage.message)
          value ! cometMessage.message
        case None => logger.info(cometMessage.username + ": " + constants.Actor.ACTOR_NOT_FOUND)
      }

    case updateUsernameActorRef: UpdateUsernameActorRef =>
      usernameActorPathMap += (updateUsernameActorRef.username -> updateUsernameActorRef.actorRef)
      logger.info(updateUsernameActorRef.username + ": " + constants.Actor.USER_COMET_ACTOR_ADDED_TO_MAP)

    case shutdownCometUserActor: ShutdownCometUserActor =>
      usernameActorPathMap.get(shutdownCometUserActor.username) match {
        case Some(userCometActor) => userCometActor ! shutdownCometUserActor
          usernameActorPathMap -= shutdownCometUserActor.username
        case None => logger.info(shutdownCometUserActor.username + ": " + constants.Actor.ACTOR_NOT_FOUND_FOR_SHUTDOWN)
      }
  }

}

class CometMailBox(settings: ActorSystem.Settings, config: Config) extends UnboundedStablePriorityMailbox(
  PriorityGenerator {
    case _: ShutdownCometUserActor => 0
    case _: UpdateUsernameActorRef => 1
    case _ => 2
  })