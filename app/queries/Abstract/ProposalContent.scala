package queries.Abstract

import models.Abstract.{ProposalContent => SerializableProposalContent}

abstract class ProposalContent {
  val proposalContentType: String
  val title: String
  val description: String

  def toSerializableProposalContent: SerializableProposalContent
}
