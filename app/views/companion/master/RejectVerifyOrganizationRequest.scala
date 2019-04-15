package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}

object RejectVerifyOrganizationRequest {

  val form = Form(
    mapping(
      constants.Form.ORGANIZATION_ID -> nonEmptyText(minLength = 4, maxLength = 45),
    )(Data.apply)(Data.unapply)
  )

  case class Data(organizationID: String)

}
