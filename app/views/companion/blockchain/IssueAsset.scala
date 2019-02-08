package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object IssueAsset {
  val form = Form(
    mapping(
      "from" -> nonEmptyText,
      "to" -> nonEmptyText,
      "documentHash" -> nonEmptyText,
      "assetType" -> nonEmptyText,
      "assetPrice" -> number,
      "quantityUnit" -> number,
      "assetQuantity" -> nonEmptyText,
      "chainID" -> nonEmptyText,
      "password" -> nonEmptyText,
      "gas" -> number
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, to: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: Int, assetQuantity: String, chainID: String, password: String, gas: Int)

}
