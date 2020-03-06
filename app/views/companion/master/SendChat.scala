package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SendChat {
  val form = Form(
    mapping(
      constants.FormField.TRADE_ROOM_ID.name -> constants.FormField.TRADE_ROOM_ID.field,
      constants.FormField.CHAT_CONTENT.name -> constants.FormField.CHAT_CONTENT.field,
      constants.FormField.FINANCIER_VISIBILITY.name -> constants.FormField.FINANCIER_VISIBILITY.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(tradeRoomID: String, chatContent:String, financierVisibility: Boolean)

}