package actors

import actors.Message._
import akka.actor.{Actor, ActorLogging}
import javax.inject.Singleton
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext

@Singleton
class PushNotificationActor() extends Actor with ActorLogging {

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_PUSH_NOTIFICATION

  def receive: PartialFunction[Any, Unit] = {
    case pushNotification: PushNotification => logger.info(pushNotification.token)
  }

}