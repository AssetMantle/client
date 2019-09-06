package views.companion.master

import java.util.Date

import play.api.data.Form
import play.api.data.Forms._

object IssueAssetInvoice {

  val form = Form(
    mapping(
      constants.FormField.ISSUE_ASSET_REQUEST_ID.name -> constants.FormField.ISSUE_ASSET_REQUEST_ID.field,
      constants.FormField.INVOICE_NUMBER.name -> constants.FormField.INVOICE_NUMBER.field,
      constants.FormField.INVOICE_DATE.name -> constants.FormField.INVOICE_DATE.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String, invoiceNumber: String, invoiceDate: Date)

}

