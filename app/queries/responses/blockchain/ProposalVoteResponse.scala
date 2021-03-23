package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse
import models.blockchain.{ProposalVote => BlockchainProposalVote}

object ProposalVoteResponse {

  case class Vote(proposal_id: String, voter: String, option: String) {
    def toSerializableProposalVote: BlockchainProposalVote = BlockchainProposalVote(proposalID = proposal_id.toInt, voter = voter, option = option)
  }

  implicit val voteReads: Reads[Vote] = Json.reads[Vote]

  case class Response(vote: Vote) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
