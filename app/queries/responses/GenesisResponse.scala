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

  case class StakingParams(bond_denom: String, historical_entries: Int, max_entries: Int, max_validators: Int, unbonding_time: String)

  implicit val stakingParamsReads: Reads[StakingParams] = Json.reads[StakingParams]

  case class Staking(params: StakingParams, delegations: Option[Seq[Delegation.Result]], redelegations: Option[Seq[Redelegation]], unbonding_delegations: Option[Seq[Undelegation]], validators: Option[Seq[Validator.Result]])

  implicit val stakingReads: Reads[Staking] = Json.reads[Staking]

  case class WithdrawAddress(delegator_address: String, withdraw_address: String) {
    def toWithdrawAddress: BlockchainWithdrawAddress = BlockchainWithdrawAddress(delegatorAddress = delegator_address, withdrawAddress = withdraw_address)
  }

  implicit val withdrawAddressReads: Reads[WithdrawAddress] = Json.reads[WithdrawAddress]

  case class DistributionParams(community_tax: BigDecimal,base_proposer_reward: BigDecimal, bonus_proposer_reward: BigDecimal, withdraw_addr_enabled: Boolean)

  implicit val distributionParamsReads: Reads[DistributionParams] = Json.reads[DistributionParams]

  case class Distribution(params: DistributionParams, delegator_withdraw_infos: Option[Seq[WithdrawAddress]])

  implicit val distributionReads: Reads[Distribution] = Json.reads[Distribution]

  case class Minter(inflation: BigDecimal, annual_provisions: BigDecimal)

  implicit val minterReads: Reads[Minter] = Json.reads[Minter]

  case class MintParams(mint_denom: String, inflation_rate_change: BigDecimal, inflation_min: BigDecimal, inflation_max: BigDecimal, goal_bonded: BigDecimal, blocks_per_year: String)

  implicit val mintParamsReads: Reads[MintParams] = Json.reads[MintParams]

  case class Mint(minter: Minter, params: MintParams)

  implicit val mintReads: Reads[Mint] = Json.reads[Mint]

  case class SlashingParams(signed_blocks_window: String, min_signed_per_window: BigDecimal, downtime_jail_duration: String, slash_fraction_double_sign: BigDecimal, slash_fraction_downtime: BigDecimal)

  implicit val slashingParamsReads: Reads[SlashingParams] = Json.reads[SlashingParams]

  case class Slashing(params: SlashingParams)

  implicit val slashingReads: Reads[Slashing] = Json.reads[Slashing]

  case class DepositParams(min_deposit: Seq[Coin], max_deposit_period: String)

  implicit val depositParamsReads: Reads[DepositParams] = Json.reads[DepositParams]

  case class VotingParams(voting_period: String)

  implicit val votingParamsReads: Reads[VotingParams] = Json.reads[VotingParams]

  case class TallyParams(quorum: BigDecimal, threshold: BigDecimal, veto: BigDecimal)

  implicit val tallyParamsReads: Reads[TallyParams] = Json.reads[TallyParams]

  case class Gov(deposit_params: DepositParams, voting_params: VotingParams, tally_params: TallyParams)

  implicit val govReads: Reads[Gov] = Json.reads[Gov]

  case class AppState(bank: Bank, genutil: Genutil, auth: Auth, distribution: Distribution, staking: Staking, mint: Mint, slashing: Slashing, gov: Gov)

  implicit val appStateReads: Reads[AppState] = Json.reads[AppState]

  case class Genesis(app_state: AppState)

  implicit val genesisReads: Reads[Genesis] = Json.reads[Genesis]

  case class Result(genesis: Genesis)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
