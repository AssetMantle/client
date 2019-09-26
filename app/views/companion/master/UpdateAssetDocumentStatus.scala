package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{boolean, mapping}

object UpdateAssetDocumentStatus {
  val form = Form(
    mapping(
      constants.FormField.FILE_ID.name -> constants.FormField.FILE_ID.field,
      constants.FormField.DOCUMENT_TYPE.name -> constants.FormField.DOCUMENT_TYPE.field,
      constants.Form.STATUS -> boolean
    )(Data.apply)(Data.unapply)
  )

  case class Data(fileID: String, documentType: String, status: Boolean)
}
