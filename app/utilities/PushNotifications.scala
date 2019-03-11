package utilities

import javax.inject.Inject
import models.master.Notifications
import play.api.Configuration
import scala.concurrent.ExecutionContext
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSRequest}

class PushNotifications @Inject()(wsClient: WSClient, notifications: Notifications) (implicit  exec: ExecutionContext, configuration: Configuration){
  object Push {
    def sendNotification(id: String, messageType: String, passedData: String) = {
      val data: JsValue = Json.obj(
        "data" -> Json.obj(
          "notification" -> Json.obj(
            "title" -> messageType,
            "passedData" -> passedData
          )
        ),
        "to" -> notifications.Service.getTokenById(id)
      )
      Thread.sleep(3000)
      val reqPost: WSRequest = wsClient.url(configuration.get[String]("notification.url"))
        .withHttpHeaders(constants.JSON.CONTENT_TYPE -> configuration.get[String]("notification.contentType"))
        .withHttpHeaders(constants.JSON.CONTENT_TYPE -> configuration.get[String]("notification.authorizationKey"))
      reqPost.post(data)
    }

    def registerNotificationToken(id: String, notificationToken: String): Int = notifications.Service.updateToken(id, notificationToken)
  }
}
