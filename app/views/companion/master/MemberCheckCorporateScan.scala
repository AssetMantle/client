package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object MemberCheckCorporateScan {
  val form = Form(
    mapping(
      constants.FormField.COMPANY_NAME.name -> constants.FormField.COMPANY_NAME.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(companyName: String)

}
