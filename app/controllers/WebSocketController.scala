package controllers

import actors.Message.WebSocket.AddPrivateActor
import actors.UserWebSocketActor
import play.api.i18n.I18nSupport
import play.api.libs.streams.ActorFlow
import play.api.mvc.{AbstractController, MessagesControllerComponents, WebSocket}
import play.api.{Configuration, Logger}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class WebSocketController @Inject()(
                                     messagesControllerComponents: MessagesControllerComponents,
                                   )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_WEB_SOCKET

  def connect: WebSocket = WebSocket.accept[String, String] { request =>
    val username = request.session.get(constants.Security.USERNAME)
    ActorFlow.actorRef { out =>
      if (username.isDefined) {
        actors.Service.appWebSocketActor ! AddPrivateActor(out, username.get)
      }
      UserWebSocketActor.props(out)
    }(actors.Service.actorSystem, actors.Service.materializer)
  }
}
