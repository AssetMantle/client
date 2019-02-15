package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object IssueAsset {
  val form = Form(
    mapping(
      "from" -> nonEmptyText(minLength = 1, maxLength = 20),
      "to" -> nonEmptyText(minLength = 1, maxLength = 45),
      "documentHash" -> nonEmptyText(minLength = 1, maxLength = 20),
      "assetType" -> nonEmptyText(minLength = 1, maxLength = 20),
      "assetPrice" -> number(min = 1, max = 10000),
      "quantityUnit" -> nonEmptyText(minLength = 1, maxLength = 20),
      "assetQuantity" -> number(min = 1, max = 10000),
      "chainID" -> nonEmptyText(minLength = 1, maxLength = 20),
      "password" -> nonEmptyText(minLength = 1, maxLength = 20),
      "gas" -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, to: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, chainID: String, password: String, gas: Int)

}
