package actors

import actors.Message._
import akka.actor.{Actor, ActorLogging}
import javax.inject.Singleton
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext

@Singleton
class SMSActor() extends Actor with ActorLogging {

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_SMS

  def receive: PartialFunction[Any, Unit] = {
    case sms: SMS => logger.info(sms.mobileNumber)
  }

}