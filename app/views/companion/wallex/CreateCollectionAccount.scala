package views.companion.wallex

import play.api.data.Form
import play.api.data.Forms.mapping

object CreateCollectionAccount {

  val form = Form(
    mapping(
      constants.FormField.ON_BEHALF_OF.name -> constants.FormField.ON_BEHALF_OF.field,
      constants.FormField.NAME.name -> constants.FormField.NAME.field,
      constants.FormField.WALLEX_REFERENCE.name -> constants.FormField.WALLEX_REFERENCE.field,
      constants.FormField.CURRENCY.name -> constants.FormField.CURRENCY.field,
      constants.FormField.PURPOSE.name -> constants.FormField.PURPOSE.field,
      constants.FormField.DESCRIPTION.name -> constants.FormField.DESCRIPTION.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(
      onBehalfOf: String,
      name: String,
      reference: String,
      currency: String,
      purpose: String,
      description: String
  )

}
