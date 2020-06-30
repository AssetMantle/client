package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._
import utilities.MicroLong

object IssueAsset {
  val form = Form(
    mapping(
      constants.FormField.FROM.name -> constants.FormField.FROM.field,
      constants.FormField.TO.name -> constants.FormField.TO.field,
      constants.FormField.DOCUMENT_HASH.name -> constants.FormField.DOCUMENT_HASH.field,
      constants.FormField.ASSET_TYPE.name -> constants.FormField.ASSET_TYPE.field,
      constants.FormField.ASSET_PRICE_PER_UNIT.name -> constants.FormField.ASSET_PRICE_PER_UNIT.field,
      constants.FormField.QUANTITY_UNIT.name -> constants.FormField.QUANTITY_UNIT.field,
      constants.FormField.ASSET_QUANTITY.name -> constants.FormField.ASSET_QUANTITY.field,
      constants.FormField.MODERATED.name -> constants.FormField.MODERATED.field,
      constants.FormField.TAKER_ADDRESS.name -> constants.FormField.TAKER_ADDRESS.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.MODE.name -> constants.FormField.MODE.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, to: String, documentHash: String, assetType: String, assetPricePerUnit: MicroLong, quantityUnit: String, assetQuantity: MicroLong, moderated: Boolean, takerAddress: String, gas: Int, mode: String, password: String)

}
