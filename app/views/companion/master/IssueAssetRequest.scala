package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object IssueAssetRequest {

  val form = Form(
    mapping(
      constants.Form.DOCUMENT_HASH -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.ASSET_TYPE -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.ASSET_PRICE -> number(min = 1, max = 10000),
      constants.Form.QUANTITY_UNIT -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.ASSET_QUANTITY -> number(min = 1, max = 10000),
    )(Data.apply)(Data.unapply)
  )

  case class Data(documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int)

}

