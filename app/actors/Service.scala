package actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import play.api.Logger

object Service {

  implicit val actorSystem: ActorSystem = ActorSystem(constants.Module.ACTOR_SERVICE, ConfigFactory.load)

  private implicit val logger: Logger = Logger(this.getClass)

  implicit val materializer: Materializer = Materializer(actorSystem)

  val emailActor: ActorRef = actorSystem.actorOf(props = Props[EmailActor]().withDispatcher("akka.actor.default-mailbox"), name = constants.Actor.ACTOR_EMAIL)

  val smsActor: ActorRef = actorSystem.actorOf(props = Props[SMSActor]().withDispatcher("akka.actor.default-mailbox"), name = constants.Actor.ACTOR_SMS)

  val pushNotificationActor: ActorRef = actorSystem.actorOf(props = Props[PushNotificationActor]().withDispatcher("akka.actor.default-mailbox"), name = constants.Actor.ACTOR_PUSH_NOTIFICATION)

  val appWebSocketActor: ActorRef = actorSystem.actorOf(props = Props[AppWebSocketActor]().withDispatcher("akka.actor.appWebSocketActorMailBox"), name = constants.Actor.ACTOR_APP_WEB_SOCKET)
}