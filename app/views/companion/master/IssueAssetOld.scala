package views.companion.master

import play.api.data.Form
import play.api.data.Forms._
import utilities.MicroNumber

object IssueAssetOld {
  val form = Form(
    mapping(
      constants.FormField.ID.name -> constants.FormField.ID.field,
      constants.FormField.TRADER_ID.name -> constants.FormField.TRADER_ID.field,
      constants.FormField.DOCUMENT_HASH.name -> constants.FormField.DOCUMENT_HASH.field,
      constants.FormField.ASSET_TYPE.name -> constants.FormField.ASSET_TYPE.field,
      constants.FormField.ASSET_PRICE_PER_UNIT.name -> constants.FormField.ASSET_PRICE_PER_UNIT.field,
      constants.FormField.QUANTITY_UNIT.name -> constants.FormField.QUANTITY_UNIT.field,
      constants.FormField.ASSET_QUANTITY.name -> constants.FormField.ASSET_QUANTITY.field,
      constants.FormField.TAKER_ADDRESS.name -> optional(constants.FormField.TAKER_ADDRESS.field),
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(id: String, tarderID: String, documentHash: String, assetType: String, assetPricePerUnit: MicroNumber, quantityUnit: String, assetQuantity: MicroNumber, takerAddress: Option[String], gas: MicroNumber, password: String)

}
