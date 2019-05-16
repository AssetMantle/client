package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ReleaseAsset {
  val form = Form(
    mapping(
      constants.Form.ADDRESS -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Form.PEG_HASH -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.GAS -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(address: String, pegHash: String, password: String, gas: Int)

}