package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object TokenDenom {
  val form = Form(
    mapping(
      constants.FormField.TOKEN_DENOM.name -> constants.FormField.TOKEN_DENOM.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(denom: String)

}
