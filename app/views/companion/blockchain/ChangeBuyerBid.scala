package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object ChangeBuyerBid {
  val form = Form(
    mapping(
      constants.FormField.FROM.name -> constants.FormField.FROM.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
      constants.FormField.TO.name -> constants.FormField.TO.field,
      constants.FormField.BID.name -> constants.FormField.BID.field,
      constants.FormField.TIME.name -> constants.FormField.TIME.field,
      constants.FormField.PEG_HASH.name -> constants.FormField.PEG_HASH.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, to: String, bid: Int, time: Int, pegHash: String, gas: Int)

}
