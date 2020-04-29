package actors

import actors.Message._
import akka.actor.{Actor, ActorLogging}
import javax.inject.Singleton
import play.api.Logger

import scala.concurrent.ExecutionContext

@Singleton
class EmailActor() extends Actor with ActorLogging {

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_EMAIL

  def receive: PartialFunction[Any, Unit] = {
    case email: Email => logger.info(email.emailAddress)
  }

}