package constants

import com.google.common.collect
import com.google.common.collect.ImmutableList
import org.bitcoinj.crypto.ChildNumber

object Blockchain {
  val MnemonicShown = 3
  val FullFundraiserPath = "44'/118'/0'/0/0"
  val AccountPrefix = "persistence"
  val ValidatorPrefix = "persistencevaloper"
  val ValidatorConsensusPublicPrefix = "persistencevalconspub"
  val NegotiationDefaultTime = 5000000
  val DefaultFaucetTokenAmount = 1
  val IDSeparator = "."
  val FirstOrderCompositeIDSeparator = "|"
  val SecondOrderCompositeIDSeparator = "*"
  val OneDec = BigDecimal("1.000000000000000000")
  val ZeroDec = BigDecimal("0.0")
  val SmallestDec = BigDecimal("0.000000000000000001")
  val ToHashSeparator = "_"
  val RequestPropertiesSeparator = ","
  val DataNameAndTypeSeparator = ":"
  val DataTypeAndValueSeparator = "|"
  val MaxTraits = 22
  val HeightDataDefaultValue: Int = -1
  val CoinType = 750
  val DefaultHDPath: ImmutableList[ChildNumber] = collect.ImmutableList.of(
    new ChildNumber(44, true),
    new ChildNumber(CoinType, true),
    new ChildNumber(0, true),
    new ChildNumber(0, false),
    new ChildNumber(0, false)
  )

  val TokenTickers: Map[String, String] = Map("uxprt" -> "XPRT")

  object PublicKey {
    val MULTI_SIG = "/cosmos.crypto.multisig.LegacyAminoPubKey"
    val SINGLE = "/cosmos.crypto.secp256k1.PubKey"
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
    val HALVING = "halving"
    val IBC = "ibc"
    val MINT = "mint"
    val SLASHING = "slashing"
    val STAKING = "staking"
    val TRANSFER = "transfer"
  }

  object Account {
    val BASE = "/cosmos.auth.v1beta1.BaseAccount"
    val CONTINUOUS_VESTING = "/cosmos.vesting.v1beta1.ContinuousVestingAccount"
    val DELAYED_VESTING = "/cosmos.vesting.v1beta1.DelayedVestingAccount"
    val MODULE = "/cosmos.auth.v1beta1.ModuleAccount"
    val PERIODIC_VESTING = "/cosmos.vesting.v1beta1.PeriodicVestingAccount"
  }

  object ValidatorStatus {
    val BONED = "BOND_STATUS_BONDED"
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
    val Amount = "amount"
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

  object Properties {
    val Burn = "burn"
    val Creation = "creation"
    val Expiry = "expiry"
    val Lock = "lock"
    val MakerSplit = "makerSplit"
    val MakerOwnableSplit = "makerOwnableSplit"
    val TakerID = "takerID"
    val ExchangeRate = "exchangeRate"
    val NubID = "nubID"
  }

  object Modules {
    val Exchanges = "exchanges"
    val Orders = "orders"
  }

  object DataType {
    val STRING_DATA = "xprt/stringData"
    val HEIGHT_DATA = "xprt/heightData"
    val ID_DATA = "xprt/idData"
    val DEC_DATA = "xprt/decData"
  }

  object FactType {
    val STRING = "S"
    val HEIGHT = "H"
    val ID = "I"
    val DEC = "D"
  }

  object Entity {
    val IDENTITY_DEFINITION = "IDENTITY_DEFINITION"
    val IDENTITY = "IDENTITY"
    val ASSET_DEFINITION = "ASSET_DEFINITION"
    val ASSET = "ASSET"
    val ORDER_DEFINITION = "ORDER_DEFINITION"
    val ORDER = "ORDER"
    val WRAPPED_COIN = "WRAPPED_COIN"
    val MAINTAINER = "MAINTAINER"
  }

  object TransactionRequest {
    //identity
    val IDENTITY_NUB = "/xprt/identities/nub/request"
    val IDENTITY_DEFINE = "/xprt/identities/define/request"
    val IDENTITY_ISSUE = "/xprt/identities/issue/request"
    val IDENTITY_PROVISION = "/xprt/identities/provision/request"
    val IDENTITY_UNPROVISION = "/xprt/identities/unprovision/request"
    //asset
    val ASSET_DEFINE = "/xprt/assets/define/request"
    val ASSET_MINT = "/xprt/assets/mint/request"
    val ASSET_MUTATE = "/xprt/assets/mutate/request"
    val ASSET_BURN = "/xprt/assets/burn/request"
    //split
    val SPLIT_SEND = "/xprt/splits/send/request"
    val SPLIT_WRAP = "/xprt/splits/wrap/request"
    val SPLIT_UNWRAP = "/xprt/splits/unwrap/request"
    //order
    val ORDER_DEFINE = "/xprt/orders/define/request"
    val ORDER_MAKE = "/xprt/orders/make/request"
    val ORDER_TAKE = "/xprt/orders/take/request"
    val ORDER_CANCEL = "/xprt/orders/cancel/request"
    //meta
    val META_REVEAL = "/xprt/metas/reveal/request"
    //maintainer
    val MAINTAINER_DEPUTIZE = "/xprt/maintainers/deputize/request"
  }

  object TransactionMessage {
    //auth
    val CREATE_VESTING_ACCOUNT = "/cosmos.vesting.v1beta1.MsgCreateVestingAccount"
    //bank
    val SEND_COIN = "/cosmos.bank.v1beta1.MsgSend"
    val MULTI_SEND = "/cosmos.bank.v1beta1.MsgMultiSend"
    //crisis
    val VERIFY_INVARIANT = "/cosmos.crisis.v1beta1.MsgVerifyInvariant"
    //distribution
    val SET_WITHDRAW_ADDRESS = "/cosmos.distribution.v1beta1.MsgSetWithdrawAddress"
    val WITHDRAW_DELEGATOR_REWARD = "/cosmos.distribution.v1beta1.MsgWithdrawDelegatorReward"
    val WITHDRAW_VALIDATOR_COMMISSION = "/cosmos.distribution.v1beta1.MsgWithdrawValidatorCommission"
    val FUND_COMMUNITY_POOL = "/cosmos.distribution.v1beta1.MsgFundCommunityPool"
    //evidence
    val SUBMIT_EVIDENCE = "/cosmos.evidence.v1beta1.MsgSubmitEvidence"
    //gov
    val DEPOSIT = "/cosmos.gov.v1beta1.MsgDeposit"
    val SUBMIT_PROPOSAL = "/cosmos.gov.v1beta1.MsgSubmitProposal"
    val VOTE = "/cosmos.gov.v1beta1.MsgVote"
    //slashing
    val UNJAIL = "/cosmos.slashing.v1beta1.MsgUnjail"
    //staking
    val CREATE_VALIDATOR = "/cosmos.staking.v1beta1.MsgCreateValidator"
    val EDIT_VALIDATOR = "/cosmos.staking.v1beta1.MsgEditValidator"
    val DELEGATE = "/cosmos.staking.v1beta1.MsgDelegate"
    val REDELEGATE = "/cosmos.staking.v1beta1.MsgBeginRedelegate"
    val UNDELEGATE = "/cosmos.staking.v1beta1.MsgUndelegate"
    //ibc-client
    val CREATE_CLIENT = "/ibc.core.client.v1.MsgCreateClient"
    val UPDATE_CLIENT = "/ibc.core.client.v1.MsgUpdateClient"
    val UPGRADE_CLIENT = "/ibc.core.client.v1.MsgUpgradeClient"
    val SUBMIT_MISBEHAVIOUR = "/ibc.core.client.v1.MsgSubmitMisbehaviour"
    //ibc-connection
    val CONNECTION_OPEN_INIT = "/ibc.core.connection.v1.MsgConnectionOpenInit"
    val CONNECTION_OPEN_TRY = "/ibc.core.connection.v1.MsgConnectionOpenTry"
    val CONNECTION_OPEN_ACK = "/ibc.core.connection.v1.MsgConnectionOpenAck"
    val CONNECTION_OPEN_CONFIRM = "/ibc.core.connection.v1.MsgConnectionOpenConfirm"
    //ibc-channel
    val CHANNEL_OPEN_INIT = "/ibc.core.channel.v1.MsgChannelOpenInit"
    val CHANNEL_OPEN_TRY = "/ibc.core.channel.v1.MsgChannelOpenTry"
    val CHANNEL_OPEN_ACK = "/ibc.core.channel.v1.MsgChannelOpenAck"
    val CHANNEL_OPEN_CONFIRM = "/ibc.core.channel.v1.MsgChannelOpenConfirm"
    val CHANNEL_CLOSE_INIT = "/ibc.core.channel.v1.MsgChannelCloseInit"
    val CHANNEL_CLOSE_CONFIRM = "/ibc.core.channel.v1.MsgChannelCloseConfirm"
    val RECV_PACKET = "/ibc.core.channel.v1.MsgRecvPacket"
    val TIMEOUT = "/ibc.core.channel.v1.MsgTimeout"
    val TIMEOUT_ON_CLOSE = "/ibc.core.channel.v1.MsgTimeoutOnClose"
    val ACKNOWLEDGEMENT = "/ibc.core.channel.v1.MsgAcknowledgement"
    //ibc-transfer
    val TRANSFER = "/ibc.applications.transfer.v1.MsgTransfer"
    //asset
    val ASSET_DEFINE = "/xprt/assets/define/message"
    val ASSET_MINT = "/xprt/assets/mint/message"
    val ASSET_MUTATE = "/xprt/assets/mutate/message"
    val ASSET_BURN = "/xprt/assets/burn/message"
    //identity
    val IDENTITY_DEFINE = "/xprt/identities/define/message"
    val IDENTITY_ISSUE = "/xprt/identities/issue/message"
    val IDENTITY_PROVISION = "/xprt/identities/provision/message"
    val IDENTITY_UNPROVISION = "/xprt/identities/unprovision/message"
    val IDENTITY_NUB = "/xprt/identities/nub/message"
    //split
    val SPLIT_SEND = "/xprt/splits/send/message"
    val SPLIT_WRAP = "/xprt/splits/wrap/message"
    val SPLIT_UNWRAP = "/xprt/splits/unwrap/message"
    //order
    val ORDER_DEFINE = "/xprt/orders/define/message"
    val ORDER_MAKE = "/xprt/orders/make/message"
    val ORDER_TAKE = "/xprt/orders/take/message"
    val ORDER_CANCEL = "/xprt/orders/cancel/message"
    //meta
    val META_REVEAL = "/xprt/metas/reveal/message"
    //maintainer
    val MAINTAINER_DEPUTIZE = "/xprt/maintainers/deputize/message"
  }


}