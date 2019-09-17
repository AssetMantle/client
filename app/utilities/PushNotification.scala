package utilities

import exceptions.BaseException
import javax.inject.Inject
import models.{master, masterTransaction}
import play.api.{Configuration, Logger}
import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class PushNotification @Inject()(wsClient: WSClient, masterTransactionNotifications: masterTransaction.Notifications, masterAccounts: master.Accounts, masterTransactionAccountTokens: masterTransaction.AccountTokens, messagesApi: MessagesApi)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.UTILITIES_NOTIFICATION

  private implicit val logger: Logger = Logger(this.getClass)

  private val url = configuration.get[String]("notification.url")

  private val authorizationKey = configuration.get[String]("notification.authorizationKey")

  private case class Notification(title: String, body: String)

  private case class Data(to: String, notification: Notification)

  private implicit val dataWrites: OWrites[Data] = Json.writes[Data]

  private implicit val notificationWrites: OWrites[Notification] = Json.writes[Notification]

  def send(username: String, notification: constants.Notification.PushNotification, messageParameters: String*)(implicit lang: Lang = Lang(masterAccounts.Service.getLanguage(username))) = Future {
    try {
      val title = messagesApi(notification.title)
      val message = messagesApi(notification.message, messageParameters: _*)
      masterTransactionNotifications.Service.create(username, title, message)
      masterTransactionAccountTokens.Service.getTokenById(username).foreach(notificationToken => wsClient.url(url).withHttpHeaders(constants.Header.CONTENT_TYPE -> constants.Header.APPLICATION_JSON).withHttpHeaders(constants.Header.AUTHORIZATION -> authorizationKey).post(Json.toJson(Data(notificationToken, Notification(title, message)))))
    } catch {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
    }
  }

  def registerNotificationToken(id: String, notificationToken: String): Int = {
    try {
      masterTransactionAccountTokens.Service.updateToken(id, notificationToken)
    } catch {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
    }
  }



}

