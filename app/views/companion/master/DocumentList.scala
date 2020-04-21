package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, optional, seq}

object DocumentList {
  val form = Form(
    mapping(
      constants.FormField.ID.name -> constants.FormField.ID.field,
      constants.FormField.DOCUMENT_LIST.name -> seq(optional(constants.FormField.DOCUMENT_TYPE.field)),
      constants.FormField.DOCUMENT_LIST_COMPLETED.name -> constants.FormField.DOCUMENT_LIST_COMPLETED.field,
    )(Data.apply)(Data.unapply).verifying(constants.FormConstraint.negotiationDocumentListConstraint)
  )

  case class Data(id: String, documentList: Seq[Option[String]], documentListCompleted: Boolean)

}
