package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object IssueAsset {
  val form = Form(
    mapping(
      constants.FormField.FROM.name -> constants.FormField.FROM.field,
      constants.FormField.TO.name -> constants.FormField.TO.field,
      constants.FormField.DOCUMENT_HASH.name -> constants.FormField.DOCUMENT_HASH.field,
      constants.FormField.ASSET_TYPE.name -> constants.FormField.ASSET_TYPE.field,
      constants.FormField.ASSET_PRICE.name -> constants.FormField.ASSET_PRICE.field,
      constants.FormField.QUANTITY_UNIT.name -> constants.FormField.QUANTITY_UNIT.field,
      constants.FormField.ASSET_QUANTITY.name -> constants.FormField.ASSET_QUANTITY.field,
      constants.Form.MODERATED -> boolean,
      constants.Form.TAKER_ADDRESS -> text(minLength = 45, maxLength = 45),
      constants.FormField.NON_EMPTY_PASSWORD.name -> constants.FormField.NON_EMPTY_PASSWORD.field,
      constants.Form.MODE-> nonEmptyText(minLength = 4, maxLength = 5)
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, to: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, moderated: Boolean, takerAddress: String, password: String, mode: String)

}
