package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object UpdateNegotiationAssetTerms {
  val form = Form(
    mapping(
      constants.FormField.ID.name -> constants.FormField.ID.field,
      constants.FormField.ASSET_DESCRIPTION.name -> constants.FormField.ASSET_DESCRIPTION.field,
      constants.FormField.ASSET_PRICE.name -> constants.FormField.ASSET_PRICE.field,
      constants.FormField.ASSET_QUANTITY.name -> constants.FormField.ASSET_QUANTITY.field,
      constants.FormField.SHIPPING_PERIOD.name -> constants.FormField.SHIPPING_PERIOD.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(id: String, description: String, price: Int, quantity: Int, shippingPeriod: Int)

}
