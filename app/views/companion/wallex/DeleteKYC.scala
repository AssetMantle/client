package views.companion.wallex

import play.api.data.Form
import play.api.data.Forms.mapping

object DeleteKYC {

  val form = Form(
    mapping(
      constants.FormField.DOCUMENT_TYPE.name -> constants.FormField.DOCUMENT_TYPE.field,
      constants.FormField.FILE_ID.name -> constants.FormField.FILE_ID.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(documentType: String,
                  fileID: String)

}
