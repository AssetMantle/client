package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.mapping
import utilities.MicroNumber

object AssetBurn {

  val form: Form[Data] = Form(
    mapping(
      constants.FormField.FROM_ID.name -> constants.FormField.FROM_ID.field,
      constants.FormField.ASSET_ID.name -> constants.FormField.ASSET_ID.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(fromID: String, assetID: String, gas: MicroNumber, password: String)

}