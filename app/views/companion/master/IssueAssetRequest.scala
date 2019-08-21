package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object IssueAssetRequest {

  val form = Form(
    mapping(
      constants.Form.DOCUMENT_HASH -> nonEmptyText(minLength = constants.FormConstraint.HASH_MINIMUM_LENGTH, maxLength = constants.FormConstraint.HASH_MAXIMUM_LENGTH),
      constants.Form.ASSET_TYPE -> nonEmptyText(minLength = constants.FormConstraint.ASSET_TYPE_MINIMUM_LENGTH, maxLength = constants.FormConstraint.ASSET_TYPE_MAXIMUM_LENGTH),
      constants.Form.ASSET_PRICE -> number(min = constants.FormConstraint.PRICE_MINIMUM_VALUE, max = constants.FormConstraint.PRICE_MAXIMUM_VALUE),
      constants.Form.QUANTITY_UNIT -> nonEmptyText(minLength = constants.FormConstraint.QUANTITY_LENGTH_MINIMUM_LENGTH, maxLength = constants.FormConstraint.QUANTITY_LENGTH_MAXIMUM_LENGTH),
      constants.Form.ASSET_QUANTITY -> number(min = constants.FormConstraint.ASSET_QUANTITY_MINIMUM_VALUE, max = constants.FormConstraint.ASSET_QUANTITY_MAXIMUM_VALUE),
      constants.Form.MODERATED -> boolean,
      constants.Form.GAS -> number(min = 0, max = constants.FormConstraint.GAS_MAXIMUM_VALUE),
      constants.Form.PASSWORD -> text(maxLength = constants.FormConstraint.PASSWORD_MAXIMUM_LENGTH),
    )(Data.apply)(Data.unapply)
  )

  case class Data(documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, moderated: Boolean, gas: Int, password: String)

}

