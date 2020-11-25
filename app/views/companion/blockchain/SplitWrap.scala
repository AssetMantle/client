package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.{mapping, optional, seq}
import utilities.MicroNumber
import views.companion.common._

object SplitWrap {

  val form: Form[Data] = Form(
    mapping(
      constants.FormField.FROM_ID.name -> constants.FormField.FROM_ID.field,
      constants.FormField.COINS.name -> seq(optional(Coin.subFormMapping)),
      constants.FormField.ADD_FIELD.name -> constants.FormField.ADD_FIELD.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(fromID: String, coins: Seq[Option[Coin.Data]], addField: Boolean, gas: MicroNumber, password: String)

}
