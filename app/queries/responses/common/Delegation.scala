package queries.responses.common

import models.blockchain.{Delegation => BlcokchainDelegation}
import play.api.libs.json.{Json, Reads}

object Delegation {

  case class Result(delegator_address: String, validator_address: String, shares: BigDecimal, balance: Coin) {
    def toDelegation: BlcokchainDelegation = BlcokchainDelegation(delegatorAddress = delegator_address, validatorAddress = validator_address, shares = shares)
  }

  implicit val resultReads: Reads[Result] = Json.reads[Result]
}
