package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SendCoin {
  val form = Form(
    mapping(
      constants.Form.TO -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Form.AMOUNT -> number(min = 1, max = 1000000),
      constants.Form.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20),
    )(Data.apply)(Data.unapply)
  )

  case class Data(to: String, amount: Int, password: String)
}
