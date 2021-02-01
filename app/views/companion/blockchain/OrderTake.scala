package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.mapping
import utilities.MicroNumber

object OrderTake {

  val form: Form[Data] = Form(
    mapping(
      constants.FormField.FROM_ID.name -> constants.FormField.FROM_ID.field,
      constants.FormField.ORDER_ID.name -> constants.FormField.ORDER_ID.field,
      constants.FormField.TAKER_OWNABLE_SPLIT.name -> constants.FormField.TAKER_OWNABLE_SPLIT.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(fromID: String, orderID: String, takerOwnableSplit: BigDecimal, gas: MicroNumber, password: String)

}
