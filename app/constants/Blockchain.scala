package constants

import com.google.common.collect
import com.google.common.collect.ImmutableList
import models.blockchain.Classification
import org.bitcoinj.crypto.ChildNumber
import play.api.Configuration
import schema.data.base.{IDData, ListData}
import schema.id.base.{ClassificationID, PropertyID, StringID}
import schema.property.base.MetaProperty
import schema.list._
import schema.qualified.{Immutables, Mutables}

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object Blockchain {
  val MnemonicShown = 3
  val FullFundraiserPath = "44'/118'/0'/0/0"
  val AccountPrefix: String = AppConfig.configuration.get[String]("blockchain.account.prefix")
  val ValidatorPrefix: String = AccountPrefix + "valoper"
  val ValidatorConsensusPublicPrefix: String = AccountPrefix + "valconspub"
  val IBCDenoms: Seq[AppConfig.IBCDenom] = AppConfig.configuration.get[Seq[Configuration]]("blockchain.ibcDenoms.ibcDenomList").map { ibcDenoms =>
    constants.AppConfig.IBCDenom(hash = ibcDenoms.get[String]("hash"), name = ibcDenoms.get[String]("name"))
  }
  val ChainID: String = AppConfig.configuration.get[String]("blockchain.chainID")
  val StakingDenom: String = AppConfig.configuration.get[String]("blockchain.stakingDenom")
  val NegotiationDefaultTime = 5000000
  val DefaultFaucetTokenAmount = 1
  val IDSeparator = "."
  val FirstOrderCompositeIDSeparator = "|"
  val SecondOrderCompositeIDSeparator = "*"
  val OneDec = BigDecimal("1.000000000000000000")
  val ZeroDec = BigDecimal("0.0")
  val SmallestDec = BigDecimal("0.000000000000000001")
  val SmallestDecReciprocal: BigDecimal = 1 / SmallestDec
  val ToHashSeparator = "_"
  val RequestPropertiesSeparator = ","
  val DataNameAndTypeSeparator = ":"
  val DataTypeAndValueSeparator = "|"
  val MaxTraits = 22
  val HeightDataDefaultValue: Int = -1
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
  val TransactionMode: String = AppConfig.configuration.get[String]("blockchain.transaction.mode")
  val KafkaEnabled: Boolean = AppConfig.configuration.get[Boolean]("blockchain.kafka.enabled")
  val KafkaTxIteratorInitialDelay: FiniteDuration = AppConfig.configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").second
  val KafkaTxIteratorInterval: FiniteDuration = AppConfig.configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  val EnableTxSchemaActor: Boolean = AppConfig.configuration.get[Boolean]("blockchain.enableTransactionSchemaActors")
  val nubClassificationID: Array[Byte] = commonUtilities.Secrets.base64Decoder("DtqQ0fXQ45Bm0eavjtbwg3GSHGP+6ylMIILn6WmkY5Y=")
  val AuthenticationProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("authentication"), typeID = ListData(Seq()).getType), data = ListData(Seq()).toAnyData)
  val NubProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("nubID"), typeID = IDData(StringID("").toAnyID).getType), data = IDData(StringID("").toAnyID).toAnyData)
  val NubClassificationID: ClassificationID = commonUtilities.ID.getClassificationID(Immutables(PropertyList(Seq(NubProperty))), Mutables(PropertyList(Seq(AuthenticationProperty))))
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

  object TransactionMessage {
    //auth
    val CREATE_VESTING_ACCOUNT = "/cosmos.vesting.v1beta1.MsgCreateVestingAccount"
    //authz
    val GRANT_AUTHORIZATION = "/cosmos.authz.v1beta1.MsgGrant"
    val REVOKE_AUTHORIZATION = "/cosmos.authz.v1beta1.MsgRevoke"
    val EXECUTE_AUTHORIZATION = "/cosmos.authz.v1beta1.MsgExec"
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
    //feeGrant
    val FEE_GRANT_ALLOWANCE = "/cosmos.feegrant.v1beta1.MsgGrantAllowance"
    val FEE_REVOKE_ALLOWANCE = "/cosmos.feegrant.v1beta1.MsgRevokeAllowance"
    //gov
    val DEPOSIT = "/cosmos.gov.v1beta1.MsgDeposit"
    val SUBMIT_PROPOSAL = "/cosmos.gov.v1beta1.MsgSubmitProposal"
    val VOTE = "/cosmos.gov.v1beta1.MsgVote"
    val WEIGHTED_VOTE = "/cosmos.gov.v1beta1.MsgVoteWeighted"
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
    val ASSET_BURN = "/assets.transactions.burn.Message"
    val ASSET_DEFINE = "/assets.transactions.define.Message"
    val ASSET_DEPUTIZE = "/assets.transactions.deputize.Message"
    val ASSET_MINT = "/assets.transactions.mint.Message"
    val ASSET_MUTATE = "/assets.transactions.mutate.Message"
    val ASSET_RENUMERATE = "/assets.transactions.renumerate.Message"
    val ASSET_REVOKE = "/assets.transactions.revoke.Message"
    //identity
    val IDENTITY_DEFINE = "/identities.transactions.define.Message"
    val IDENTITY_DEPUTIZE = "/identities.transactions.deputize.Message"
    val IDENTITY_ISSUE = "/identities.transactions.issue.Message"
    val IDENTITY_MUTATE = "/identities.transactions.mutate.Message"
    val IDENTITY_NUB = "/identities.transactions.nub.Message"
    val IDENTITY_PROVISION = "/identities.transactions.provision.Message"
    val IDENTITY_QUASH = "/identities.transactions.quash.Message"
    val IDENTITY_REVOKE = "/identities.transactions.revoke.Message"
    val IDENTITY_UNPROVISION = "/identities.transactions.unprovision.Message"
    //split
    val SPLIT_SEND = "/splits.transactions.send.Message"
    val SPLIT_WRAP = "/splits.transactions.wrap.Message"
    val SPLIT_UNWRAP = "/splits.transactions.unwrap.Message"
    //order
    val ORDER_CANCEL = "/orders.transactions.cancel.Message"
    val ORDER_DEFINE = "/orders.transactions.define.Message"
    val ORDER_DEPUTIZE = "/orders.transactions.deputize.Message"
    val ORDER_IMMEDIATE = "/orders.transactions.immediate.Message"
    val ORDER_MAKE = "/orders.transactions.make.Message"
    val ORDER_MODIFY = "/orders.transactions.modify.Message"
    val ORDER_REVOKE = "/orders.transactions.revoke.Message"
    val ORDER_TAKE = "/orders.transactions.take.Message"
    //metaList
    val META_REVEAL = "/metas.transactions.reveal.Message"
  }


}