package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{boolean, mapping}

object ChangeOrganizationKYCDocumentStatus {
  val form = Form(
    mapping(
      constants.FormField.ORGANIZATION_ID.name -> constants.FormField.ORGANIZATION_ID.field,
      constants.FormField.DOCUMENT_TYPE.name -> constants.FormField.DOCUMENT_TYPE.field,
      constants.Form.STATUS -> boolean
    )(Data.apply)(Data.unapply)
  )

  case class Data(organizationID: String, documentType: String, status: Boolean)
}
