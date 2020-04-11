package actors

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, OverflowStrategy}
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json.JsValue
import scala.concurrent.duration._

@Singleton
class Create @Inject()(actorSystem: ActorSystem, shutdownActors: ShutdownActor)(implicit configuration: Configuration){

  private val cometActorSleepTime = configuration.get[Long]("akka.actors.cometActorSleepTime")

  private val configurationActorTimeout = configuration.get[Int]("akka.actors.timeout").seconds

  private implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  val mainActor: ActorRef = actorSystem.actorOf(props = MainActor.props(configurationActorTimeout, actorSystem), name = constants.Module.ACTOR_MAIN)

  object Service {
    def cometSource(username: String) = {
      shutdownActors.shutdown(constants.Module.ACTOR_MAIN, username)
      Thread.sleep(cometActorSleepTime)
      val (systemUserActor, source) = Source.actorRef[JsValue](0, OverflowStrategy.dropHead).preMaterialize()
      mainActor ! actors.CreateAccountChildActorMessage(username = username, actorRef = systemUserActor)
      source
    }
  }
}
