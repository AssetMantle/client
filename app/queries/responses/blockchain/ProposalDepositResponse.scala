package queries.responses.blockchain

import models.blockchain.{ProposalDeposit => BlockchainProposalDeposit}
import play.api.libs.json.{Json, Reads}
import queries.responses.common.Coin


object ProposalDepositResponse {

  case class Deposit(proposal_id: String, depositor: String, amount: Seq[Coin]) {
    def toSerializableProposalDeposit: BlockchainProposalDeposit = BlockchainProposalDeposit(proposalID = proposal_id.toInt, depositor = depositor, amount = amount.map(_.toCoin))
  }

  implicit val depositReads: Reads[Deposit] = Json.reads[Deposit]

  case class Response(deposit: Deposit)

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
