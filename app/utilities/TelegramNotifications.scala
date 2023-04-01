package utilities

import play.api.libs.ws.WSClient

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TelegramNotifications @Inject()(
                                       wsClient: WSClient,
                                     )(implicit executionContext: ExecutionContext) {

  private val canSend = constants.AppConfig.Telegram.BotToken != "" && constants.AppConfig.Telegram.ChatId != ""

  private val url = "https://api.telegram.org/bot" + constants.AppConfig.Telegram.BotToken + "/sendMessage?chat_id=" + constants.AppConfig.Telegram.ChatId + "&text="

  def send(message: String): Unit = if (canSend && message != "") wsClient.url(url + message).get

}
