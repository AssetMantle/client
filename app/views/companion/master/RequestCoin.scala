package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}

object RequestCoin {
  val form = Form(
    mapping(
      constants.Forms.COUPON -> nonEmptyText(minLength = 0, maxLength = 50),

    )(Data.apply)(Data.unapply)
  )

  case class Data(coupon: String)

}
