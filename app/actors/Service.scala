package actors

import actors.Message._
import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.scaladsl.Source
import akka.stream.{CompletionStrategy, Materializer, OverflowStrategy}
import com.typesafe.config.ConfigFactory
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration._

object Service {

  implicit val actorSystem: ActorSystem = ActorSystem(constants.Module.ACTOR_SERVICE, ConfigFactory.load)

  private implicit val logger: Logger = Logger(this.getClass)

  implicit val module: String = constants.Module.ACTOR_SERVICE

  implicit val materializer: Materializer = Materializer(actorSystem)

  val cometActor: ActorRef = actorSystem.actorOf(props = Props[CometActor].withDispatcher("akka.actor.default-mailbox"), name = constants.Actor.ACTOR_COMET)

  val emailActor: ActorRef = actorSystem.actorOf(props = Props[EmailActor].withDispatcher("akka.actor.default-mailbox"), name = constants.Actor.ACTOR_EMAIL)

  val smsActor: ActorRef = actorSystem.actorOf(props = Props[SMSActor].withDispatcher("akka.actor.default-mailbox"), name = constants.Actor.ACTOR_SMS)

  val pushNotificationActor: ActorRef = actorSystem.actorOf(props = Props[PushNotificationActor].withDispatcher("akka.actor.default-mailbox"), name = constants.Actor.ACTOR_PUSH_NOTIFICATION)

  val appWebSocketActor: ActorRef = actorSystem.actorOf(props = Props[AppWebSocketActor].withDispatcher("akka.actor.default-mailbox"), name = constants.Actor.ACTOR_APP_WEB_SOCKET)

  object Comet {
    private def cometCompletionMatcher: PartialFunction[Any, CompletionStrategy] = {
      case shutdownCometUserActor: ShutdownCometUserActor => logger.info(shutdownCometUserActor.username)
        CompletionStrategy.immediately
    }

    private def cometFailureMatcher: PartialFunction[Any, BaseException] = {
      case constants.Comet.ERROR => throw new BaseException(constants.Response.COMET_ACTOR_ERROR)
    }

    def createSource(username: String, keepAliveDuration: FiniteDuration): Source[JsValue, NotUsed] = {
      cometActor ! ShutdownCometUserActor(username)
      val (systemUserActor, source) = Source.actorRef[JsValue](cometCompletionMatcher, cometFailureMatcher, 0, OverflowStrategy.dropHead).preMaterialize()
      cometActor ! UpdateUsernameActorRef(username, systemUserActor)
      source.keepAlive(keepAliveDuration, () => actors.Message.makeCometMessage(username, constants.Comet.KEEP_ALIVE, actors.Message.KeepAlive()).message)
    }

    def shutdownUserActor(username: String): Unit = cometActor ! ShutdownCometUserActor(username)

  }

}