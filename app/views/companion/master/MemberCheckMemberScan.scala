package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object MemberCheckMemberScan {
  val form = Form(
    mapping(
      constants.FormField.UBO_ID.name -> constants.FormField.UBO_ID.field,
      constants.FormField.FIRST_NAME.name -> constants.FormField.FIRST_NAME.field,
      constants.FormField.LAST_NAME.name -> constants.FormField.LAST_NAME.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(uboID: String, firstName: String, lastName: String)

}
