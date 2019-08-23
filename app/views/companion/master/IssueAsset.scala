package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object IssueAsset {
  val form = Form(
    mapping(
      constants.Form.REQUEST_ID -> constants.FormField.REQUEST_ID.field,
      constants.Form.ACCOUNT_ID -> constants.FormField.ACCOUNT_ID.field,
      constants.Form.DOCUMENT_HASH -> constants.FormField.HASH.field,
      constants.Form.ASSET_TYPE -> constants.FormField.ASSET_TYPE.field,
      constants.Form.ASSET_PRICE -> constants.FormField.ASSET_PRICE.field,
      constants.Form.QUANTITY_UNIT -> constants.FormField.QUANTITY_UNIT.field,
      constants.Form.ASSET_QUANTITY -> constants.FormField.ASSET_QUANTITY.field,
      constants.Form.PASSWORD -> constants.FormField.PASSWORD.field,
      constants.Form.GAS -> constants.FormField.GAS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String, accountID: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, password: String, gas: Int)

}
