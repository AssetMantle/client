package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object SetBuyerFeedback {
  val form = Form(
    mapping(
      constants.FormField.FROM.name -> constants.FormField.FROM.field,
      constants.FormField.NON_EMPTY_PASSWORD.name -> constants.FormField.NON_EMPTY_PASSWORD.field,
      constants.FormField.TO.name -> constants.FormField.TO.field,
      constants.FormField.PEG_HASH.name -> constants.FormField.PEG_HASH.field,
      constants.FormField.RATING.name -> constants.FormField.RATING.field,
      constants.Form.MODE-> nonEmptyText(minLength = 4, maxLength = 5)
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, to: String, pegHash: String, rating: Int, mode: String)

}