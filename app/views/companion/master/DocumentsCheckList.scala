package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, optional}

object DocumentsCheckList {
  val form = Form(
    mapping(
      constants.FormField.ID.name -> constants.FormField.ID.field,
      constants.FormField.BILL_OF_EXCHANGE.name -> constants.FormField.BILL_OF_EXCHANGE.field,
      constants.FormField.COO.name -> constants.FormField.COO.field,
      constants.FormField.COA.name -> constants.FormField.COA.field,
      constants.FormField.OTHER_DOCUMENTS.name -> optional(constants.FormField.OTHER_DOCUMENTS.field),
    )(Data.apply)(Data.unapply)
  )

  case class Data(id: String, billOfExchange: Boolean, coo: Boolean, coa: Boolean, otherDocuments: Option[String])

}
