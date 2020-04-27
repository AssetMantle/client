package views.companion.master

import java.util.Date

import play.api.data.Form
import play.api.data.Forms._

object ContractContent {

  val form = Form(
    mapping(
      constants.FormField.ID.name -> constants.FormField.ID.field,
      constants.FormField.CONTRACT_NUMBER.name -> constants.FormField.CONTRACT_NUMBER.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(id: String, contractNumber: String)

}

