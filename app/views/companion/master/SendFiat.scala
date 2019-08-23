package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SendFiat {
  val form = Form(
    mapping(
      constants.Form.PASSWORD -> constants.FormField.PASSWORD.field,
      constants.Form.SELLER_ADDRESS -> constants.FormField.BLOCKCHAIN_ADDRESS.field,
      constants.Form.AMOUNT -> constants.FormField.AMOUNT.field,
      constants.Form.PEG_HASH -> constants.FormField.PEG_HASH.field,
      constants.Form.GAS -> constants.FormField.GAS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, sellerAddress: String, amount: Int, pegHash: String, gas: Int)

}