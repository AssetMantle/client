package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject._
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc._
import views.companion.master.NotificationToken

import scala.concurrent.ExecutionContext


@Singleton
class NotificationController @Inject()(cc: ControllerComponents, ws: WSClient)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(cc) with I18nSupport {

  def webpush() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.component.master.webPush(NotificationToken.form))
  }

  def sendNotification: Action[AnyContent] = Action { implicit request =>
    NotificationToken.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.webPush(formWithErrors))
      },
      loginData => {
        try {
          val data: JsValue = Json.obj(
            "data" -> Json.obj(
              "notification" -> Json.obj(
                "title" -> "FCM Foreground Message",
                "body" -> "This is an FCM Foreground Message",
                "icon" -> "notificationImage.png"
              )
            ),
            "to" -> loginData.token
          )
          Thread.sleep(3000)
          val reqPost: WSRequest = ws.url("https://fcm.googleapis.com/fcm/send")
            .withHttpHeaders("Content-Type" -> "application/json")
            .withHttpHeaders("Authorization" -> "key=AAAAwSmfGOo:APA91bGSaXARMCGOFGuEB-wJM2tyLQU3HhTRxb1SZiWefjDq3gGiXuVogs9n7jA-MafSK5cJQ4YS8b_9LvlxQUqzTa5jGDHXVd6l9zp8xeXT30gUUDOGmR5Z_-QnFUNQC1xifiyzUKqd")
          reqPost.post(data)
          Ok(Json.prettyPrint(data))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      }
    )
  }
}