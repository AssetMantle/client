package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object IssueAssetRequest {

  val form = Form(
    mapping(
      constants.Forms.DOCUMENT_HASH -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.ASSET_TYPE -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.ASSET_PRICE -> number(min = 1, max = 10000),
      constants.Forms.QUANTITY_UNIT -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.ASSET_QUANTITY -> number(min = 1, max = 10000),
    )(Data.apply)(Data.unapply)
  )

  case class Data(documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int)

}

