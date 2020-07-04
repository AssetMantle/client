package views.companion.master

import play.api.data.Form
import play.api.data.Forms._
import utilities.MicroNumber

object SetSellerFeedback {
  val form = Form(
    mapping(
      constants.FormField.BUYER_ADDRESS.name -> constants.FormField.BUYER_ADDRESS.field,
      constants.FormField.PEG_HASH.name -> constants.FormField.PEG_HASH.field,
      constants.FormField.RATING.name -> constants.FormField.RATING.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(buyerAddress: String, pegHash: String, rating: Int, gas: MicroNumber, password: String)

}