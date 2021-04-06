package blockchainTx.common

import play.api.libs.json.{Json, Reads}
import models.blockchain.{Delegation => BlockchainDelegation}

object Delegation {

  case class Result(delegator_address: String, validator_address: String, shares: BigDecimal) {
    def toDelegation: BlockchainDelegation = BlockchainDelegation(delegatorAddress = delegator_address, validatorAddress = validator_address, shares = shares)
  }

  implicit val resultReads: Reads[Result] = Json.reads[Result]
}
