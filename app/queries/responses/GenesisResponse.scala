package queries.responses

import models.blockchain.{Redelegation => BlockchainRedelegation, Undelegation => BlockchainUndelegation, WithdrawAddress => BlockchainWithdrawAddress}
import models.common.Serializable
import play.api.libs.json.{Json, Reads}
import queries.responses.TransactionResponse.Msg
import queries.responses.common._
import transactions.Abstract.BaseResponse
import utilities.MicroNumber

object GenesisResponse {

  case class GenTxValue(msg: Seq[Msg])

  implicit val genTxValueReads: Reads[GenTxValue] = Json.reads[GenTxValue]

  case class GenTx(value: GenTxValue)

  implicit val genTxReads: Reads[GenTx] = Json.reads[GenTx]

  case class Genutil(gentxs: Option[Seq[GenTx]])

  implicit val genUtilReads: Reads[Genutil] = Json.reads[Genutil]

  case class Bank(send_enabled: Boolean)

  implicit val bankReads: Reads[Bank] = Json.reads[Bank]

  case class Auth(accounts: Seq[Account.Result])

  implicit val authReads: Reads[Auth] = Json.reads[Auth]

  case class RedelegationEntry(creation_height: String, completion_time: String, initial_balance: String, shares_dst: BigDecimal) {
    def toRedelegationEntry: Serializable.RedelegationEntry = Serializable.RedelegationEntry(creationHeight = creation_height.toInt, completionTime = completion_time, initialBalance = MicroNumber(BigInt(initial_balance)), sharesDestination = shares_dst)
  }

  implicit val redelegationEntryReads: Reads[RedelegationEntry] = Json.reads[RedelegationEntry]

  case class Redelegation(delegator_address: String, validator_src_address: String, validator_dst_address: String, entries: Seq[RedelegationEntry]) {
    def toRedelegation: BlockchainRedelegation = BlockchainRedelegation(delegatorAddress = delegator_address, validatorSourceAddress = validator_src_address, validatorDestinationAddress = validator_dst_address, entries = entries.map(_.toRedelegationEntry))
  }

  implicit val redelegationReads: Reads[Redelegation] = Json.reads[Redelegation]

  case class UndelegationEntry(creation_height: String, completion_time: String, initial_balance: String, balance: String) {
    def toUndelegationEntry: Serializable.UndelegationEntry = Serializable.UndelegationEntry(creationHeight = creation_height.toInt, completionTime = completion_time, initialBalance = MicroNumber(BigInt(initial_balance)), balance = MicroNumber(BigInt(balance)))
  }

  implicit val undelegationEntryReads: Reads[UndelegationEntry] = Json.reads[UndelegationEntry]

  case class Undelegation(delegator_address: String, validator_address: String, entries: Seq[UndelegationEntry]) {
    def toUndelegation: BlockchainUndelegation = BlockchainUndelegation(delegatorAddress = delegator_address, validatorAddress = validator_address, entries = entries.map(_.toUndelegationEntry))
  }

  implicit val undelegationReads: Reads[Undelegation] = Json.reads[Undelegation]

  case class Staking(delegations: Option[Seq[Delegation.Result]], redelegations: Option[Seq[Redelegation]], unbonding_delegations: Option[Seq[Undelegation]], validators: Option[Seq[Validator.Result]])

  implicit val stakingReads: Reads[Staking] = Json.reads[Staking]

  case class WithdrawAddress(delegator_address: String, withdraw_address: String) {
    def toWithdrawAddress: BlockchainWithdrawAddress = BlockchainWithdrawAddress(delegatorAddress = delegator_address, withdrawAddress = withdraw_address)
  }

  implicit val withdrawAddressReads: Reads[WithdrawAddress] = Json.reads[WithdrawAddress]

  case class Distribution(delegator_withdraw_infos: Option[Seq[WithdrawAddress]])

  implicit val distributionReads: Reads[Distribution] = Json.reads[Distribution]

  case class AppState(bank: Bank, genutil: Genutil, auth: Auth, distribution: Distribution, staking: Staking)

  implicit val appStateReads: Reads[AppState] = Json.reads[AppState]

  case class Genesis(app_state: AppState)

  implicit val genesisReads: Reads[Genesis] = Json.reads[Genesis]

  case class Result(genesis: Genesis)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
