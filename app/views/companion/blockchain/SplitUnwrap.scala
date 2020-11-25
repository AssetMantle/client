package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.mapping
import utilities.MicroNumber

object SplitUnwrap {

  val form: Form[Data] = Form(
    mapping(
      constants.FormField.FROM_ID.name -> constants.FormField.FROM_ID.field,
      constants.FormField.OWNABLE_ID.name -> constants.FormField.OWNABLE_ID.field,
      constants.FormField.SPLIT.name -> constants.FormField.SPLIT.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(fromID: String, ownableID: String, split: BigDecimal, gas: MicroNumber, password: String)

}
