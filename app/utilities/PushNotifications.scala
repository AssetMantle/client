package utilities

import javax.inject.Inject
import models.master.Notifications
import play.api.Configuration
import scala.concurrent.ExecutionContext
import constants._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSRequest}

class PushNotifications @Inject()(wsClient: WSClient, notifications: Notifications) (implicit  exec: ExecutionContext, configuration: Configuration){
  object Push {
    def sendNotification(id: String, messageType: String) = {
      val data: JsValue = Json.obj(
        "data" -> Json.obj(
          "notification" -> Json.obj(
            "title" -> messageType
          )
        ),
        "to" -> notifications.Service.getTokenById(id)

      )
      Thread.sleep(3000)
      val reqPost: WSRequest = wsClient.url(configuration.get[String]("notification.url"))
        .withHttpHeaders(Notification.CONTENT_TYPE -> configuration.get[String]("notification.contentType"))
        .withHttpHeaders(Notification.AUTHORIZATION -> configuration.get[String]("notification.authorizationKey"))
      println(" / "+ configuration.get[String]("notification.url")+ " / " + configuration.get[String]("notification.contentType")+ " / "+ configuration.get[String]("notification.authorizationKey"))
      reqPost.post(data)
    }

    def registerNotificationToken(id: String, notificationToken: String): Int = notifications.Service.updateToken(id, notificationToken)
  }
}
