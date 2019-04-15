package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object VerifyZone {


  val form = Form(
    mapping(
      constants.Forms.ID -> nonEmptyText(minLength = 4, maxLength = 20),
      constants.Forms.PASSWORD -> nonEmptyText(minLength = 4, maxLength = 20),
      constants.Forms.STATUS -> boolean,
    )(Data.apply)(Data.unapply)
  )

  case class Data(id: String, password: String, status: Boolean)

}
