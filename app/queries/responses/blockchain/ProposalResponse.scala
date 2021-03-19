package queries.responses.blockchain

import models.blockchain.{Proposal => BlockchainProposal}
import play.api.libs.json.{Json, Reads}
import queries.Abstract.ProposalContent
import queries.responses.common.Coin
import models.common.Serializable
import transactions.Abstract.BaseResponse

object ProposalResponse {

  import queries.responses.common.ProposalContent._

  case class FinalTallyResult(yes: String, abstain: String, no: String, no_with_veto: String) {
    def toSerializableFinalTallyResult: Serializable.FinalTallyResult = Serializable.FinalTallyResult(yes = yes, abstain = abstain, no = no, noWithVeto = no_with_veto)
  }

  implicit val finalTallyResultReads: Reads[FinalTallyResult] = Json.reads[FinalTallyResult]

  case class Proposal(proposal_id: String, content: ProposalContent, status: String, final_tally_result: FinalTallyResult, submit_time: String, deposit_end_time: String, total_deposit: Seq[Coin], voting_start_time: String, voting_end_time: String) {
    def toSerializableProposal: BlockchainProposal = BlockchainProposal(id = proposal_id, content = content.toSerializableProposalContent, proposalType = content.proposalContentType, status = status, finalTallyResult = final_tally_result.toSerializableFinalTallyResult, submitTime = submit_time, depositEndTime = deposit_end_time, totalDeposit = total_deposit.map(_.toCoin), votingStartTime = voting_start_time, votingEndTime = voting_end_time)
  }

  implicit val proposalReads: Reads[Proposal] = Json.reads[Proposal]

  case class Response(proposal: Proposal) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
