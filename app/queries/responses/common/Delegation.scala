package queries.responses.common

import models.blockchain.{Delegation => BlcokchainDelegation}
import play.api.libs.json.{Json, Reads}

case class Delegation(delegator_address: String, validator_address: String, shares: BigDecimal) {
  def toDelegation: BlcokchainDelegation = BlcokchainDelegation(delegatorAddress = delegator_address, validatorAddress = validator_address, shares = shares)
}

object Delegation {

  implicit val delegationReads: Reads[Delegation] = Json.reads[Delegation]

  case class Result(delegation: Delegation)

  implicit val resultReads: Reads[Result] = Json.reads[Result]
}
