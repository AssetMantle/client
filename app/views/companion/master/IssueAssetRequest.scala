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
      constants.Form.TAKER_ADDRESS -> text(maxLength = 45),
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, moderated: Boolean, takerAddress: String, password: String)

}

