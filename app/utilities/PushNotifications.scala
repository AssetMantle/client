package utilities

import exceptions.BaseException
import javax.inject.Inject
import models.master.Accounts
import models.masterTransaction.{AccountTokens, Notifications}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Configuration
import play.api.i18n.{Lang, Langs, MessagesApi}
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration

class PushNotifications @Inject()(wsClient: WSClient, notifications: Notifications, accounts: Accounts, accountTokens: AccountTokens, langs: Langs, messagesApi: MessagesApi)(implicit exec: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.CONTROLLERS_NOTIFICATION

  private val url = configuration.get[String]("notification.url")

  private val authorizationKey = configuration.get[String]("notification.authorizationKey")

  def sendNotification(accountID: String, messageType: String, passedData: Seq[String] = Seq(""))(implicit lang: Lang = Lang(accounts.Service.getLanguage(accountID))): WSResponse = {
    try {
      notifications.Service.addNotification(accountID, messagesApi(module + "Title" + "." + messageType), messagesApi(module + "Message" + "." + messageType, passedData(0)), DateTime.now(DateTimeZone.UTC).getMillis())
      Await.result(wsClient.url(url).withHttpHeaders(constants.Header.CONTENT_TYPE -> constants.Header.APPLICATION_JSON).withHttpHeaders(constants.Header.AUTHORIZATION -> authorizationKey)
        .post(Json.toJson(Data(accountTokens.Service.getTokenById(accountID), Notification(messagesApi(module + "Title" + "." + messageType), messagesApi(module + "Message" + "." + messageType, passedData(0)))))), Duration.Inf)
    }
    catch {
      case baseException: BaseException => throw new BaseException(baseException.message)
    }
  }

  private implicit val notificationWrites: OWrites[Notification] = Json.writes[Notification]

  def registerNotificationToken(id: String, notificationToken: String): Int = accountTokens.Service.updateToken(id, notificationToken)

  private implicit val dataWrites: OWrites[Data] = Json.writes[Data]

  private case class Notification(title: String, body: String)

  private case class Data(to: String, notification: Notification)

}

