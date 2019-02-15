package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object ReleaseAsset {
  val form = Form(
    mapping(
      "from" -> nonEmptyText(minLength = 1, maxLength = 20),
      "to" -> nonEmptyText(minLength = 1, maxLength = 45),
      "pegHash" -> nonEmptyText(minLength = 1, maxLength = 20),
      "chainID" -> nonEmptyText(minLength = 1, maxLength = 20),
      "password" -> nonEmptyText(minLength = 1, maxLength = 20),
      "gas" -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, to: String, pegHash: String, chainID: String, password: String, gas: Int)

}