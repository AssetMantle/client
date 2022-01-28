package utilities

import models.Abstract.Authz.Authorization
import models.common.FeeGrant.Allowance
import models.common.Serializable.Coin
import utilities.Date.RFC3339

object Blockchain {

  def addCoins(oldCoins: Seq[Coin], add: Seq[Coin]): Seq[Coin] = if (oldCoins.nonEmpty) {
    val newCoins = oldCoins.map(oldCoin => add.find(_.denom == oldCoin.denom).fold(oldCoin)(addCoin => Coin(denom = addCoin.denom, amount = oldCoin.amount + addCoin.amount)))
    newCoins ++ add.filter(x => !newCoins.map(_.denom).contains(x.denom))
  } else add

  def subtractCoins(fromCoins: Seq[Coin], amount: Seq[Coin]): (Seq[Coin], Boolean) = {
    val result = addCoins(fromCoins, amount.map(x => x.copy(amount = x.amount * -1)))
    (result, result.exists(_.isNegative == true))
  }

  object Authz {
    case class ValidateResponse(accept: Boolean, delete: Boolean, updated: Option[Authorization])
  }

  object FeeGrant {
    case class ValidateResponse(delete: Boolean, updated: Allowance)
  }

  case class SlashingEvidence(height: Int, time: RFC3339, validatorHexAddress: String, validatorPower: MicroNumber)
}
