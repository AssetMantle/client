package queries.responses.blockchain

import models.blockchain.{Redelegation => BlockchainRedelegation, Undelegation => BlockchainUndelegation, WithdrawAddress => BlockchainWithdrawAddress}
import models.common.Serializable
import play.api.libs.json.{Json, Reads}
import queries.Abstract.Account
import queries.responses.blockchain.TransactionResponse.Msg
import queries.responses.common.{Coin, Delegation, Validator}
import transactions.Abstract.BaseResponse
import utilities.MicroNumber
import queries.responses.common.Account._

object GenesisResponse {

  case class GenTxValue(msg: Seq[Msg])

  implicit val genTxValueReads: Reads[GenTxValue] = Json.reads[GenTxValue]

  case class GenTx(value: GenTxValue)

  implicit val genTxReads: Reads[GenTx] = Json.reads[GenTx]

  case class Genutil(gentxs: Option[Seq[GenTx]])

  implicit val genUtilReads: Reads[Genutil] = Json.reads[Genutil]

  object Bank {

    case class SendEnabled(denom: String, enabled: Boolean)

    implicit val sendEnabledReads: Reads[SendEnabled] = Json.reads[SendEnabled]

    case class BankParams(default_send_enabled: Boolean, send_enabled: Seq[SendEnabled])

    implicit val bankParamsReads: Reads[BankParams] = Json.reads[BankParams]

    case class BankBalance(address: String, coins: Seq[Coin])

    implicit val bankBalanceReads: Reads[BankBalance] = Json.reads[BankBalance]

    case class DenomUnits(denom: Option[String], exponent: Option[Int], aliases: Seq[String])

    implicit val denomUnitsReads: Reads[DenomUnits] = Json.reads[DenomUnits]

    case class Module(params: BankParams, balances: Seq[BankBalance], supply: Seq[Coin])

    implicit val bankModuleReads: Reads[Module] = Json.reads[Module]
  }

  object Auth {

    case class Params(max_memo_characters: String, sig_verify_cost_ed25519: String, sig_verify_cost_secp256k1: String, tx_sig_limit: String, tx_size_cost_per_byte: String)

    implicit val authParamsReads: Reads[Params] = Json.reads[Params]

    case class Module(accounts: Seq[Account], params: Params)

    implicit val authModuleReads: Reads[Module] = Json.reads[Module]
  }

  object Distribution {

    case class WithdrawAddress(delegator_address: String, withdraw_address: String) {
      def toWithdrawAddress: BlockchainWithdrawAddress = BlockchainWithdrawAddress(delegatorAddress = delegator_address, withdrawAddress = withdraw_address)
    }

    implicit val withdrawAddressReads: Reads[WithdrawAddress] = Json.reads[WithdrawAddress]

    case class Params(community_tax: String, base_proposer_reward: String, bonus_proposer_reward: String, withdraw_addr_enabled: Boolean)

    implicit val distributionParamsReads: Reads[Params] = Json.reads[Params]

    case class Module(params: Params, delegator_withdraw_infos: Seq[WithdrawAddress])

    implicit val distributionReads: Reads[Module] = Json.reads[Module]
  }

  object Halving {

    case class Params(blockHeight: String)

    implicit val halvingParamsReads: Reads[Params] = Json.reads[Params]

    case class Module(params: Params)

    implicit val moduleReads: Reads[Module] = Json.reads[Module]
  }

  object Mint {

    case class Minter(inflation: String, annual_provisions: String)

    implicit val minterReads: Reads[Minter] = Json.reads[Minter]

    case class MintParams(mint_denom: String, inflation_rate_change: String, inflation_min: String, inflation_max: String, goal_bonded: String, blocks_per_year: String)

    implicit val mintParamsReads: Reads[MintParams] = Json.reads[MintParams]

    case class Module(minter: Minter, params: MintParams)

    implicit val mintReads: Reads[Module] = Json.reads[Module]
  }

  object Slashing {

    case class SlashingParams(signed_blocks_window: String, min_signed_per_window: String, downtime_jail_duration: String, slash_fraction_double_sign: String, slash_fraction_downtime: String)

    implicit val slashingParamsReads: Reads[SlashingParams] = Json.reads[SlashingParams]

    case class Module(params: SlashingParams)

    implicit val slashingReads: Reads[Module] = Json.reads[Module]

  }

  object Staking {

    case class RedelegationEntry(creation_height: String, completion_time: String, initial_balance: String, shares_dst: String) {
      def toRedelegationEntry: Serializable.RedelegationEntry = Serializable.RedelegationEntry(creationHeight = creation_height.toInt, completionTime = completion_time, initialBalance = MicroNumber(BigInt(initial_balance)), sharesDestination = BigDecimal(shares_dst))
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

    case class Module(params: StakingParams, delegations: Seq[Delegation.Result], redelegations: Seq[Redelegation], unbonding_delegations: Seq[Undelegation], validators: Seq[Validator.Result])

    implicit val stakingReads: Reads[Module] = Json.reads[Module]

  }

  object Gov {

    case class DepositParams(min_deposit: Seq[Coin], max_deposit_period: String)

    implicit val depositParamsReads: Reads[DepositParams] = Json.reads[DepositParams]

    case class VotingParams(voting_period: String)

    implicit val votingParamsReads: Reads[VotingParams] = Json.reads[VotingParams]

    case class TallyParams(quorum: String, threshold: String, veto_threshold: String)

    implicit val tallyParamsReads: Reads[TallyParams] = Json.reads[TallyParams]

    case class Module(deposit_params: DepositParams, voting_params: VotingParams, tally_params: TallyParams)

    implicit val govReads: Reads[Module] = Json.reads[Module]
  }

  case class AppState(auth: Auth.Module, bank: Bank.Module, distribution: Distribution.Module, genutil: Genutil, gov: Gov.Module, halving: Halving.Module, mint: Mint.Module, slashing: Slashing.Module, staking: Staking.Module)

  implicit val appStateReads: Reads[AppState] = Json.reads[AppState]

  case class Genesis(app_state: AppState)

  implicit val genesisReads: Reads[Genesis] = Json.reads[Genesis]

  case class Result(genesis: Genesis)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
