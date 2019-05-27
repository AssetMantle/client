package utilities

import exceptions.BaseException
import javax.inject.Inject
import models.master.Accounts
import models.masterTransaction
import play.api.Configuration
import play.api.i18n.{Lang, Langs, MessagesApi}
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class PushNotification @Inject()(wsClient: WSClient, masterTransactionNotifications: masterTransaction.Notifications, accounts: Accounts, masterTransactionAccountTokens: masterTransaction.AccountTokens, langs: Langs, messagesApi: MessagesApi)(implicit exec: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.UTILITIES_PUSH_NOTIFICATION

  private val url = configuration.get[String]("notification.url")

  private val authorizationKey = configuration.get[String]("notification.authorizationKey")

  def sendNotification(username: String, notification: constants.Notification.Notification, messageParameters: String*)(implicit lang: Lang = Lang(accounts.Service.getLanguage(username))) = Future {
    try {
      val title = messagesApi(notification.title)
      val message = messagesApi(notification.message, messageParameters: _*)
      masterTransactionNotifications.Service.create(username, title, message)
      masterTransactionAccountTokens.Service.getTokenById(username).foreach(notificationToken => wsClient.url(url).withHttpHeaders(constants.Header.CONTENT_TYPE -> constants.Header.APPLICATION_JSON).withHttpHeaders(constants.Header.AUTHORIZATION -> authorizationKey).post(Json.toJson(Data(notificationToken, Notification(title, message)))))
    } catch {
      case baseException: BaseException => throw new BaseException(baseException.message)
    }
  }

  private implicit val notificationWrites: OWrites[Notification] = Json.writes[Notification]

  def registerNotificationToken(id: String, notificationToken: String): Int = masterTransactionAccountTokens.Service.updateToken(id, notificationToken)

  private implicit val dataWrites: OWrites[Data] = Json.writes[Data]

  private case class Notification(title: String, body: String)

  private case class Data(to: String, notification: Notification)

}

