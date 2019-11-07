package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SendFiat {
  val form = Form(
    mapping(
      constants.FormField.SELLER_ADDRESS.name -> constants.FormField.SELLER_ADDRESS.field,
      constants.FormField.AMOUNT.name -> constants.FormField.AMOUNT.field,
      constants.FormField.PEG_HASH.name -> constants.FormField.PEG_HASH.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(sellerAddress: String, amount: Int, pegHash: String, gas: Int = 0, password: String = "")

}