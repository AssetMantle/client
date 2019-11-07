package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object UpdateOrganizationKYCDocumentStatus {
  val form = Form(
    mapping(
      constants.FormField.ORGANIZATION_ID.name -> constants.FormField.ORGANIZATION_ID.field,
      constants.FormField.DOCUMENT_TYPE.name -> constants.FormField.DOCUMENT_TYPE.field,
      constants.FormField.STATUS.name -> constants.FormField.STATUS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(organizationID: String, documentType: String, status: Boolean = false)

}
