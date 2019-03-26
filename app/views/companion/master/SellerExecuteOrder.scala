package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SellerExecuteOrder {
  val form = Form(
    mapping(
      "password" -> nonEmptyText(minLength = 1, maxLength = 20),
      "buyerAddress" -> nonEmptyText(minLength = 1, maxLength = 45),
      "sellerAddress" -> nonEmptyText(minLength = 1, maxLength = 45),
      "awbProofHash" -> nonEmptyText(minLength = 1, maxLength = 20),
      "pegHash" -> nonEmptyText(minLength = 1, maxLength = 20),
      "gas" -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, buyerAddress: String, sellerAddress: String, awbProofHash: String, pegHash: String, gas: Int)

}
