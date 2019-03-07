package utilities

import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSRequest}

object PushNotifications {
  def sendNotification(token: String)(implicit ws: WSClient) = {
    val data: JsValue = Json.obj(
      "data" -> Json.obj(
        "notification" -> Json.obj(
          "title" -> "Login"
        )
      ),
      "to" -> token
    )
    Thread.sleep(3000)
    val reqPost: WSRequest = ws.url("https://fcm.googleapis.com/fcm/send")
      .withHttpHeaders("Content-Type" -> "application/json")
      .withHttpHeaders("Authorization" -> "key=AAAAwSmfGOo:APA91bGSaXARMCGOFGuEB-wJM2tyLQU3HhTRxb1SZiWefjDq3gGiXuVogs9n7jA-MafSK5cJQ4YS8b_9LvlxQUqzTa5jGDHXVd6l9zp8xeXT30gUUDOGmR5Z_-QnFUNQC1xifiyzUKqd")
    reqPost.post(data)
  }
}