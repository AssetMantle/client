package views.companion.wallex

import play.api.data.Form
import play.api.data.Forms.mapping

object SubmitDocument {
  val form = Form(
    mapping(
      constants.FormField.WALLEX_DOCUMENT_TYPE.name -> constants.FormField.WALLEX_DOCUMENT_TYPE.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(documentType: String)

}
