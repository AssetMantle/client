package views.companion.master.memberCheck

import play.api.data.Form
import play.api.data.Forms.{mapping, optional}

object AddUBOMemberCheck {

  val form = Form(
    mapping(
      constants.FormField.UBO_ID.name -> constants.FormField.UBO_ID.field,
      constants.FormField.SCAN_ID.name -> constants.FormField.SCAN_ID.field,
      constants.FormField.RESULT_ID.name -> optional(constants.FormField.RESULT_ID.field),
      constants.FormField.STATUS.name -> constants.FormField.STATUS.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(uboID: String, scanID: Int, resultID: Option[Int], status: Boolean)

}
