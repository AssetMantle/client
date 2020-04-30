package views.companion.master

import java.util.Date

import play.api.data.Form
import play.api.data.Forms._

object AddInvoiceContent {

  val form = Form(
    mapping(
      constants.FormField.NEGOTIATION_ID.name -> constants.FormField.NEGOTIATION_ID.field,
      constants.FormField.INVOICE_NUMBER.name -> constants.FormField.INVOICE_NUMBER.field,
      constants.FormField.INVOICE_DATE.name -> constants.FormField.INVOICE_DATE.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(negotiationID: String, invoiceNumber: String, invoiceDate: Date)

}

