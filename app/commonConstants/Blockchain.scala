package commonConstants

import org.bitcoinj.crypto.ChildNumber

object Blockchain {

  val AccountPrefix = "mantle"
  val ValidatorPrefix: String = AccountPrefix + "valoper"
  val ValidatorConsensusPublicPrefix: String = AccountPrefix + "valconspub"
  val CoinType = 118
  val DefaultHDPath: Seq[ChildNumber] = Seq(
    new ChildNumber(44, true),
    new ChildNumber(CoinType, true),
    new ChildNumber(0, true),
    new ChildNumber(0, false),
    new ChildNumber(0, false)
  )

  val IDSeparator = "."

  object PublicKey {
    val MULTI_SIG = "/cosmos.crypto.multisig.LegacyAminoPubKey"
    val SINGLE_SECP256K1 = "/cosmos.crypto.secp256k1.PubKey"
    val SINGLE_SECP256R1 = "/cosmos.crypto.secp256r1.PubKey"
    val VALIDATOR = "/cosmos.crypto.ed25519.PubKey"
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
