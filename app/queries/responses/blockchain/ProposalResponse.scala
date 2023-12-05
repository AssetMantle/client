package queries.responses.blockchain

import models.blockchain.{Proposal => BlockchainProposal}
import models.common.Serializable
import play.api.libs.json.{Json, Reads}
import queries.Abstract.ProposalContent
import queries.responses.common.Coin
import utilities.Date.RFC3339

object ProposalResponse {

  case class FinalTallyResult(yes: String, abstain: String, no: String, no_with_veto: String) {
    def toSerializableFinalTallyResult: Serializable.FinalTallyResult = Serializable.FinalTallyResult(yes = BigDecimal(yes), abstain = BigDecimal(abstain), no = BigDecimal(no), noWithVeto = BigDecimal(no_with_veto))
  }

  implicit val finalTallyResultReads: Reads[FinalTallyResult] = Json.reads[FinalTallyResult]

  case class Proposal(proposal_id: String, content: ProposalContent, status: String, final_tally_result: FinalTallyResult, submit_time: RFC3339, deposit_end_time: RFC3339, total_deposit: Seq[Coin], voting_start_time: RFC3339, voting_end_time: RFC3339) {
    def toSerializableProposal: BlockchainProposal = BlockchainProposal(id = proposal_id.toInt, content = content.toSerializableProposalContent, status = status, finalTallyResult = final_tally_result.toSerializableFinalTallyResult, submitTime = submit_time, depositEndTime = deposit_end_time, totalDeposit = total_deposit.map(_.toCoin), votingStartTime = voting_start_time, votingEndTime = voting_end_time)
  }

  implicit val proposalReads: Reads[Proposal] = Json.reads[Proposal]

  case class Response(proposal: Proposal)

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
