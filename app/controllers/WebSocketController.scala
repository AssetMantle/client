package controllers

import actors.UserWebSocketActor
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.libs.streams.ActorFlow
import play.api.mvc.{AbstractController, MessagesControllerComponents, WebSocket}
import play.api.Configuration
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext

@Singleton
class WebSocketController @Inject()(
                                     messagesControllerComponents: MessagesControllerComponents,
                                   )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_WEB_SOCKET

  def connect: WebSocket = WebSocket.accept[String, String] { request =>
    val username = request.session.get(constants.Security.USERNAME)
    ActorFlow.actorRef { out =>
      UserWebSocketActor.props(username, out)
    }(actors.Service.actorSystem, actors.Service.materializer)
  }
}
