package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object SalesQuoteDocuments {
  val form = Form(
    mapping(
      constants.FormField.REQUEST_ID.name -> constants.FormField.REQUEST_ID.field,
      constants.FormField.OBL.name -> constants.FormField.OBL.field,
      constants.FormField.INVOICE.name -> constants.FormField.INVOICE.field,
      constants.FormField.COO.name -> constants.FormField.COO.field,
      constants.FormField.COA.name -> constants.FormField.COA.field,
      constants.FormField.OTHER_DOCUMENTS.name -> constants.FormField.OTHER_DOCUMENTS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String, obl: Boolean, invoice: Boolean, COO: Boolean, COA: Boolean, otherDocuments: String)

}
