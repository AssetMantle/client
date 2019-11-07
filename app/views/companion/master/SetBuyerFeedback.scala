package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SetBuyerFeedback {
  val form = Form(
    mapping(
      constants.FormField.SELLER_ADDRESS.name -> constants.FormField.SELLER_ADDRESS.field,
      constants.FormField.PEG_HASH.name -> constants.FormField.PEG_HASH.field,
      constants.FormField.RATING.name -> constants.FormField.RATING.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(sellerAddress: String, pegHash: String, rating: Int = 0, gas: Int = 0, password: String = "")

}