package views.companion.master

import constants.FormField
import play.api.data.Form
import play.api.data.Forms._

object SendMessage {
  val form = Form(
    mapping(
      constants.FormField.CHAT_WINDOW_ID.name -> constants.FormField.CHAT_WINDOW_ID.field,
      constants.FormField.TEXT.name -> constants.FormField.TEXT.field,
      constants.FormField.REPLY_TO_MESSAGE.name -> optional(FormField.REPLY_TO_MESSAGE.field),
    )(Data.apply)(Data.unapply)
  )

  case class Data(chatID: String, text:String, replyToID: Option[String])

}