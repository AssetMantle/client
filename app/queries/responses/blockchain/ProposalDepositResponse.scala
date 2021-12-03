package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Coin
import models.blockchain.{ProposalDeposit => BlockchainProposalDeposit}
import transactions.Abstract.BaseResponse

object ProposalDepositResponse {

  case class Deposit(proposal_id: String, depositor: String, amount: Seq[Coin]) {
    def toSerializableProposalDeposit: BlockchainProposalDeposit = BlockchainProposalDeposit(proposalID = proposal_id.toInt, depositor = depositor, amount = amount.map(_.toCoin))
  }

  implicit val depositReads: Reads[Deposit] = Json.reads[Deposit]

  case class Response(deposit: Deposit) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
