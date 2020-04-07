package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, optional}

object RejectOrganizationRequest {

  val form = Form(
    mapping(
      constants.FormField.ORGANIZATION_ID.name -> constants.FormField.ORGANIZATION_ID.field,
      constants.FormField.COMMENT.name -> optional(constants.FormField.COMMENT.field),
    )(Data.apply)(Data.unapply)
  )

  case class Data(organizationID: String, comment: Option[String])

}
