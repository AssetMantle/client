package actors

import actors.Message._
import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.scaladsl.Source
import akka.stream.{CompletionStrategy, Materializer, OverflowStrategy}
import com.typesafe.config.ConfigFactory
import exceptions.BaseException
import play.api.Logger
import play.api.libs.json.JsValue

object Service {

  private val actorSystem = ActorSystem(constants.Module.ACTOR_SERVICE, ConfigFactory.load)

  private implicit val logger: Logger = Logger(constants.Module.ACTOR_SERVICE)

  private implicit val module: String = constants.Module.ACTOR_SERVICE

  private implicit val materializer: Materializer = Materializer(actorSystem)

  val cometActor: ActorRef = actorSystem.actorOf(props = Props[CometActor].withDispatcher("akka.actor.cometMailBox"), name = constants.Module.ACTOR_COMET)

  val emailActor: ActorRef = actorSystem.actorOf(props = Props[EmailActor].withDispatcher("akka.actor.default-mailbox"), name = constants.Module.ACTOR_EMAIL)

  val smsActor: ActorRef = actorSystem.actorOf(props = Props[SMSActor].withDispatcher("akka.actor.default-mailbox"), name = constants.Module.ACTOR_SMS)

  val pushNotificationActor: ActorRef = actorSystem.actorOf(props = Props[PushNotificationActor].withDispatcher("akka.actor.default-mailbox"), name = constants.Module.ACTOR_PUSH_NOTIFICATION)

  object Comet {
    private def cometCompletionMatcher: PartialFunction[Any, CompletionStrategy] = {
      case shutdownCometUserActor: ShutdownCometUserActor => logger.info(shutdownCometUserActor.username)
        CompletionStrategy.immediately
    }

    private def cometFailureMatcher: PartialFunction[Any, BaseException] = {
      case constants.Comet.ERROR => throw new BaseException(constants.Response.COMET_ACTOR_ERROR)
    }

    def createSource(username: String): Source[JsValue, NotUsed] = {
      cometActor ! ShutdownCometUserActor(username)
      val (systemUserActor, source) = Source.actorRef[JsValue](cometCompletionMatcher, cometFailureMatcher, 0, OverflowStrategy.dropHead).preMaterialize()
      cometActor ! UpdateUsernameActorRef(username, systemUserActor)
      source
    }

    def shutdownUserActor(username: String): Unit = cometActor ! ShutdownCometUserActor(username)

  }

}