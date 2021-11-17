package queries.responses.blockchain

import models.Abstract.Parameter
import models.blockchain.{Redelegation => BlockchainRedelegation, Undelegation => BlockchainUndelegation, WithdrawAddress => BlockchainWithdrawAddress}
import models.common.Parameters.GovernanceParameter
import models.common.Serializable
import play.api.libs.json.{Json, Reads}
import queries.Abstract.Account
import queries.responses.blockchain.TransactionResponse._
import queries.responses.blockchain.params._
import queries.responses.common.{Coin, Delegation, Validator}
import transactions.Abstract.BaseResponse
import utilities.MicroNumber

object GenesisResponse {

  import queries.responses.common.Accounts.accountReads

  case class GenTxValue(msg: Seq[Msg], fee: Fee, memo: String)

  implicit val genTxValueReads: Reads[GenTxValue] = Json.reads[GenTxValue]

  case class GenTx(value: GenTxValue) {
    def getSigners: Seq[String] = {
      var seen: Map[String, Boolean] = Map()
      var signers: Seq[String] = Seq()
      value.msg.foreach(message => message.toStdMsg.getSigners.foreach(signer => {
        if (!seen.getOrElse(signer, false)) {
          signers = signers :+ signer
          seen = seen + (signer -> true)
        }
      }))
      signers
    }

    def getFeePayer: String = {
      val signers = getSigners
      if (signers.nonEmpty) signers.head else ""
    }
  }

  implicit val genTxReads: Reads[GenTx] = Json.reads[GenTx]

  case class GenUtil(gentxs: Seq[GenTx])

  implicit val genUtilReads: Reads[GenUtil] = Json.reads[GenUtil]

  object Auth {

    case class Module(accounts: Seq[Account], params: AuthResponse.Params)

    implicit val authModuleReads: Reads[Module] = Json.reads[Module]
  }

  object Bank {

    case class Module(send_enabled: Boolean)

    implicit val bankModuleReads: Reads[Module] = Json.reads[Module]
  }

  object Distribution {

    case class WithdrawAddress(delegator_address: String, withdraw_address: String) {
      def toWithdrawAddress: BlockchainWithdrawAddress = BlockchainWithdrawAddress(delegatorAddress = delegator_address, withdrawAddress = withdraw_address)
    }

    implicit val withdrawAddressReads: Reads[WithdrawAddress] = Json.reads[WithdrawAddress]

    case class Module(params: DistributionResponse.Params, delegator_withdraw_infos: Seq[WithdrawAddress])

    implicit val distributionReads: Reads[Module] = Json.reads[Module]
  }

  object Halving {

    case class Module(params: HalvingResponse.Params)

    implicit val moduleReads: Reads[Module] = Json.reads[Module]
  }

  object Mint {

    case class Minter(inflation: String, annual_provisions: String)

    implicit val minterReads: Reads[Minter] = Json.reads[Minter]

    case class Module(minter: Minter, params: MintResponse.Params)

    implicit val mintReads: Reads[Module] = Json.reads[Module]
  }

  object Slashing {

    case class Module(params: SlashingResponse.Params)

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

    case class Module(params: StakingResponse.Params, delegations: Option[Seq[Delegation]], redelegations: Option[Seq[Redelegation]], unbonding_delegations: Option[Seq[Undelegation]], validators: Option[Seq[Validator.Result]])

    implicit val stakingReads: Reads[Module] = Json.reads[Module]

  }

  object Gov {

    case class DepositParams(min_deposit: Seq[Coin], max_deposit_period: String)

    implicit val depositParamsReads: Reads[DepositParams] = Json.reads[DepositParams]

    case class VotingParams(voting_period: String)

    implicit val votingParamsReads: Reads[VotingParams] = Json.reads[VotingParams]

    case class TallyParams(quorum: String, threshold: String, veto: String)

    implicit val tallyParamsReads: Reads[TallyParams] = Json.reads[TallyParams]

    case class Module(deposit_params: DepositParams, voting_params: VotingParams, tally_params: TallyParams, proposals: Option[Seq[ProposalResponse.Proposal]], deposits: Option[Seq[ProposalDepositResponse.Deposit]], votes: Option[Seq[ProposalVoteResponse.Vote]]) {
      def toParameter: Parameter = GovernanceParameter(minDeposit = deposit_params.min_deposit.map(_.toCoin), maxDepositPeriod = deposit_params.max_deposit_period.split("s")(0).toLong, votingPeriod = voting_params.voting_period.split("s")(0).toLong, quorum = BigDecimal(tally_params.quorum), threshold = BigDecimal(tally_params.threshold), vetoThreshold = BigDecimal(tally_params.veto))
    }

    implicit val govReads: Reads[Module] = Json.reads[Module]
  }

  case class AppState(auth: Auth.Module, bank: Bank.Module, distribution: Distribution.Module, genutil: GenUtil, gov: Gov.Module, mint: Mint.Module, slashing: Slashing.Module, staking: Staking.Module)

  implicit val appStateReads: Reads[AppState] = Json.reads[AppState]

  case class Genesis(app_state: AppState, genesis_time: String, chain_id: String)

  implicit val genesisReads: Reads[Genesis] = Json.reads[Genesis]

  case class Result(genesis: Genesis)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
