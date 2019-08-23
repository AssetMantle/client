package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object IssueAsset {
  val form = Form(
    mapping(
      constants.Form.FROM -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.TO -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Form.DOCUMENT_HASH -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.ASSET_TYPE -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.ASSET_PRICE -> number(min = 1, max = 10000),
      constants.Form.QUANTITY_UNIT -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.ASSET_QUANTITY -> number(min = 1, max = 10000),
      constants.Form.MODERATED -> boolean,
      constants.Form.TAKER_ADDRESS -> text(minLength = 45, maxLength = 45),
      constants.Form.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.MODE-> nonEmptyText(minLength = 4, maxLength = 5)
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, to: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, moderated: Boolean, takerAddress: String, password: String, mode: String)

}
