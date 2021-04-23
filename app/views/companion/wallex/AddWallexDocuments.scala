package views.companion.wallex

import play.api.data.Form
import play.api.data.Forms.mapping

object AddWallexDocuments {
  val form = Form(
    mapping(
      constants.FormField.DOCUMENT_TYPE.name -> constants.FormField.DOCUMENT_TYPE.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(documentType: String)

}
