package utilities

import javax.inject.Inject
import models.masterTransaction.AccountTokens
import models.master.Accounts
import play.api.Configuration
import play.api.i18n.{Lang, Langs, MessagesApi}
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.WSClient
import scala.concurrent.ExecutionContext

class PushNotifications @Inject()(wsClient: WSClient,accounts: Accounts, accountTokens: AccountTokens, langs: Langs, messagesApi: MessagesApi)(implicit exec: ExecutionContext, configuration: Configuration) {

  private val url = configuration.get[String]("notification.url")

  private val authorizationKey = configuration.get[String]("notification.authorizationKey")

  private case class Notification(title: String, body: String)
  private implicit val notificationWrites: OWrites[Notification] = Json.writes[Notification]

  private case class Data(to: String, notification: Notification)
  private implicit val dataWrites: OWrites[Data] = Json.writes[Data]

  def sendNotification(id: String, messageType: String, passedData: Seq[String] = Seq(""))(implicit currentModule: String, lang: Lang = Lang(accounts.Service.getLanguageById(id)))= {
    Thread.sleep(3000)
    wsClient.url(url).withHttpHeaders(constants.Header.CONTENT_TYPE -> constants.Header.APPLICATION_JSON).withHttpHeaders(constants.Header.AUTHORIZATION -> authorizationKey)
      .post(Json.toJson(Data(accountTokens.Service.getTokenById(id), Notification(messagesApi("NotificationTitle"+"."+messageType), messagesApi("NotificationMessage"+"."+messageType, passedData(0))))))
  }

  def registerNotificationToken(id: String, notificationToken: String): Int = accountTokens.Service.updateToken(id, notificationToken)

}

