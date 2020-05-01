package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object RedeemAsset {
  val form = Form(
    mapping(
      constants.FormField.ASSET_ID.name -> constants.FormField.ASSET_ID.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(assetID: String, gas: Int, password: String)

}