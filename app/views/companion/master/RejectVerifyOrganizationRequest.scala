package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object RejectVerifyOrganizationRequest {

  val form = Form(
    mapping(
      constants.FormField.ORGANIZATION_ID.name -> constants.FormField.ORGANIZATION_ID.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(organizationID: String)

}
