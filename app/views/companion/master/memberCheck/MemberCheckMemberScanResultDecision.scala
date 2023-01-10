package views.companion.master.memberCheck

import play.api.data.Form
import play.api.data.Forms.mapping

object MemberCheckMemberScanResultDecision {

  val form = Form(
    mapping(
      constants.FormField.RESULT_ID.name -> constants.FormField.RESULT_ID.field,
      constants.FormField.MATCH_DECISION.name -> constants.FormField.MATCH_DECISION.field,
      constants.FormField.ASSESSED_RISK.name -> constants.FormField.ASSESSED_RISK.field,
      constants.FormField.COMMENT.name -> constants.FormField.COMMENT.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(resultID: Int, matchDecision: String, assessedRisk: String, comment: String)

}
