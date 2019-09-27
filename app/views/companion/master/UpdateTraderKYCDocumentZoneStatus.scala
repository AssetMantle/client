package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{boolean, mapping}

object UpdateTraderKYCDocumentZoneStatus {
  val form = Form(
    mapping(
      constants.FormField.TRADER_ID.name -> constants.FormField.TRADER_ID.field,
      constants.FormField.DOCUMENT_TYPE.name -> constants.FormField.DOCUMENT_TYPE.field,
      constants.Form.ZONE_STATUS -> boolean
    )(Data.apply)(Data.unapply)
  )

  case class Data(traderID: String, documentType: String, zoneStatus: Boolean)
}
