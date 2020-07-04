package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping
import utilities.MicroLong


object UpdateNegotiationAssetTerms {
  val form = Form(
    mapping(
      constants.FormField.ID.name -> constants.FormField.ID.field,
      constants.FormField.ASSET_DESCRIPTION.name -> constants.FormField.ASSET_DESCRIPTION.field,
      constants.FormField.ASSET_PRICE_PER_UNIT.name -> constants.FormField.ASSET_PRICE_PER_UNIT.field,
      constants.FormField.ASSET_QUANTITY.name -> constants.FormField.ASSET_QUANTITY.field,
      constants.FormField.QUANTITY_UNIT.name -> constants.FormField.QUANTITY_UNIT.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(id: String, description: String, pricePerUnit: MicroLong, quantity: MicroLong, quantityUnit: String, gas: MicroLong, password: String)

}
