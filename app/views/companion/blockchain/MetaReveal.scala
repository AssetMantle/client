package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.mapping
import utilities.MicroNumber
import views.companion.common._

object MetaReveal {

  val form: Form[Data] = Form(
    mapping(
      constants.FormField.FROM.name -> constants.FormField.FROM.field,
      constants.FormField.REVEAL_FACT.name -> Fact.subFormMapping,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, revealFact: Fact.Data, gas: MicroNumber)

}
