package queries.Abstract

import models.Abstract.{ProposalContent => SerializableProposalContent}

abstract class ProposalContent {
  val proposalContentType: String

  def toSerializableProposalContent: SerializableProposalContent
}
