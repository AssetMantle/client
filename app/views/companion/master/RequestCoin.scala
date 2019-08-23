package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object RequestCoin {
  val form = Form(
    mapping(
      constants.Form.COUPON -> constants.FormField.COUPON.field

    )(Data.apply)(Data.unapply)
  )

  case class Data(coupon: String)

}
