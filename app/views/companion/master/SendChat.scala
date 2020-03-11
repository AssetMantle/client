package views.companion.master

import constants.FormField
import play.api.data.Form
import play.api.data.Forms._

object SendChat {
  val form = Form(
    mapping(
      constants.FormField.CHAT_WINDOW_ID.name -> constants.FormField.CHAT_WINDOW_ID.field,
      constants.FormField.MESSAGE.name -> constants.FormField.MESSAGE.field,
      constants.FormField.REPLY_TO_CHAT.name -> optional(FormField.REPLY_TO_CHAT.field),
    )(Data.apply)(Data.unapply)
  )

  case class Data(chatWindowID: String, message:String, replyToID: Option[String])

}