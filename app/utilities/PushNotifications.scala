package utilities

import javax.inject.Inject
import models.master.Accounts
import models.masterTransaction.{AccountTokens, Notifications}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Configuration
import play.api.i18n.{Lang, Langs, MessagesApi}
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

class PushNotifications @Inject()(wsClient: WSClient, notifications: Notifications, accounts: Accounts, accountTokens: AccountTokens, langs: Langs, messagesApi: MessagesApi)(implicit exec: ExecutionContext, configuration: Configuration) {

  private val url = configuration.get[String]("notification.url")

  private val authorizationKey = configuration.get[String]("notification.authorizationKey")

  def sendNotification(id: String, messageType: String, passedData: Seq[String] = Seq(""))(implicit lang: Lang = Lang(accounts.Service.getLanguage(id))) = {
    Thread.sleep(3000)
    notifications.Service.addNotification(id, messagesApi("NotificationTitle" + "." + messageType), messagesApi("NotificationMessage" + "." + messageType, passedData(0)), DateTime.now(DateTimeZone.UTC).getMillis())
    wsClient.url(url).withHttpHeaders(constants.Header.CONTENT_TYPE -> constants.Header.APPLICATION_JSON).withHttpHeaders(constants.Header.AUTHORIZATION -> authorizationKey)
      .post(Json.toJson(Data(accountTokens.Service.getTokenById(id), Notification(messagesApi("NotificationTitle" + "." + messageType), messagesApi("NotificationMessage" + "." + messageType, passedData(0))))))
  }

  private implicit val notificationWrites: OWrites[Notification] = Json.writes[Notification]

  def registerNotificationToken(id: String, notificationToken: String): Int = accountTokens.Service.updateToken(id, notificationToken)

  private implicit val dataWrites: OWrites[Data] = Json.writes[Data]

  private case class Notification(title: String, body: String)

  private case class Data(to: String, notification: Notification)

}

