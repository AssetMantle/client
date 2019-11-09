package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ReleaseAsset {
  val form = Form(
    mapping(
      //TODO BLOCKCHAIN_ADDRESS to Username
      constants.FormField.BLOCKCHAIN_ADDRESS.name -> constants.FormField.BLOCKCHAIN_ADDRESS.field,
      constants.FormField.PEG_HASH.name -> constants.FormField.PEG_HASH.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(blockchainAddress: String, pegHash: String, gas: Int, password: String)

}