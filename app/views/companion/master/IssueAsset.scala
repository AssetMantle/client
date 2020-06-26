package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object IssueAsset {
  val form = Form(
    mapping(
      constants.FormField.ASSET_TYPE.name -> constants.FormField.ASSET_TYPE.field,
      constants.FormField.ASSET_DESCRIPTION.name -> constants.FormField.ASSET_DESCRIPTION.field,
      constants.FormField.ASSET_QUANTITY.name -> constants.FormField.ASSET_QUANTITY.field,
      constants.FormField.QUANTITY_UNIT.name -> constants.FormField.QUANTITY_UNIT.field,
      constants.FormField.ASSET_PRICE_PER_UNIT.name -> constants.FormField.ASSET_PRICE_PER_UNIT.field,
      constants.FormField.SHIPPING_PERIOD.name -> constants.FormField.SHIPPING_PERIOD.field,
      constants.FormField.PORT_OF_LOADING.name -> constants.FormField.PORT_OF_LOADING.field,
      constants.FormField.PORT_OF_DISCHARGE.name -> constants.FormField.PORT_OF_DISCHARGE.field,
      constants.FormField.MODERATED.name -> constants.FormField.MODERATED.field,
      constants.FormField.GAS.name -> optional(constants.FormField.GAS.field),
      constants.FormField.PASSWORD.name -> optional(constants.FormField.PASSWORD.field),
    )(Data.apply)(Data.unapply).verifying(constants.FormConstraint.issueAssetConstraint)
  )

  case class Data(assetType: String, description: String, quantity: Int, quantityUnit: String, pricePerUnit: Double, shippingPeriod: Int, portOfLoading: String, portOfDischarge: String, moderated: Boolean, gas: Option[Int], password: Option[String])

}
