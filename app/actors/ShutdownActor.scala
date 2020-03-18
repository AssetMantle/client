package actors

import akka.actor.ActorSystem
import akka.util.Timeout
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ShutdownActorMessage()

@Singleton
class ShutdownActor @Inject()(actorSystem: ActorSystem)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private val actorTimeout = configuration.get[Int]("akka.actors.timeout").seconds

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_SHUTDOWN

  def onLogOut(actorPath: String, username: String): Future[Unit] = Future {
    shutdown(actorPath, username)
  }

  def shutdown(actorPath: String, username: String): Unit = {
    actorSystem.actorSelection("/user/" + actorPath + "/" + username).resolveOne().onComplete {
      case Success(actorRef) => logger.info(module + ": " + actorPath + "/" + username)
        actorRef ! ShutdownActorMessage()
      case Failure(ex) => logger.info(module + ": " + ex.getMessage)
    }
  }
}

