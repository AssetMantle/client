package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object IssueAsset {
  val form = Form(
    mapping(
      constants.Form.REQUEST_ID -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Form.ACCOUNT_ID -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Form.DOCUMENT_HASH -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.ASSET_TYPE -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.ASSET_PRICE -> number(min = 1, max = 10000),
      constants.Form.QUANTITY_UNIT -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.ASSET_QUANTITY -> number(min = 1, max = 10000),
      constants.Form.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20),
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String, accountID: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, password: String)

}
