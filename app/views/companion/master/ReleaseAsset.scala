package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ReleaseAsset {
  val form = Form(
    mapping(
      constants.Form.BLOCKCHAIN_ADDRESS -> constants.FormField.BLOCKCHAIN_ADDRESS.field,
      constants.Form.PEG_HASH -> constants.FormField.PEG_HASH.field,
      constants.Form.PASSWORD -> constants.FormField.PASSWORD.field,
      constants.Form.GAS -> constants.FormField.GAS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(address: String, pegHash: String, password: String, gas: Int)

}