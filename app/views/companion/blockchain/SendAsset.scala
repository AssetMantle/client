package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object SendAsset {
  val form = Form(
    mapping(
      "from" -> nonEmptyText,
      "password" -> nonEmptyText,
      "to" -> nonEmptyText,
      "pegHash" -> nonEmptyText,
      "chainID" -> nonEmptyText,
      "gas" -> number
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, to: String, pegHash: String, chainID: String, gas: Int)

}
