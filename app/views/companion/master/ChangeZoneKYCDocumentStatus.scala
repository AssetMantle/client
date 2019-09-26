package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, boolean}

object ChangeZoneKYCDocumentStatus {
  val form = Form(
    mapping(
      constants.FormField.ZONE_ID.name -> constants.FormField.ZONE_ID.field,
      constants.FormField.DOCUMENT_TYPE.name -> constants.FormField.DOCUMENT_TYPE.field,
      constants.Form.STATUS -> boolean
    )(Data.apply)(Data.unapply)
  )

  case class Data(zoneID: String, documentType: String, status: Boolean)
}
