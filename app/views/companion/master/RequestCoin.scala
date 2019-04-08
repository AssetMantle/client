package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, text}

object RequestCoin {
  val form = Form(
    mapping(
      constants.Forms.COUPON -> text,

    )(Data.apply)(Data.unapply)
  )

  case class Data(coupon: String)

}
