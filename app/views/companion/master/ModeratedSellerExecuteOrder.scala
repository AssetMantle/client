package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ModeratedSellerExecuteOrder {
  val form = Form(
    mapping(
      constants.FormField.ORDER_ID.name -> constants.FormField.ORDER_ID.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(orderID: String, gas: Int, password: String)

}
