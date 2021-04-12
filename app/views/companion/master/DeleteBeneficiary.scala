package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object DeleteBeneficiary {

  val form = Form(
    mapping(
      constants.FormField.BENEFICIARY_ID.name -> constants.FormField.BENEFICIARY_ID.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(id: String)

}