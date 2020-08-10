package constants

object TransactionMessage {

  //bank
  val SEND_COIN = "cosmos-sdk/MsgSend"

  //crisis
  val VERIFY_INVARIANT = "cosmos-sdk/MsgVerifyInvariant"

  //distribution
  val SET_WITHDRAW_ADDRESS = "cosmos-sdk/MsgModifyWithdrawAddress"
  val WITHDRAW_DELEGATOR_REWARD = "cosmos-sdk/MsgWithdrawDelegationReward"
  val WITHDRAW_VALIDATOR_COMMISSION = "cosmos-sdk/MsgWithdrawValidatorCommission"
  val FUND_COMMUNITY_POOL = "cosmos-sdk/MsgFundCommunityPool"

  //evidence
  val SUBMIT_EVIDENCE = "cosmos-sdk/MsgSubmitEvidence"

  //gov
  val DEPOSIT = "cosmos-sdk/MsgDeposit"
  val SUBMIT_PROPOSAL = "cosmos-sdk/MsgSubmitProposal"
  val VOTE = "cosmos-sdk/MsgVote"

  //slashing
  val UNJAIL = "cosmos-sdk/MsgUnjail"

  //staking
  val CREATE_VALIDATOR = "cosmos-sdk/MsgCreateValidator"
  val EDIT_VALIDATOR = "cosmos-sdk/MsgEditValidator"
  val DELEGATE = "cosmos-sdk/MsgDelegate"
  val REDELEGATE = "cosmos-sdk/MsgBeginRedelegate"
  val UNDELEGATE = "cosmos-sdk/MsgUndelegate"

  val ASSET_MINT = "/xprt/assets/mint/message"
  val ASSET_MUTATE = "/xprt/assets/mutate/message"
  val ASSET_BURN = "/xprt/assets/burn/message"

  val IDENTITY_ISSUE = "/xprt/identities/issue/message"
  val IDENTITY_PROVISION = "/xprt/identities/provision/message"
  val IDENTITY_UNPROVISION = "/xprt/identities/unprovision/message"

  val SPLIT_SEND = "/xprt/splits/send/message"
  val SPLIT_WRAP = "/xprt/splits/wrap/message"
  val SPLIT_UNWRAP = "/xprt/splits/unwrap/message"

  val ORDER_MAKE = "/xprt/orders/make/message"
  val ORDER_TAKE = "/xprt/orders/take/message"
  val ORDER_CANCEL = "/xprt/orders/cancel/message"

  val CLASSIFICATION_DEFINE = "/xprt/classifications/define/message"
}
