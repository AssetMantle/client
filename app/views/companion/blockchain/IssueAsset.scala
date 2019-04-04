package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object IssueAsset {
  val form = Form(
    mapping(
      constants.Forms.FROM -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.TO -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Forms.DOCUMENT_HASH -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.ASSET_TYPE -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.ASSET_PRICE -> number(min = 1, max = 10000),
      constants.Forms.QUANTITY_UNIT -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.ASSET_QUANTITY -> number(min = 1, max = 10000),
      constants.Forms.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.GAS -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, to: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, password: String, gas: Int)

}
