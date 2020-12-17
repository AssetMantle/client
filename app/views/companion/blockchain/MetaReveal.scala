package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.mapping
import utilities.MicroNumber
import views.companion.common._

object MetaReveal {

  val form: Form[Data] = Form(
    mapping(
      constants.FormField.REVEAL_FACT.name -> Fact.subFormMapping,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(revealFact: Fact.Data, gas: MicroNumber, password: String)

}
