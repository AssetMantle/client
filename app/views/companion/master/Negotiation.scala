package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object Negotiation {
  val form = Form(
    mapping(
      constants.FormField.ASSET_ID.name -> constants.FormField.ASSET_ID.field,
      constants.FormField.COUNTER_PARTY.name -> constants.FormField.COUNTER_PARTY.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(assetID: String, counterParty: String)
}
