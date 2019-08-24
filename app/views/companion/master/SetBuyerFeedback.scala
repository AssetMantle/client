package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SetBuyerFeedback {
  val form = Form(
    mapping(
      constants.FormField.NON_EMPTY_PASSWORD.name -> constants.FormField.NON_EMPTY_PASSWORD.field,
      constants.FormField.SELLER_ADDRESS.name -> constants.FormField.SELLER_ADDRESS.field,
      constants.FormField.PEG_HASH.name -> constants.FormField.PEG_HASH.field,
      constants.FormField.RATING.name -> constants.FormField.RATING.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, sellerAddress: String, pegHash: String, rating: Int)

}