package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object IssueAsset {
  val form = Form(
    mapping(
      "to" -> nonEmptyText(minLength = 1, maxLength = 45),
      "documentHash" -> nonEmptyText(minLength = 1, maxLength = 20),
      "assetType" -> nonEmptyText(minLength = 1, maxLength = 20),
      "assetPrice" -> number(min = 1, max = 10000),
      "quantityUnit" -> nonEmptyText(minLength = 1, maxLength = 20),
      "assetQuantity" -> number(min = 1, max = 10000),
      "password" -> nonEmptyText(minLength = 1, maxLength = 20),
      "gas" -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(to: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, password: String, gas: Int)

}
