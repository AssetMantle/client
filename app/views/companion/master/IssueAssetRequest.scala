package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object IssueAssetRequest {

  val form = Form(
    mapping(
      constants.Form.DOCUMENT_HASH -> constants.FormField.HASH.field,
      constants.Form.ASSET_TYPE -> constants.FormField.ASSET_TYPE.field,
      constants.Form.ASSET_PRICE -> constants.FormField.ASSET_PRICE.field,
      constants.Form.QUANTITY_UNIT -> constants.FormField.QUANTITY_UNIT.field,
      constants.Form.ASSET_QUANTITY -> constants.FormField.ASSET_QUANTITY.field,
      constants.Form.MODERATED -> boolean,
      constants.Form.GAS -> constants.FormField.GAS.field,
      constants.Form.PASSWORD -> constants.FormField.PASSWORD.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, moderated: Boolean, gas: Int, password: String)

}

