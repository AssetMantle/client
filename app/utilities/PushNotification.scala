package utilities

import javax.inject.Inject
import models.master.Accounts
import models.masterTransaction.{AccountTokens, Notifications}
import play.api.Configuration
import play.api.i18n.{Lang, Langs, MessagesApi}
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class PushNotification @Inject()(wsClient: WSClient, notifications: Notifications, accounts: Accounts, accountTokens: AccountTokens, langs: Langs, messagesApi: MessagesApi)(implicit exec: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.UTILITIES_NOTIFICATION

  private val url = configuration.get[String]("notification.url")

  private val authorizationKey = configuration.get[String]("notification.authorizationKey")

  def sendNotification(accountID: String, messageType: String, passedData: Seq[String] = Seq(""))(implicit lang: Lang = Lang(accounts.Service.getLanguage(accountID))): WSResponse = {
    notifications.Service.addNotification(accountID, messagesApi(module + "Title" + "." + messageType), messagesApi(module + "Message" + "." + messageType, passedData(0)))
    Await.result(wsClient.url(url).withHttpHeaders(constants.Header.CONTENT_TYPE -> constants.Header.APPLICATION_JSON).withHttpHeaders(constants.Header.AUTHORIZATION -> authorizationKey)
      .post(Json.toJson(Data(accountTokens.Service.getTokenById(accountID), Notification(messagesApi(module + "Title" + "." + messageType), messagesApi(module + "Message" + "." + messageType, passedData(0)))))), Duration.Inf)
  }

  private implicit val notificationWrites: OWrites[Notification] = Json.writes[Notification]

  def registerNotificationToken(id: String, notificationToken: String) = Future {
    accountTokens.Service.updateNotificationToken(id, notificationToken)
  }

  private implicit val dataWrites: OWrites[Data] = Json.writes[Data]

  private case class Notification(title: String, body: String)

  private case class Data(to: String, notification: Notification)

}

