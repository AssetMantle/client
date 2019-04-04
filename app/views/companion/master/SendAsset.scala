package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SendAsset {
  val form = Form(
    mapping(
      constants.Forms.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.TO -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Forms.PEG_HASH -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.GAS -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, to: String, pegHash: String, gas: Int)

}
