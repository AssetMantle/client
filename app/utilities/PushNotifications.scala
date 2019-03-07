package utilities

import com.fasterxml.jackson.core.JsonParseException
import exceptions.BlockChainException
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}

object PushNotifications {
  def sendNotification(token: String)(implicit ws: WSClient, response: WSResponse, logger: Logger) = {
    try {
      val data: JsValue = Json.obj(
        "data" -> Json.obj(
          "notification" -> Json.obj(
            "title" -> "FCM Foreground Message",
            "body" -> "This is an FCM Foreground Message",
            "icon" -> "notificationImage.png"
          )
        ),
        "to" -> token
      )
      Thread.sleep(3000)
      val reqPost: WSRequest = ws.url("https://fcm.googleapis.com/fcm/send")
        .withHttpHeaders("Content-Type" -> "application/json")
        .withHttpHeaders("Authorization" -> "key=AAAAwSmfGOo:APA91bGSaXARMCGOFGuEB-wJM2tyLQU3HhTRxb1SZiWefjDq3gGiXuVogs9n7jA-MafSK5cJQ4YS8b_9LvlxQUqzTa5jGDHXVd6l9zp8xeXT30gUUDOGmR5Z_-QnFUNQC1xifiyzUKqd")
      reqPost.post(data)
    } catch {
      case jsonParseException: JsonParseException => logger.error(response.body.toString, jsonParseException)
        throw new BlockChainException(response.body.toString)
    }
  }
}