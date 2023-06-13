package queries.responses.common

import exceptions.BaseException
import models.blockchain.{Account => BlockchainAccount}
import models.common.Serializable
import models.common.Serializable.Vesting.VestingParameters
import play.api.Logger
import play.api.libs.json.{JsObject, Json, Reads}
import queries.Abstract.{Account, PublicKey}

object Accounts {

  private implicit val module: String = constants.Module.ACCOUNT_RESPONSE

  private implicit val logger: Logger = Logger(this.getClass)

  case class BaseAccount(address: String, pub_key: Option[PublicKey], account_number: String, sequence: String) extends Account {
    def toSerializableAccount: BlockchainAccount = BlockchainAccount(address = address, accountType = Option(constants.Blockchain.Account.BASE), accountNumber = account_number.toInt, sequence = sequence.toInt, vestingParameters = None, publicKey = this.pub_key.map(_.getBytes), publicKeyType = this.pub_key.map(_.getType))
  }

  implicit val baseAccountReads: Reads[BaseAccount] = Json.reads[BaseAccount]

  case class ModuleAccount(base_account: BaseAccount, name: String, permissions: Seq[String]) extends Account {
    val address: String = base_account.address

    def toSerializableAccount: BlockchainAccount = BlockchainAccount(address = address, accountType = Option(constants.Blockchain.Account.MODULE), accountNumber = base_account.account_number.toInt, sequence = base_account.sequence.toInt, vestingParameters = None, publicKey = this.base_account.pub_key.map(_.getBytes), publicKeyType = this.base_account.pub_key.map(_.getType))
  }

  implicit val moduleAccountReads: Reads[ModuleAccount] = Json.reads[ModuleAccount]

  case class BaseVestingAccount(base_account: BaseAccount, original_vesting: Seq[Coin], delegated_free: Seq[Coin], delegated_vesting: Seq[Coin], end_time: String)

  implicit val baseVestingAccountReads: Reads[BaseVestingAccount] = Json.reads[BaseVestingAccount]

  case class DelayedVestingAccount(base_vesting_account: BaseVestingAccount) extends Account {
    val address: String = base_vesting_account.base_account.address

    def toSerializableAccount: BlockchainAccount = BlockchainAccount(address = base_vesting_account.base_account.address, accountType = Option(constants.Blockchain.Account.DELAYED_VESTING), accountNumber = base_vesting_account.base_account.account_number.toInt, sequence = base_vesting_account.base_account.sequence.toInt, vestingParameters = Option(VestingParameters(originalVesting = base_vesting_account.original_vesting.map(_.toCoin), delegatedFree = base_vesting_account.delegated_free.map(_.toCoin), delegatedVesting = base_vesting_account.delegated_vesting.map(_.toCoin), endTime = base_vesting_account.end_time, startTime = None, vestingPeriods = Seq())), publicKey = this.base_vesting_account.base_account.pub_key.map(_.getBytes), publicKeyType = this.base_vesting_account.base_account.pub_key.map(_.getType))
  }

  implicit val delayedVestingAccountReads: Reads[DelayedVestingAccount] = Json.reads[DelayedVestingAccount]

  case class ContinuousVestingAccount(base_vesting_account: BaseVestingAccount, start_time: String) extends Account {
    val address: String = base_vesting_account.base_account.address

    def toSerializableAccount: BlockchainAccount = BlockchainAccount(address = base_vesting_account.base_account.address, accountType = Option(constants.Blockchain.Account.CONTINUOUS_VESTING), accountNumber = base_vesting_account.base_account.account_number.toInt, sequence = base_vesting_account.base_account.sequence.toInt, vestingParameters = Option(VestingParameters(originalVesting = base_vesting_account.original_vesting.map(_.toCoin), delegatedFree = base_vesting_account.delegated_free.map(_.toCoin), delegatedVesting = base_vesting_account.delegated_vesting.map(_.toCoin), endTime = base_vesting_account.end_time, startTime = Option(start_time), vestingPeriods = Seq())), publicKey = this.base_vesting_account.base_account.pub_key.map(_.getBytes), publicKeyType = this.base_vesting_account.base_account.pub_key.map(_.getType))
  }

  implicit val continuousVestingAccountReads: Reads[ContinuousVestingAccount] = Json.reads[ContinuousVestingAccount]

  case class VestingPeriod(length: String, amount: Seq[Coin]) {
    def toSerializableVestingPeriod: Serializable.Vesting.VestingPeriod = Serializable.Vesting.VestingPeriod(length = length, amount = amount.map(_.toCoin))
  }

  implicit val vestingPeriodReads: Reads[VestingPeriod] = Json.reads[VestingPeriod]

  case class PeriodicVestingAccount(base_vesting_account: BaseVestingAccount, start_time: String, vesting_periods: Seq[VestingPeriod]) extends Account {
    val address: String = base_vesting_account.base_account.address

    def toSerializableAccount: BlockchainAccount = BlockchainAccount(address = base_vesting_account.base_account.address, accountType = Option(constants.Blockchain.Account.CONTINUOUS_VESTING), accountNumber = base_vesting_account.base_account.account_number.toInt, sequence = base_vesting_account.base_account.sequence.toInt, vestingParameters = Option(VestingParameters(originalVesting = base_vesting_account.original_vesting.map(_.toCoin), delegatedFree = base_vesting_account.delegated_free.map(_.toCoin), delegatedVesting = base_vesting_account.delegated_vesting.map(_.toCoin), endTime = base_vesting_account.end_time, startTime = Option(start_time), vestingPeriods = vesting_periods.map(_.toSerializableVestingPeriod))), publicKey = this.base_vesting_account.base_account.pub_key.map(_.getBytes), publicKeyType = this.base_vesting_account.base_account.pub_key.map(_.getType))
  }

  implicit val periodicVestingAccountReads: Reads[PeriodicVestingAccount] = Json.reads[PeriodicVestingAccount]

  def accountApply(accountType: String, value: JsObject): Account = accountType match {
    case constants.Blockchain.Account.BASE => utilities.JSON.convertJsonStringToObject[BaseAccount](value.toString)
    case constants.Blockchain.Account.MODULE => utilities.JSON.convertJsonStringToObject[ModuleAccount](value.toString)
    case constants.Blockchain.Account.DELAYED_VESTING => utilities.JSON.convertJsonStringToObject[DelayedVestingAccount](value.toString)
    case constants.Blockchain.Account.CONTINUOUS_VESTING => utilities.JSON.convertJsonStringToObject[ContinuousVestingAccount](value.toString)
    case constants.Blockchain.Account.PERIODIC_VESTING => utilities.JSON.convertJsonStringToObject[PeriodicVestingAccount](value.toString)
    case _ => constants.Response.ACCOUNT_TYPE_NOT_FOUND.throwBaseException()
  }
}
