package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, optional, seq}

object DocumentsList {
  val form = Form(
    mapping(
      constants.FormField.ID.name -> constants.FormField.ID.field,
      constants.FormField.DOCUMENTS_LIST.name -> seq(optional(constants.FormField.DOCUMENT_TYPE.field)),
      constants.FormField.DOCUMENTS_LIST_COMPLETED.name -> constants.FormField.DOCUMENTS_LIST_COMPLETED.field,
    )(Data.apply)(Data.unapply).verifying(constants.FormConstraint.negotiationDocumentsListConstraint)
  )

  case class Data(id: String, documentsList: Seq[Option[String]], documentsListCompleted: Boolean)

}
