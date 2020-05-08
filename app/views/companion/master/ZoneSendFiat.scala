package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ZoneSendFiat {
  val form = Form(
    mapping(
      constants.FormField.ID.name -> constants.FormField.ID.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(id: String)

}
