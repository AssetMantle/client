package constants

object Blockchain {
  val MnemonicShown = 3
  val FullFundraiserPath = "44'/118'/0'/0/0"
  val AccountPrefix = "cosmos"
  val ValidatorPrefix = "cosmosvaloper"
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

  object PublicKey {
    val MULTI_SIG = "tendermint/PubKeyMultisigThreshold"
    val SINGLE = "tendermint/PubKeySecp256k1"
  }

  object Account {
    val DELAYED_VESTING = "cosmos-sdk/DelayedVestingAccount"
    val BASE = "cosmos-sdk/Account"
    val MODULE = "cosmos-sdk/ModuleAccount"
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

  object Events {

    object Slashing {
      val MissedBlocks = "missed_blocks"
      val DoubleSign = "double_sign"
      val MissingSignature = "missing_signature"

      def getSlashingReason(reason: String): String = reason match {
        case MissingSignature => constants.View.MISSING_SIGNATURE
        case DoubleSign => constants.View.DOUBLE_SIGNING
        case _ => constants.View.UNKNOWN
      }
    }

  }

  object Entity {
    val IDENTITY_DEFINITION = "IDENTITY_DEFINITION"
    val IDENTITY = "IDENTITY"
    val ASSET_DEFINITION = "ASSET_DEFINITION"
    val ASSET = "ASSET"
    val ORDER_DEFINITION = "ORDER_DEFINITION"
    val ORDER = "ORDER"
    val WRAPPED_COIN = "WRAPPED_COIN"
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
    //bank
    val SEND_COIN = "cosmos-sdk/MsgSend"
    val MULTI_SEND = "cosmos-sdk/MsgMultiSend"
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