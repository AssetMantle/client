package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object RedeemAsset {
  val form = Form(
    mapping(
      "from" -> nonEmptyText,
      "to" -> nonEmptyText,
      "pegHash" -> nonEmptyText,
      "chainID" -> nonEmptyText,
      "password" -> nonEmptyText,
      "gas" -> number
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, to: String, pegHash: String, chainID: String, password: String, gas: Int)

}