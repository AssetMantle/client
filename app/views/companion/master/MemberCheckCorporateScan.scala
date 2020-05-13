package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object MemberCheckCorporateScan {
  val form = Form(
    mapping(
      constants.FormField.ORGANIZATION_ID.name -> constants.FormField.ORGANIZATION_ID.field,
      constants.FormField.COMPANY_NAME.name -> constants.FormField.COMPANY_NAME.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(organizationID: String, companyName: String)

}
