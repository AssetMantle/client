package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object IssueAsset {
  val form = Form(
    mapping(
      constants.Form.REQUEST_ID -> nonEmptyText(minLength = constants.FormConstraint.REQUEST_ID_LENGTH, maxLength = constants.FormConstraint.REQUEST_ID_LENGTH),
      constants.Form.ACCOUNT_ID -> nonEmptyText(minLength = constants.FormConstraint.USERNAME_MINIMUM_LENGTH, maxLength = constants.FormConstraint.USERNAME_MAXIMUM_LENGTH),
      constants.Form.DOCUMENT_HASH -> nonEmptyText(minLength = constants.FormConstraint.HASH_MINIMUM_LENGTH, maxLength = constants.FormConstraint.HASH_MAXIMUM_LENGTH),
      constants.Form.ASSET_TYPE -> nonEmptyText(minLength = constants.FormConstraint.ASSET_TYPE_MINIMUM_LENGTH, maxLength = constants.FormConstraint.ASSET_TYPE_MAXIMUM_LENGTH),
      constants.Form.ASSET_PRICE -> number(min = constants.FormConstraint.PRICE_MINIMUM_VALUE, max = constants.FormConstraint.PRICE_MAXIMUM_VALUE),
      constants.Form.QUANTITY_UNIT -> nonEmptyText(minLength = constants.FormConstraint.QUANTITY_LENGTH_MINIMUM_LENGTH, maxLength = constants.FormConstraint.QUANTITY_LENGTH_MAXIMUM_LENGTH),
      constants.Form.ASSET_QUANTITY -> number(min = constants.FormConstraint.ASSET_QUANTITY_MINIMUM_VALUE, max = constants.FormConstraint.ASSET_QUANTITY_MAXIMUM_VALUE),
      constants.Form.PASSWORD -> nonEmptyText(minLength = constants.FormConstraint.PASSWORD_MINIMUM_LENGTH, maxLength = constants.FormConstraint.PASSWORD_MAXIMUM_LENGTH),
      constants.Form.GAS -> number(min = constants.FormConstraint.GAS_MINIMUM_VALUE, max = constants.FormConstraint.GAS_MAXIMUM_VALUE)
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String, accountID: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, password: String, gas: Int)

}
