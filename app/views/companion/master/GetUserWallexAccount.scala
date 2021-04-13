package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object GetUserWallexAccount {

  val form = Form(
    mapping(
      constants.FormField.USER_ID.name -> constants.FormField.USER_ID.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(
      userID: String,
  )

}
