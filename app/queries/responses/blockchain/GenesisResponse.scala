package queries.responses.blockchain

import models.Abstract.Parameter
import models.blockchain.{Redelegation => BlockchainRedelegation, Undelegation => BlockchainUndelegation, WithdrawAddress => BlockchainWithdrawAddress}
import models.common.Parameters._
import models.common.Serializable
import play.api.libs.json.{Json, Reads}
import queries.Abstract.Account
import queries.responses.blockchain.common.Data._
import queries.responses.blockchain.common.{Document, ParameterList, Split}
import queries.responses.blockchain.params._
import queries.responses.common.{Coin, Delegation, Validator, Authz => commonAuthz, FeeGrant => commonFeeGrant}
import schema.data.{base => baseSchemaData}
import transactions.Abstract.BaseResponse
import utilities.Date.RFC3339
import utilities.MicroNumber

object GenesisResponse {

  case class MsgCreateValidator(delegator_address: String, validator_address: String)

  implicit val msgCreateValidatorReads: Reads[MsgCreateValidator] = Json.reads[MsgCreateValidator]

  case class GenTxBody(messages: Seq[MsgCreateValidator])

  implicit val genTxBodyReads: Reads[GenTxBody] = Json.reads[GenTxBody]

  case class GenTx(body: GenTxBody)

  implicit val genTxReads: Reads[GenTx] = Json.reads[GenTx]

  case class GenUtil(gen_txs: Seq[GenTx])

  implicit val genUtilReads: Reads[GenUtil] = Json.reads[GenUtil]

  object Asset {
    case class Mappable(asset: Document)

    implicit val metaMappableReads: Reads[Mappable] = Json.reads[Mappable]

    case class Module(mappables: Seq[Mappable], parameterList: ParameterList) {
      def getParameter: AssetParameter = AssetParameter(
        burnEnabled = baseSchemaData.BooleanData(this.parameterList.getMetaProperty("burnEnabled").getData.getProtoBytes).value,
        mintEnabled = baseSchemaData.BooleanData(this.parameterList.getMetaProperty("mintEnabled").getData.getProtoBytes).value,
        renumerateEnabled = baseSchemaData.BooleanData(this.parameterList.getMetaProperty("renumerateEnabled").getData.getProtoBytes).value)
    }

    implicit val metaReads: Reads[Module] = Json.reads[Module]
  }

  object Auth {

    case class Module(accounts: Seq[Account], params: AuthResponse.Params)

    implicit val authModuleReads: Reads[Module] = Json.reads[Module]
  }

  object Authz {

    case class Authorization(granter: String, grantee: String, expiration: RFC3339, authorization: commonAuthz.Authorization)

    implicit val authorizationReads: Reads[Authorization] = Json.reads[Authorization]

    case class Module(authorization: Seq[Authorization])

    implicit val authzModuleReads: Reads[Module] = Json.reads[Module]
  }

  object Bank {

    case class BankBalance(address: String, coins: Seq[Coin])

    implicit val bankBalanceReads: Reads[BankBalance] = Json.reads[BankBalance]

    case class DenomUnits(denom: Option[String], exponent: Option[Int], aliases: Seq[String])

    implicit val denomUnitsReads: Reads[DenomUnits] = Json.reads[DenomUnits]

    case class Module(params: BankResponse.Params, balances: Seq[BankBalance], supply: Seq[Coin])

    implicit val bankModuleReads: Reads[Module] = Json.reads[Module]
  }

  object Classification {
    case class Mappable(classification: Document)

    implicit val metaMappableReads: Reads[Mappable] = Json.reads[Mappable]

    case class Module(mappables: Seq[Mappable], parameterList: ParameterList) {
      def getParameter: ClassificationParameter = ClassificationParameter(
        bondRate = baseSchemaData.NumberData(this.parameterList.getMetaProperty("bondRate").getData.getProtoBytes).value,
        maxPropertyCount = baseSchemaData.NumberData(this.parameterList.getMetaProperty("maxPropertyCount").getData.getProtoBytes).value)
    }

    implicit val metaReads: Reads[Module] = Json.reads[Module]
  }

  object Distribution {

    case class WithdrawAddress(delegator_address: String, withdraw_address: String) {
      def toWithdrawAddress: BlockchainWithdrawAddress = BlockchainWithdrawAddress(delegatorAddress = delegator_address, withdrawAddress = withdraw_address)
    }

    implicit val withdrawAddressReads: Reads[WithdrawAddress] = Json.reads[WithdrawAddress]

    case class Module(params: DistributionResponse.Params, delegator_withdraw_infos: Seq[WithdrawAddress])

    implicit val distributionReads: Reads[Module] = Json.reads[Module]
  }

  object FeeGrant {

    case class Allowance(granter: String, grantee: String, allowance: commonFeeGrant.Allowance)

    implicit val allowanceReads: Reads[Allowance] = Json.reads[Allowance]

    case class Module(allowances: Seq[Allowance])

    implicit val feeGrantReads: Reads[Module] = Json.reads[Module]
  }

  object Identity {
    case class Mappable(identity: Document)

    implicit val metaMappableReads: Reads[Mappable] = Json.reads[Mappable]

    case class Module(mappables: Seq[Mappable], parameterList: ParameterList) {
      def getParameter: IdentityParameter = IdentityParameter(
        maxProvisionAddressCount = baseSchemaData.NumberData(this.parameterList.getMetaProperty("maxProvisionAddressCount").getData.getProtoBytes).value)
    }

    implicit val metaReads: Reads[Module] = Json.reads[Module]
  }

  object Maintainer {
    case class Mappable(maintainer: Document)

    implicit val metaMappableReads: Reads[Mappable] = Json.reads[Mappable]

    case class Module(mappables: Seq[Mappable], parameterList: ParameterList) {
      def getParameter: MaintainerParameter = MaintainerParameter(
        deputizeAllowed = baseSchemaData.BooleanData(this.parameterList.getMetaProperty("deputizeAllowed").getData.getProtoBytes).value)
    }

    implicit val metaReads: Reads[Module] = Json.reads[Module]
  }


  object Meta {

    case class Mappable(data: AnyData)

    implicit val metaMappableReads: Reads[Mappable] = Json.reads[Mappable]

    case class Module(mappables: Seq[Mappable], parameterList: ParameterList) {
      def getParameter: MetaParameter = MetaParameter(
        revealEnabled = baseSchemaData.BooleanData(this.parameterList.getMetaProperty("revealEnabled").getData.getProtoBytes).value)
    }


    implicit val metaReads: Reads[Module] = Json.reads[Module]

  }

  object Mint {

    case class Minter(inflation: String, annual_provisions: String)

    implicit val minterReads: Reads[Minter] = Json.reads[Minter]

    case class Module(minter: Minter, params: MintResponse.Params)

    implicit val mintReads: Reads[Module] = Json.reads[Module]
  }

  object Order {
    case class Mappable(order: Document)

    implicit val metaMappableReads: Reads[Mappable] = Json.reads[Mappable]

    case class Module(mappables: Seq[Mappable], parameterList: ParameterList) {
      def getParameter: OrderParameter = OrderParameter(
        maxOrderLife = baseSchemaData.HeightData(this.parameterList.getMetaProperty("maxOrderLife").getData.getProtoBytes).value.value)
    }

    implicit val metaReads: Reads[Module] = Json.reads[Module]
  }

  object Slashing {

    case class Module(params: SlashingResponse.Params)

    implicit val slashingReads: Reads[Module] = Json.reads[Module]

  }

  object Split {
    case class Mappable(split: Split)

    implicit val metaMappableReads: Reads[Mappable] = Json.reads[Mappable]

    case class Module(mappables: Seq[Mappable], parameterList: ParameterList) {
      def getParameter: SplitParameter = SplitParameter(
        wrapAllowedCoins = Seq("umntl")) //TODO: baseSchemaData.ListData(this.parameterList.getMetaProperty("wrapAllowedCoins").getData.getProtoBytes).getAbstractDataList.map(x => (baseSchemaData.IDData(x.getProtoBytes).getProtoBytes)))
    }

    implicit val metaReads: Reads[Module] = Json.reads[Module]
  }

  object Staking {

    case class RedelegationEntry(creation_height: String, completion_time: RFC3339, initial_balance: String, shares_dst: String) {
      def toRedelegationEntry: Serializable.RedelegationEntry = Serializable.RedelegationEntry(creationHeight = creation_height.toInt, completionTime = completion_time, initialBalance = MicroNumber(BigInt(initial_balance)), sharesDestination = BigDecimal(shares_dst))
    }

    implicit val redelegationEntryReads: Reads[RedelegationEntry] = Json.reads[RedelegationEntry]

    case class Redelegation(delegator_address: String, validator_src_address: String, validator_dst_address: String, entries: Seq[RedelegationEntry]) {
      def toRedelegation: BlockchainRedelegation = BlockchainRedelegation(delegatorAddress = delegator_address, validatorSourceAddress = validator_src_address, validatorDestinationAddress = validator_dst_address, entries = entries.map(_.toRedelegationEntry))
    }

    implicit val redelegationReads: Reads[Redelegation] = Json.reads[Redelegation]

    case class UndelegationEntry(creation_height: String, completion_time: RFC3339, initial_balance: String, balance: String) {
      def toUndelegationEntry: Serializable.UndelegationEntry = Serializable.UndelegationEntry(creationHeight = creation_height.toInt, completionTime = completion_time, initialBalance = MicroNumber(BigInt(initial_balance)), balance = MicroNumber(BigInt(balance)))
    }

    implicit val undelegationEntryReads: Reads[UndelegationEntry] = Json.reads[UndelegationEntry]

    case class Undelegation(delegator_address: String, validator_address: String, entries: Seq[UndelegationEntry]) {
      def toUndelegation: BlockchainUndelegation = BlockchainUndelegation(delegatorAddress = delegator_address, validatorAddress = validator_address, entries = entries.map(_.toUndelegationEntry))
    }

    implicit val undelegationReads: Reads[Undelegation] = Json.reads[Undelegation]

    case class Module(params: StakingResponse.Params, delegations: Seq[Delegation], redelegations: Seq[Redelegation], unbonding_delegations: Seq[Undelegation], validators: Seq[Validator.Result])

    implicit val stakingReads: Reads[Module] = Json.reads[Module]

  }

  object Gov {

    case class DepositParams(min_deposit: Seq[Coin], max_deposit_period: String)

    implicit val depositParamsReads: Reads[DepositParams] = Json.reads[DepositParams]

    case class VotingParams(voting_period: String)

    implicit val votingParamsReads: Reads[VotingParams] = Json.reads[VotingParams]

    case class TallyParams(quorum: String, threshold: String, veto_threshold: String)

    implicit val tallyParamsReads: Reads[TallyParams] = Json.reads[TallyParams]

    case class Module(deposit_params: DepositParams, voting_params: VotingParams, tally_params: TallyParams, proposals: Seq[ProposalResponse.Proposal], deposits: Seq[ProposalDepositResponse.Deposit], votes: Seq[ProposalVoteResponse.Vote]) {
      def toParameter: Parameter = GovernanceParameter(minDeposit = deposit_params.min_deposit.map(_.toCoin), maxDepositPeriod = deposit_params.max_deposit_period.split("s")(0).toLong, votingPeriod = voting_params.voting_period.split("s")(0).toLong, quorum = BigDecimal(tally_params.quorum), threshold = BigDecimal(tally_params.threshold), vetoThreshold = BigDecimal(tally_params.veto_threshold))
    }

    implicit val govReads: Reads[Module] = Json.reads[Module]
  }

  case class AppState(assets: Option[Asset.Module], auth: Auth.Module, authz: Authz.Module, bank: Bank.Module, distribution: Distribution.Module, classifications: Option[Classification.Module], feegrant: FeeGrant.Module, genutil: GenUtil, gov: Gov.Module, identities: Option[Identity.Module], maintainers: Option[Maintainer.Module], metas: Option[Meta.Module], mint: Mint.Module, orders: Option[Order.Module], slashing: Slashing.Module, splits: Option[Split.Module], staking: Staking.Module)

  implicit val appStateReads: Reads[AppState] = Json.reads[AppState]

  case class Genesis(app_state: AppState, genesis_time: RFC3339, chain_id: String, initial_height: String)

  implicit val genesisReads: Reads[Genesis] = Json.reads[Genesis]

  case class Result(genesis: Genesis)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
