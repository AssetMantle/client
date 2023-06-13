package utilities

import models.Abstract.{Authorization, FeeAllowance}
import models.common.Serializable.Coin
import models.masterTransaction.WalletTransaction
import play.api.Logger
import utilities.Date.RFC3339

object Blockchain {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.UTILITIES_BLOCKCHAIN

  case class AccountTransaction(address: String, txHash: String) {
    def toWalletTx(height: Int): WalletTransaction = WalletTransaction(address = this.address, txHash = this.txHash, height = height)
  }

  case class ValidatorTransaction(address: String, txHash: String) {
    def toValidatorTx(height: Int): models.masterTransaction.ValidatorTransaction = models.masterTransaction.ValidatorTransaction(address = this.address, txHash = this.txHash, height = height)
  }

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
    case class ValidateResponse(delete: Boolean, updated: FeeAllowance)
  }

  case class SlashingEvidence(height: Int, time: RFC3339, validatorHexAddress: String, validatorPower: MicroNumber)


}
