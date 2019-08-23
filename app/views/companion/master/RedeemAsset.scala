package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object RedeemAsset {
  val form = Form(
    mapping(
      constants.Form.ZONE_ID -> constants.FormField.ZONE_ID.field,
      constants.Form.PEG_HASH -> constants.FormField.PEG_HASH.field,
      constants.Form.PASSWORD -> constants.FormField.PASSWORD.field,
      constants.Form.GAS -> constants.FormField.GAS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(zoneID: String, pegHash: String, password: String, gas: Int)

}