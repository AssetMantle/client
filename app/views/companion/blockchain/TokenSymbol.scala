package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object TokenSymbol {
  val form = Form(
    mapping(
      constants.FormField.TOKEN_SYMBOL.name -> constants.FormField.TOKEN_SYMBOL.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(symbol: String)

}
