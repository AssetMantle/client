package utilities

import models.blockchain.Validator

object Delegations {

  def getTokenAmountFromShares(validator: Validator, shares: BigDecimal): MicroNumber = MicroNumber((shares * (validator.delegatorShares / BigDecimal(validator.tokens.value))).toBigInt())

}
