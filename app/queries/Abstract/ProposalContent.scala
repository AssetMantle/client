package queries.Abstract

import models.Abstract.{ProposalContent => SerializableProposalContent}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsObject, JsPath, Reads}
import queries.responses.common.ProposalContents.proposalContentApply

abstract class ProposalContent {
  val proposalContentType: String
  val title: String
  val description: String

  def toSerializableProposalContent: SerializableProposalContent
}

object ProposalContent {
  // Cannot do simply as interface with member `@type` because structure of TextProposal and CancelSoftwareUpgrade is exactly same.
  // Though TextProposal seems to be part of legacy
  implicit val proposalContentReads: Reads[ProposalContent] = (
    (JsPath \ "@type").read[String] and
      JsPath.read[JsObject]
    ) (proposalContentApply _)
}