package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{boolean, mapping}

object ChangeTraderKYCDocumentOrganizationStatus {
  val form = Form(
    mapping(
      constants.FormField.TRADER_ID.name -> constants.FormField.TRADER_ID.field,
      constants.FormField.DOCUMENT_TYPE.name -> constants.FormField.DOCUMENT_TYPE.field,
      constants.Form.ORGANIZATION_STATUS -> boolean
    )(Data.apply)(Data.unapply)
  )

  case class Data(traderID: String, documentType: String, organizationStatus: Boolean)
}
