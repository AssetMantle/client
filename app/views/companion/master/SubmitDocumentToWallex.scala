package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object SubmitDocumentToWallex {
  val form = Form(
    mapping(
      constants.FormField.DOCUMENT_TYPE.name -> constants.FormField.DOCUMENT_TYPE.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(documentType: String)

}
