package utilities

import javax.inject.Inject
import models.master.Notifications
import play.api.Configuration
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class PushNotifications @Inject()(wsClient: WSClient, notifications: Notifications)(implicit exec: ExecutionContext, configuration: Configuration) {

  private val url = configuration.get[String]("notification.url")

  private val authorizationKey = configuration.get[String]("notification.authorizationKey")

  private case class Notification(title: String, body: String)
  private implicit val notificationWrites: OWrites[Notification] = Json.writes[Notification]

  private case class Data(to: String, notification: Notification)
  private implicit val dataWrites: OWrites[Data] = Json.writes[Data]

  def sendNotification(id: String, messageType: String, passedData: Seq[String] = Seq("Executed"))(implicit currentModule: String)= {
    Thread.sleep(3000)
    wsClient.url(url).withHttpHeaders(constants.Header.CONTENT_TYPE -> constants.Header.APPLICATION_JSON).withHttpHeaders(constants.Header.AUTHORIZATION -> authorizationKey)
      .post(Json.toJson(Data(notifications.Service.getTokenById(id), Notification(messageType, passedData(0)))))
  }

  def registerNotificationToken(id: String, notificationToken: String): Int = notifications.Service.updateToken(id, notificationToken)

}

