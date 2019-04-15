package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object IssueFiatRequest {
  val form = Form(
    mapping(
      constants.Form.TRANSACTION_ID -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.TRANSACTION_AMOUNT -> number(min = 1, max = 10000),
    )(Data.apply)(Data.unapply)
  )

  case class Data(transactionID: String, transactionAmount: Int)

}
