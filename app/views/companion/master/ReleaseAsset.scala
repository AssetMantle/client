package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ReleaseAsset {
  val form = Form(
    mapping(
      constants.FormField.BLOCKCHAIN_ADDRESS.name -> constants.FormField.BLOCKCHAIN_ADDRESS.field,
      constants.FormField.PEG_HASH.name -> constants.FormField.PEG_HASH.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(address: String, pegHash: String, password: String, gas: Int)

}