package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}

object RejectVerifyOrganizationRequest {

  val form = Form(
    mapping(
      constants.Form.ORGANIZATION_ID -> nonEmptyText(minLength = constants.FormConstraint.ORGANIZATION_ID_MINIMUM_LENGTH, maxLength = constants.FormConstraint.ORGANIZATION_ID_MAXIMUM_LENGTH),
    )(Data.apply)(Data.unapply)
  )

  case class Data(organizationID: String)

}
