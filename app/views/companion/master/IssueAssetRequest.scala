package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object IssueAssetRequest {

  val form = Form(
    mapping(
      constants.FormField.DOCUMENT_HASH.name -> constants.FormField.DOCUMENT_HASH.field,
      constants.FormField.ASSET_TYPE.name -> constants.FormField.ASSET_TYPE.field,
      constants.FormField.ASSET_PRICE.name -> constants.FormField.ASSET_PRICE.field,
      constants.FormField.QUANTITY_UNIT.name -> constants.FormField.QUANTITY_UNIT.field,
      constants.FormField.ASSET_QUANTITY.name -> constants.FormField.ASSET_QUANTITY.field,
      constants.Form.MODERATED -> boolean,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.TAKER_ADDRESS.name -> constants.FormField.TAKER_ADDRESS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, moderated: Boolean,gas:Int, takerAddress: String, password: String)

}

