package views.companion.wallex

import play.api.data.Form
import play.api.data.Forms.mapping

object GetUserAccount {

  val form = Form(
    mapping(
      constants.FormField.WALLEX_USER_ID.name -> constants.FormField.WALLEX_USER_ID.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(
      userID: String
  )

}
