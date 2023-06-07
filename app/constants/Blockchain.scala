package constants

import com.google.common.collect
import com.google.common.collect.ImmutableList
import org.bitcoinj.crypto.ChildNumber
import play.api.Configuration

object Blockchain {
  val AccountPrefix: String = AppConfig.configuration.get[String]("blockchain.account.prefix")
  val ValidatorPrefix: String = AccountPrefix + "valoper"
  val ValidatorConsensusPublicPrefix: String = AccountPrefix + "valconspub"
  val IBCDenoms: Seq[AppConfig.IBCDenom] = AppConfig.configuration.get[Seq[Configuration]]("blockchain.ibcDenoms.ibcDenomList").map { ibcDenoms =>
    constants.AppConfig.IBCDenom(hash = ibcDenoms.get[String]("hash"), name = ibcDenoms.get[String]("name"))
  }
  val ChainID: String = AppConfig.configuration.get[String]("blockchain.chainID")
  val StakingDenom: String = AppConfig.configuration.get[String]("blockchain.stakingDenom")
  val CoinType = 118
  val DefaultHDPath: ImmutableList[ChildNumber] = collect.ImmutableList.of(
    new ChildNumber(44, true),
    new ChildNumber(CoinType, true),
    new ChildNumber(0, true),
    new ChildNumber(0, false),
    new ChildNumber(0, false)
  )

  val RPCEndPoint: String = AppConfig.configuration.get[String]("blockchain.rpcURL")
  val RestEndPoint: String = AppConfig.configuration.get[String]("blockchain.restURL")

  object PublicKey {
    val MULTI_SIG = "/cosmos.crypto.multisig.LegacyAminoPubKey"
    val SINGLE_SECP256K1 = "/cosmos.crypto.secp256k1.PubKey"
    val SINGLE_SECP256R1 = "/cosmos.crypto.secp256r1.PubKey"
    val VALIDATOR = "/cosmos.crypto.ed25519.PubKey"
  }

  object Proposal {
    val PARAMETER_CHANGE = "/cosmos.params.v1beta1.ParameterChangeProposal"
    val TEXT = "/cosmos.gov.v1beta1.TextProposal"
    val COMMUNITY_POOL_SPEND = "/cosmos.distribution.v1beta1.CommunityPoolSpendProposal"
    val SOFTWARE_UPGRADE = "/cosmos.upgrade.v1beta1.SoftwareUpgradeProposal"
    val CANCEL_SOFTWARE_UPGRADE = "/cosmos.upgrade.v1beta1.CancelSoftwareUpgradeProposal"

    object Status {
      val UNSPECIFIED = "PROPOSAL_STATUS_UNSPECIFIED"
      val DEPOSIT_PERIOD = "PROPOSAL_STATUS_DEPOSIT_PERIOD"
      val VOTING_PERIOD = "PROPOSAL_STATUS_VOTING_PERIOD"
      val PASSED = "PROPOSAL_STATUS_PASSED"
      val REJECTED = "PROPOSAL_STATUS_REJECTED"
      val FAILED = "PROPOSAL_STATUS_FAILED"
    }

  }

  object ParameterType {
    val AUTH = "auth"
    val BANK = "bank"
    val CRISIS = "crisis"
    val DISTRIBUTION = "distribution"
    val GOVERNANCE = "gov"
    val IBC = "ibc"
    val MINT = "mint"
    val SLASHING = "slashing"
    val STAKING = "staking"
    val TRANSFER = "transfer"
    val CLASSIFICATIONS = "classifications"
    val ASSETS = "assets"
    val IDENTITIES = "identities"
    val MAINTAINERS = "maintainers"
    val METAS = "metas"
    val ORDERS = "orders"
    val SPLITS = "splits"
  }

  object Account {
    val BASE = "/cosmos.auth.v1beta1.BaseAccount"
    val CONTINUOUS_VESTING = "/cosmos.vesting.v1beta1.ContinuousVestingAccount"
    val DELAYED_VESTING = "/cosmos.vesting.v1beta1.DelayedVestingAccount"
    val MODULE = "/cosmos.auth.v1beta1.ModuleAccount"
    val PERIODIC_VESTING = "/cosmos.vesting.v1beta1.PeriodicVestingAccount"
  }

  object ValidatorStatus {
    val BONDED = "BOND_STATUS_BONDED"
    val UNBONDED = "BOND_STATUS_UNBONDED"
    val UNBONDING = "BOND_STATUS_UNBONDING"
  }

  object Event {
    //bank
    val Transfer = "transfer"
    //crisis
    val Invariant = "invariant"
    //distribution
    val SetWithdrawAddress = "set_withdraw_address"
    val Rewards = "rewards"
    val Commission = "commission"
    val WithdrawRewards = "withdraw_rewards"
    val WithdrawCommission = "withdraw_commission"
    val ProposerReward = "proposer_reward"
    //evidence
    val SubmitEvidence = "submit_evidence"
    //governance
    val SubmitProposal = "submit_proposal"
    val ProposalDeposit = "proposal_deposit"
    val ProposalVote = "proposal_vote"
    val InactiveProposal = "inactive_proposal"
    val ActiveProposal = "active_proposal"
    //mint
    val Mint = "mint"
    //slashing
    val Slash = "slash"
    val Liveness = "liveness"
    //staking
    val CompleteUnbonding = "complete_unbonding"
    val CompleteRedelegation = "complete_redelegation"
    val CreateValidator = "create_validator"
    val EditValidator = "edit_validator"
    val Delegate = "delegate"
    val Unbond = "unbond"
    val Redelegate = "redelegate"

    object Attribute {
      //bank
      val Recipient = "recipient"
      val Sender = "sender"
      //crisis
      val Route = "route"
      //distribution
      val WithdrawAddress = "withdraw_address"
      val Amount = "amount"
      //evidence
      val EvidenceHash = "evidence_hash"
      //governance
      val ProposalResult = "proposal_result"
      val Option = "option"
      val ProposalID = "proposal_id"
      val VotingPeriodStart = "voting_period_start"
      val ProposalDropped = "proposal_dropped"
      val ProposalPassed = "proposal_passed"
      val ProposalRejected = "proposal_rejected"
      val ProposalFailed = "proposal_failed"
      val ProposalType = "proposal_type"
      //mint
      val BondedRatio = "bonded_ratio"
      val Inflation = "inflation"
      val AnnualProvisions = "annual_provisions"
      //slashing
      val Address = "address"
      val Height = "height"
      val Power = "power"
      val Reason = "reason"
      val Jailed = "jailed"
      val MissedBlocks = "missed_blocks"
      val DoubleSign = "double_sign"
      val MissingSignature = "missing_signature"
      //staking
      val Validator = "validator"
      val CommissionRate = "commission_rate"
      val MinSelfDelegation = "min_self_delegation"
      val SrcValidator = "source_validator"
      val DstValidator = "destination_validator"
      val Delegator = "delegator"
      val CompletionTime = "completion_time"
    }

  }

  object Tendermint {
    val DuplicateVoteEvidence = "tendermint/DuplicateVoteEvidence"
    val LightClientAttackEvidence = "tendermint/LightClientAttackEvidence"
  }

  object Authz {
    val SEND_AUTHORIZATION = "/cosmos.bank.v1beta1.SendAuthorization"
    val GENERIC_AUTHORIZATION = "/cosmos.authz.v1beta1.GenericAuthorization"
    val STAKE_AUTHORIZATION = "/cosmos.staking.v1beta1.StakeAuthorization"

    object StakeAuthorization {
      val AUTHORIZATION_TYPE_DELEGATE = "AUTHORIZATION_TYPE_DELEGATE"
      val AUTHORIZATION_TYPE_UNDELEGATE = "AUTHORIZATION_TYPE_UNDELEGATE"
      val AUTHORIZATION_TYPE_REDELEGATE = "AUTHORIZATION_TYPE_REDELEGATE"
    }
  }

  object FeeGrant {
    val BASIC_ALLOWANCE = "/cosmos.feegrant.v1beta1.BasicAllowance"
    val PERIODIC_ALLOWANCE = "/cosmos.feegrant.v1beta1.PeriodicAllowance"
    val ALLOWED_MSG_ALLOWANCE = "/cosmos.feegrant.v1beta1.AllowedMsgAllowance"
  }

}