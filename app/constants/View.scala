package constants

import scala.language.postfixOps

object View {
  val ASSET_MANTLE = "ASSET_MANTLE"
  val APP_NAME = "APP_NAME"
  val META_DESCRIPTION = "META_DESCRIPTION"
  val STATUS = "STATUS"
  val DOCUMENT = "DOCUMENT"

  val DESCRIPTION = "DESCRIPTION"
  val TYPE = "TYPE"
  val UNKNOWN = "UNKNOWN"

  //ChatRoom
  val LOAD_MORE = "LOAD_MORE"

  //Recent Activities
  val LOAD_MORE_ACTIVITIES = "LOAD_MORE_ACTIVITIES"

  val RECENT_ACTIVITY = "RECENT_ACTIVITY"
  val DETAILS = "DETAILS"
  val ACCOUNT = "ACCOUNT"
  val INDEX = "INDEX"
  val DASHBOARD = "DASHBOARD"
  val COMMIT = "COMMIT"
  val PROFILE = "PROFILE"

  val NAME = "NAME"
  val ID = "ID"
  val TITLE = "TITLE"
  val PENDING = "PENDING"
  val DELETE = "DELETE"
  val BACK = "BACK"
  val YES = "YES"
  val NO = "NO"
  val FOOTER_TEXT = "FOOTER_TEXT"
  val ADD = "ADD"
  val REGISTER = "REGISTER"
  val NO_ACTIVITIES = "NO_ACTIVITIES"
  val NEXT = "NEXT"
  val SUBMIT = "SUBMIT"
  val PROFILE_PICTURE = "PROFILE_PICTURE"
  val ADDRESS = "ADDRESS"
  val CONNECTION_ERROR = "CONNECTION_ERROR"
  val FAILURE = "FAILURE"
  val WARNING = "WARNING"
  val SUCCESS = "SUCCESS"
  val INFORMATION = "INFORMATION"
  val BLOCKCHAIN_ADDRESS = "BLOCKCHAIN_ADDRESS"
  val BROWSE = "BROWSE"
  val OR = "OR"
  val DROP_FILE = "DROP_FILE"
  val UPLOAD = "UPLOAD"
  val UPDATE = "UPDATE"
  val FOOTER_LOGO = "FOOTER_LOGO"
  val FROM = "FROM"
  val TO = "TO"
  val NUB_ID = "NUB_ID"

  val BLOCKS = "BLOCKS"
  val TRANSACTION_STATISTICS_X_LABEL = "TRANSACTION_STATISTICS_X_LABEL"
  val TRANSACTIONS = "TRANSACTIONS"
  val TRANSACTION = "TRANSACTION"
  val VALIDATORS = "VALIDATORS"
  val PROPOSALS = "PROPOSALS"
  val OTHERS = "OTHERS"
  val WALLET = "WALLET"

  val BLOCKCHAIN_CONNECTION_LOST = "BLOCKCHAIN_CONNECTION_LOST"

  val BLOCK = "BLOCK"
  val WALLET_ADDRESS = "WALLET_ADDRESS"

  val LATEST_BLOCK_HEIGHT = "LATEST_BLOCK_HEIGHT"
  val BLOCK_HEADER = "BLOCK_HEADER"
  val HEIGHT = "HEIGHT"
  val PROPOSER = "PROPOSER"

  val TIME = "TIME"
  val NUMBER_OF_TRANSACTIONS = "NUMBER_OF_TRANSACTIONS"
  val AVERAGE_BLOCK_TIME = "AVERAGE_BLOCK_TIME"
  val CHAIN_ID = "CHAIN_ID"
  val USD = "USD"

  val NO_BLOCK_TRANSACTIONS = "NO_BLOCK_TRANSACTIONS"
  val BLOCK_TRANSACTIONS = "BLOCK_TRANSACTIONS"
  val TRANSACTION_HASH = "TRANSACTION_HASH"
  val HASH = "HASH"
  val TRANSACTION_FEES = "TRANSACTION_FEES"
  val NUMBER_OF_MESSAGES = "NUMBER_OF_MESSAGES"
  val LOG = "LOG"
  val CODE = "CODE"
  val MEMO = "MEMO"
  val SIGNER = "SIGNER"
  val FEE_PAYER = "FEE_PAYER"
  val TOKENS_STATISTICS = "TOKENS_STATISTICS"
  val VOTING_POWERS = "VOTING_POWERS"
  val TOTAL_ACTIVE_VALIDATORS = "TOTAL_ACTIVE_VALIDATORS"
  val TOP_VALIDATORS = "TOP_VALIDATORS"
  val ACCOUNT_WALLET = "ACCOUNT_WALLET"
  val TOKEN_PRICE = "TOKEN_PRICE"
  val IMMUTABLE_META_PROPERTIES = "IMMUTABLE_META_PROPERTIES"
  val IMMUTABLE_MESA_PROPERTIES = "IMMUTABLE_MESA_PROPERTIES"
  val MUTABLE_META_PROPERTIES = "MUTABLE_META_PROPERTIES"
  val MUTABLE_MESA_PROPERTIES = "MUTABLE_MESA_PROPERTIES"

  val PROPOSAL = "PROPOSAL"
  val PROPOSAL_DEPOSITS = "PROPOSAL_DEPOSITS"
  val PROPOSAL_VOTES = "PROPOSAL_VOTES"
  val PROPOSAL_ID = "PROPOSAL_ID"
  val VALIDATOR = "VALIDATOR"
  val ACTIVE_VALIDATORS = "ACTIVE_VALIDATORS"
  val INACTIVE_VALIDATORS = "INACTIVE_VALIDATORS"
  val VALIDATOR_DESCRIPTION = "VALIDATOR_DESCRIPTION"
  val OPERATOR_ADDRESS = "OPERATOR_ADDRESS"
  val VALIDATOR_MONIKER = "VALIDATOR_MONIKER"
  val VALIDATOR_IDENTITY = "VALIDATOR_IDENTITY"
  val VALIDATOR_WEBSITE = "VALIDATOR_WEBSITE"
  val VALIDATOR_SECURITY_CONTACT = "VALIDATOR_SECURITY_CONTACT"
  val VALIDATOR_DESCRIPTION_DETAILS = "VALIDATOR_DESCRIPTION_DETAILS"
  val VALIDATOR_DELEGATIONS = "VALIDATOR_DELEGATIONS"
  val VALIDATOR_COMMISSION = "VALIDATOR_COMMISSION"
  val TOKENS = "TOKENS"
  val RANK = "RANK"
  val DELEGATOR_SHARES = "DELEGATOR_SHARES"
  val COMMISSION_RATE = "COMMISSION_RATE"
  val DELEGATIONS = "DELEGATIONS"
  val MAXIMUM_RATE = "MAXIMUM_RATE"
  val MAXIMUM_CHANGE_RATE = "MAXIMUM_CHANGE_RATE"
  val VALIDATOR_BONDING_STATUS = "VALIDATOR_BONDING_STATUS"
  val JAILED = "JAILED"
  val ACTIVE = "ACTIVE"
  val INACTIVE = "INACTIVE"
  val VOTING_POWER = "VOTING_POWER"
  val LAST_UPDATED = "LAST_UPDATED"
  val TRANSACTION_MESSAGE_TYPE = "TRANSACTION_MESSAGE_TYPE"
  val MESSAGE = "MESSAGE"
  val VALIDATOR_ADDRESS = "VALIDATOR_ADDRESS"
  val DELEGATOR_ADDRESS = "DELEGATOR_ADDRESS"
  val WITHDRAW_ADDRESS = "WITHDRAW_ADDRESS"
  val PUBLIC_KEY = "PUBLIC_KEY"
  val AMOUNT_DELEGATED = "AMOUNT_DELEGATED"
  val MINIMUM_SELF_DELEGATION = "MINIMUM_SELF_DELEGATION"
  val VALIDATOR_SOURCE_ADDRESS = "VALIDATOR_SOURCE_ADDRESS"
  val VALIDATOR_DESTINATION_ADDRESS = "VALIDATOR_DESTINATION_ADDRESS"
  val VOTING_START = "VOTING_START"
  val VOTING_END = "VOTING_END"
  val SUBMIT_TIME = "SUBMIT_TIME"
  val TOTAL_DEPOSIT = "TOTAL_DEPOSIT"
  val INITIAL_DEPOSIT = "INITIAL_DEPOSIT"
  val DEPOSIT_END_TIME = "DEPOSIT_END_TIME"
  val CONTENT = "CONTENT"
  val VOTE = "VOTE"
  val FINAL_TALLY_RESULTS = "FINAL_TALLY_RESULTS"
  val NO_WITH_VETO = "NO_WITH_VETO"
  val ABSTAIN = "ABSTAIN"
  val KEPLR_ERROR_MESSAGE = "KEPLR_ERROR_MESSAGE"

  val BONDED_AMOUNT = "BONDED_AMOUNT"
  val NOT_BONDED_AMOUNT = "NOT_BONDED_AMOUNT"
  val TOTAL_SUPPLY = "TOTAL_SUPPLY"
  val COMMUNITY_POOL = "COMMUNITY_POOL"
  val APY = "APY"
  val INFLATION = "INFLATION"
  val BONDED_TOKENS = "BONDED_TOKENS"
  val STAKING_POOL = "STAKING_POOL"
  val EVENTS = "EVENTS"
  val NO_EVENTS = "NO_EVENTS"
  val LOCKED_AMOUNT = "LOCKED_AMOUNT"
  val BURNED_AMOUNT = "BURNED_AMOUNT"

  val MONIKER_NOT_FOUND = "MONIKER_NOT_FOUND"
  val TRANSACTION_DETAILS = "TRANSACTION_DETAILS"
  val TRANSACTION_MESSAGES = "TRANSACTION_MESSAGES"

  val AVAILABLE = "AVAILABLE"
  val DELEGATED = "DELEGATED"
  val UNDELEGATING = "UNDELEGATING"
  val REDELEGATING = "REDELEGATING"
  val GRANTED = "GRANTED"
  val ASSIGNED = "ASSIGNED"
  val COMMISSION_REWARDS = "COMMISSION_REWARDS"
  val DELEGATOR_REWARDS = "DELEGATOR_REWARDS"
  val STAKING_TOKEN = "STAKING_TOKEN"
  val ALL_TOKENS = "ALL_TOKENS"
  val REWARDS = "REWARDS"
  val TOTAL_REWARDS = "TOTAL_REWARDS"
  val TRANSACTIONS_TRAFFIC = "TRANSACTIONS_TRAFFIC"
  val TRANSACTIONS_MESSAGES_TRAFFIC = "TRANSACTIONS_MESSAGES_TRAFFIC"
  val TOTAL_ACCOUNTS = "TOTAL_ACCOUNTS"
  val TOTAL_TRANSACTIONS = "TOTAL_TRANSACTIONS"
  val TOTAL_IBC_IN_TRANSACTIONS = "TOTAL_IBC_IN_TRANSACTIONS"
  val TOTAL_IBC_OUT_TRANSACTIONS = "TOTAL_IBC_OUT_TRANSACTIONS"
  val TOTAL_DELEGATE_TRANSACTIONS = "TOTAL_DELEGATE_TRANSACTIONS"
  val TOTAL_EXECUTE_AUTHORIZATION_TRANSACTIONS = "TOTAL_EXECUTE_AUTHORIZATION_TRANSACTIONS"
  val TOTAL_VALUE = "TOTAL_VALUE"
  val KEPLR = "KEPLR"

  val UPTIME = "UPTIME"
  val SELF_DELEGATED = "SELF_DELEGATED"
  val OTHERS_DELEGATED = "OTHERS_DELEGATED"
  val ACCOUNT_DELEGATIONS = "ACCOUNT_DELEGATIONS"
  val ACCOUNT_TRANSACTIONS = "ACCOUNT_TRANSACTIONS"
  val AMOUNT_UNDELEGATING = "AMOUNT_UNDELEGATING"
  val COMPLETION_TIME = "COMPLETION_TIME"
  val NO_TRANSACTIONS_FOUND = "NO_TRANSACTIONS_FOUND"
  val VALIDATOR_TRANSACTIONS = "VALIDATOR_TRANSACTIONS"

  val DATA = "DATA"
  val INPUTS = "INPUTS"
  val OUTPUTS = "OUTPUTS"
  val VALUE = "VALUE"
  val INVALID_MNEMONICS = "INVALID_MNEMONICS"
  val EMPTY_WALLET = "EMPTY_WALLET"
  val AMOUNT = "AMOUNT"

  val DOUBLE_SIGNING = "DOUBLE_SIGNING"
  val MISSING_SIGNATURE = "MISSING_SIGNATURE"
  val COINS = "COINS"
  val COIN = "COIN"
  val FROM_ID = "FROM_ID"
  val TO_ID = "TO_ID"
  val OWNABLE_ID = "OWNABLE_ID"
  val CLASSIFICATION_ID = "CLASSIFICATION_ID"
  val MAINTAINED_PROPERTIES = "MAINTAINED_PROPERTIES"
  val TAKER_ID = "TAKER_ID"
  val MAKER_OWNABLE_ID = "MAKER_OWNABLE_ID"
  val TAKER_OWNABLE_ID = "TAKER_OWNABLE_ID"
  val AMOUNTS = "AMOUNTS"
  val MINT = "MINT"
  val BURN = "BURN"
  val CAN_MAKE = "CAN_MAKE"
  val CAN_CANCEL = "CAN_CANCEL"
  val CAN_ISSUE = "CAN_ISSUE"
  val CAN_QUASH = "CAN_QUASH"
  val MAINTAINER = "MAINTAINER"
  val ADD_MAINTAINER = "ADD_MAINTAINER"
  val MUTATE_MAINTAINER = "MUTATE_MAINTAINER"
  val REMOVE_MAINTAINER = "REMOVE_MAINTAINER"
  val RENUMERATE = "RENUMERATE"
  val LATEST_BLOCKS = "LATEST_BLOCKS"
  val COPY = "COPY"
  val END_TIME = "END_TIME"
  val DELAYED = "DELAYED"
  val SENDER = "SENDER"
  val INVARIANT_MODULE_NAME = "INVARIANT_MODULE_NAME"
  val INVARIANT_ROUTE = "INVARIANT_ROUTE"
  val DEPOSITOR = "DEPOSITOR"
  val SUBMITTER = "SUBMITTER"
  val EVIDENCE = "EVIDENCE"
  val POWER = "POWER"
  val CONSENSUS_ADDRESS = "CONSENSUS_ADDRESS"
  val CLIENT_ID = "CLIENT_ID"
  val COUNTERPARTY = "COUNTERPARTY"
  val CONNECTION_ID = "CONNECTION_ID"
  val VERSION = "VERSION"
  val IDENTIFIER = "IDENTIFIER"
  val IDENTITY_ID = "IDENTITY_ID"
  val ASSET_ID = "ASSET_ID"
  val ORDER_ID = "ORDER_ID"
  val FEATURES = "FEATURES"
  val DELAY_PERIOD = "DELAY_PERIOD"
  val CLIENT_HEIGHT = "CLIENT_HEIGHT"
  val REVISION_HEIGHT = "REVISION_HEIGHT"
  val REVISION_NUMBER = "REVISION_NUMBER"
  val CHANNEL = "CHANNEL"
  val STATE = "STATE"
  val ORDERING = "ORDERING"
  val CONNECTION_HOPS = "CONNECTION_HOPS"
  val FUNGIBLE_TOKEN_PACKET_DATA = "FUNGIBLE_TOKEN_PACKET_DATA"
  val DENOM = "DENOM"
  val RECEIVER = "RECEIVER"
  val SEQUENCE = "SEQUENCE"
  val SOURCE_PORT = "SOURCE_PORT"
  val SOURCE_CHANNEL = "SOURCE_CHANNEL"
  val DESTINATION_PORT = "DESTINATION_PORT"
  val DESTINATION_CHANNEL = "DESTINATION_CHANNEL"
  val TIMEOUT_HEIGHT = "TIMEOUT_HEIGHT"
  val TIMEOUT_TIMESTAMP = "TIMEOUT_TIMESTAMP"
  val PROOF_HEIGHT = "PROOF_HEIGHT"
  val COUNTERPARTY_CONNECTION_ID = "COUNTERPARTY_CONNECTION_ID"
  val CONSENSUS_HEIGHT = "CONSENSUS_HEIGHT"
  val PREVIOUS_CONNECTION_ID = "PREVIOUS_CONNECTION_ID"
  val PORT_ID = "PORT_ID"
  val PREVIOUS_CHANNEL_ID = "PREVIOUS_CHANNEL_ID"
  val COUNTERPARTY_VERSION = "COUNTERPARTY_VERSION"
  val CHANNEL_ID = "CHANNEL_ID"
  val COUNTERPARTY_CHANNEL_ID = "COUNTERPARTY_CHANNEL_ID"
  val PACKET = "PACKET"
  val NEXT_SEQUENCE_RECV = "NEXT_SEQUENCE_RECV"
  val TOKEN = "TOKEN"
  val VOTER = "VOTER"
  val OPTION = "OPTION"
  val SUBSPACE = "SUBSPACE"
  val KEY = "KEY"
  val RECIPIENT = "RECIPIENT"
  val INFO = "INFO"
  val MESSAGES_TYPE = "MESSAGES_TYPE"
  val NO_DELEGATIONS_FOUND = "NO_DELEGATIONS_FOUND"

  val AUTHORIZATION_TYPE = "AUTHORIZATION_TYPE"
  val GRANT = "GRANT"
  val GRANTER = "GRANTER"
  val GRANTEE = "GRANTEE"
  val ALLOWANCE = "ALLOWANCE"
  val EXPIRATION = "EXPIRATION"
  val MAKER_OWNABLE_SPLIT = "MAKER_OWNABLE_SPLIT"
  val TAKER_OWNABLE_SPLIT = "TAKER_OWNABLE_SPLIT"
  val SPEND_LIMITS = "SPEND_LIMITS"
  val MAX_TOKENS = "MAX_TOKENS"
  val ALLOW_LIST = "ALLOW_LIST"
  val DENY_LIST = "DENY_LIST"
  val AUTHORIZATION = "AUTHORIZATION"
  val AUTHORIZATIONS = "AUTHORIZATIONS"
  val MESSAGES = "MESSAGES"
  val ALLOWANCE_TYPE = "ALLOWANCE_TYPE"
  val PERIOD = "PERIOD"
  val PERIOD_SPEND_LIMIT = "PERIOD_SPEND_LIMIT"
  val PERIOD_CAN_SPEND = "PERIOD_CAN_SPEND"
  val PERIOD_RESET = "PERIOD_RESET"
  val ALLOWED_MESSAGES = "ALLOWED_MESSAGES"
  val STAKE_AUTHORIZATION_TYPE = "STAKE_AUTHORIZATION_TYPE"
  val NO_EXPIRY_DATE = "NO_EXPIRY_DATE"
  val PLATFORM_NAME = "PLATFORM_NAME"
  val PROPERTY_ID = "PROPERTY_ID"

  val VALIDATORS_VOTES = "VALIDATORS_VOTES"
  val VALIDATOR_NAME = "VALIDATOR_NAME"
  val TRANSACTION_ANSWER = "TRANSACTION_ANSWER"
  val TRANSACTION_TIME = "TRANSACTION_TIME"
  val WALLET_DETAILS = "WALLET_DETAILS"
  val DOCUMENT_NOT_FOUND = "DOCUMENT_NOT_FOUND"

  val PARAMETERS = "PARAMETERS"
  val MINTING_PARAMETERS = "MINTING_PARAMETERS"
  val BLOCKS_PER_YEAR = "BLOCKS_PER_YEAR"
  val INFLATION_MAX = "INFLATION_MAX"
  val MINT_DECIMAL = "MINT_DECIMAL"
  val GOAL_BONDED = "GOAL_BONDED"
  val INFLATION_MIN = "INFLATION_MIN"
  val MINT_DENOM = "MINT_DENOM"
  val INFLATION_RATE_CHANGE = "INFLATION_RATE_CHANGE"
  val STAKING_PARAMETERS = "STAKING_PARAMETERS"
  val UNBONDING_TIME = "UNBONDING_TIME"
  val HISTORICAL_ENTRIES = "HISTORICAL_ENTRIES"
  val MAX_ENTRIES = "MAX_ENTRIES"
  val MAX_VALIDATORS = "MAX_VALIDATORS"
  val BOND_DENOM = "BOND_DENOM"
  val GOVERNANCE_PARAMETERS = "GOVERNANCE_PARAMETERS"
  val MIN_DEPOSIT = "MIN_DEPOSIT"
  val VOTING_PERIOD = "VOTING_PERIOD"
  val THRESHOLD = "THRESHOLD"
  val MAX_DEPOSIT_PERIOD = "MAX_DEPOSIT_PERIOD"
  val QUORUM = "QUORUM"
  val VETO_THRESHOLD = "VETO_THRESHOLD"
  val DISTRIBUTION_PARAMETERS = "DISTRIBUTION_PARAMETERS"
  val BASE_PROPOSER_REWARD = "BASE_PROPOSER_REWARD"
  val COMMUNITY_TAX = "COMMUNITY_TAX"
  val BONUS_PROPOSER_REWARD = "BONUS_PROPOSER_REWARD"
  val WITHDRAW_ADDR_ENABLED = "WITHDRAW_ADDR_ENABLED"
  val SLASHING_PARAMETERS = "SLASHING_PARAMETERS"
  val SIGNED_BLOCKS_WINDOW = "SIGNED_BLOCKS_WINDOW"
  val MIN_SIGNED_PER_WINDOW = "MIN_SIGNED_PER_WINDOW"
  val SLASH_FRACTION_DOWNTIME = "SLASH_FRACTION_DOWNTIME"
  val DOWNTIME_JAIL_DURATION = "DOWNTIME_JAIL_DURATION"
  val SLASH_FRACTION_DOUBLESIGN = "SLASH_FRACTION_DOUBLESIGN"
  val ASSETS_PARAMETER = "ASSETS_PARAMETER"
  val CLASSIFICATIONS_PARAMETER = "CLASSIFICATIONS_PARAMETER"
  val IDENTITIES_PARAMETER = "IDENTITIES_PARAMETER"
  val MAINTAINERS_PARAMETER = "MAINTAINERS_PARAMETER"
  val METAS_PARAMETER = "METAS_PARAMETER"
  val ORDERS_PARAMETER = "ORDERS_PARAMETER"
  val SPLITS_PARAMETER = "SPLITS_PARAMETER"
  val MINT_ASSET = "MINT_ASSET"
  val BURN_ASSET = "BURN_ASSET"
  val RENUMERATE_ASSET = "RENUMERATE_ASSET"
  val BOND_RATE = "BOND_RATE"
  val MAX_PROPERTY = "MAX_PROPERTY"
  val MAX_PROVISIONED_ADDRESSES = "MAX_PROVISIONED_ADDRESSES"
  val DEPTUZIE_ALLOWED = "DEPTUZIE_ALLOWED"
  val REVEAL_ENABLED = "REVEAL_ENABLED"
  val MAX_ORDER_LIFE = "MAX_ORDER_LIFE"
  val WRAPPING_ALLOWED_DENOMS = "WRAPPING_ALLOWED_DENOMS"
  val UNWRAPPING_ALLOWED_DENOMS = "UNWRAPPING_ALLOWED_DENOMS"
  val TOKEN_DETAILS = "TOKEN_DETAILS"
  val ASSET = "ASSET"
  val IDENTITY = "IDENTITY"
  val ORDER = "ORDER"
  val CLASSIFICATION = "CLASSIFICATION"
  val TRANSFER_ENABLED = "TRANSFER_ENABLED"
  val DEFINE_ENABLED = "DEFINE_ENABLED"
  val ISSUE_ENABLED = "ISSUE_ENABLED"
  val QUASH_ENABLED = "QUASH_ENABLED"
  val PUT_ENABLED = "PUT_ENABLED"

  val AuthzAuthorizationMap: Map[String, String] = Map(
    constants.Blockchain.Authz.SEND_AUTHORIZATION -> "SEND_AUTHORIZATION",
    constants.Blockchain.Authz.GENERIC_AUTHORIZATION -> "GENERIC_AUTHORIZATION",
    constants.Blockchain.Authz.STAKE_AUTHORIZATION -> "STAKE_AUTHORIZATION",
  )

  val FeeGrantAllowanceMap: Map[String, String] = Map(
    constants.Blockchain.FeeGrant.BASIC_ALLOWANCE -> "BASIC_ALLOWANCE",
    constants.Blockchain.FeeGrant.PERIODIC_ALLOWANCE -> "PERIODIC_ALLOWANCE",
    constants.Blockchain.FeeGrant.ALLOWED_MSG_ALLOWANCE -> "ALLOWED_MSG_ALLOWANCE",
  )

  val TxMessagesMap: Map[String, String] = Map(
    //auth
    schema.constants.Messages.CREATE_VESTING_ACCOUNT -> "MESSAGE_CREATE_VESTING_ACCOUNT",
    //authz
    schema.constants.Messages.GRANT_AUTHORIZATION -> "MESSAGE_GRANT_AUTHORIZATION",
    schema.constants.Messages.REVOKE_AUTHORIZATION -> "MESSAGE_REVOKE_AUTHORIZATION",
    schema.constants.Messages.EXECUTE_AUTHORIZATION -> "MESSAGE_EXECUTE_AUTHORIZATION",
    //bank
    schema.constants.Messages.SEND_COIN -> "MESSAGE_SEND",
    schema.constants.Messages.MULTI_SEND -> "MESSAGE_MULTI_SEND",
    //evidence
    schema.constants.Messages.VERIFY_INVARIANT -> "MESSAGE_VERIFY_INVARIANT",
    //feeGrant
    schema.constants.Messages.FEE_GRANT_ALLOWANCE -> "MESSAGE_FEE_GRANT_ALLOWANCE",
    schema.constants.Messages.FEE_REVOKE_ALLOWANCE -> "MESSAGE_FEE_REVOKE_ALLOWANCE",
    //distribution
    schema.constants.Messages.SET_WITHDRAW_ADDRESS -> "MESSAGE_SET_WITHDRAW_ADDRESS",
    schema.constants.Messages.WITHDRAW_DELEGATOR_REWARD -> "MESSAGE_WITHDRAW_REWARD",
    schema.constants.Messages.WITHDRAW_VALIDATOR_COMMISSION -> "MESSAGE_WITHDRAW_COMMISSION",
    schema.constants.Messages.FUND_COMMUNITY_POOL -> "MESSAGE_FUND_COMMUNITY_POOL",
    schema.constants.Messages.SUBMIT_EVIDENCE -> "MESSAGE_SUBMIT_EVIDENCE",
    schema.constants.Messages.DEPOSIT -> "MESSAGE_DEPOSIT",
    schema.constants.Messages.SUBMIT_PROPOSAL -> "MESSAGE_PROPOSAL",
    schema.constants.Messages.VOTE -> "MESSAGE_VOTE",
    schema.constants.Messages.UNJAIL -> "MESSAGE_UNJAIL",
    schema.constants.Messages.CREATE_VALIDATOR -> "MESSAGE_CREATE_VALIDATOR",
    schema.constants.Messages.EDIT_VALIDATOR -> "MESSAGE_EDIT_VALIDATOR",
    schema.constants.Messages.DELEGATE -> "MESSAGE_DELEGATE",
    schema.constants.Messages.REDELEGATE -> "MESSAGE_REDELEGATE",
    schema.constants.Messages.UNDELEGATE -> "MESSAGE_UNBOND",
    schema.constants.Messages.CREATE_CLIENT -> "MESSAGE_IBC_CREATE_CLIENT",
    schema.constants.Messages.UPDATE_CLIENT -> "MESSAGE_IBC_UPDATE_CLIENT",
    schema.constants.Messages.UPGRADE_CLIENT -> "MESSAGE_IBC_UPGRADE_CLIENT",
    schema.constants.Messages.SUBMIT_MISBEHAVIOUR -> "MESSAGE_IBC_MISBEHAVIOUR",
    schema.constants.Messages.CONNECTION_OPEN_INIT -> "MESSAGE_IBC_CONN_OPEN_INIT",
    schema.constants.Messages.CONNECTION_OPEN_TRY -> "MESSAGE_IBC_CONN_OPEN_TRY",
    schema.constants.Messages.CONNECTION_OPEN_ACK -> "MESSAGE_IBC_CONN_OPEN_ACK",
    schema.constants.Messages.CONNECTION_OPEN_CONFIRM -> "MESSAGE_IBC_CONN_OPEN_CONFIRM",
    schema.constants.Messages.CHANNEL_OPEN_INIT -> "MESSAGE_IBC_CHANNEL_OPEN_INIT",
    schema.constants.Messages.CHANNEL_OPEN_TRY -> "MESSAGE_IBC_CHANNEL_OPEN_TRY",
    schema.constants.Messages.CHANNEL_OPEN_ACK -> "MESSAGE_IBC_CHANNEL_OPEN_ACK",
    schema.constants.Messages.CHANNEL_OPEN_CONFIRM -> "MESSAGE_IBC_CHANNEL_OPEN_CONFIRM",
    schema.constants.Messages.CHANNEL_CLOSE_INIT -> "MESSAGE_IBC_CHANNEL_CLOSE_INIT",
    schema.constants.Messages.CHANNEL_CLOSE_CONFIRM -> "MESSAGE_IBC_CHANNEL_CLOSE_CONFIRM",
    schema.constants.Messages.RECV_PACKET -> "MESSAGE_IBC_RECV_PACKET",
    schema.constants.Messages.TIMEOUT -> "MESSAGE_IBC_TIMEOUT",
    schema.constants.Messages.TIMEOUT_ON_CLOSE -> "MESSAGE_IBC_TIMEOUT_ON_CLOSE",
    schema.constants.Messages.ACKNOWLEDGEMENT -> "MESSAGE_IBC_ACKNOWLEDGEMENT",
    schema.constants.Messages.TRANSFER -> "MESSAGE_IBC_TRANSFER",
    //assets
    schema.constants.Messages.ASSET_BURN -> "MESSAGE_ASSET_BURN",
    schema.constants.Messages.ASSET_DEFINE -> "MESSAGE_ASSET_DEFINE",
    schema.constants.Messages.ASSET_DEPUTIZE -> "MESSAGE_ASSET_DEPUTIZE",
    schema.constants.Messages.ASSET_MINT -> "MESSAGE_ASSET_MINT",
    schema.constants.Messages.ASSET_MUTATE -> "MESSAGE_ASSET_MUTATE",
    schema.constants.Messages.ASSET_RENUMERATE -> "MESSAGE_ASSET_RENUMERATE",
    schema.constants.Messages.ASSET_REVOKE -> "MESSAGE_ASSET_REVOKE",
    schema.constants.Messages.ASSET_SEND -> "MESSAGE_ASSET_SEND",
    schema.constants.Messages.ASSET_WRAP -> "MESSAGE_ASSET_WRAP",
    schema.constants.Messages.ASSET_UNWRAP -> "MESSAGE_ASSET_UNWRAP",
    //identities
    schema.constants.Messages.IDENTITY_DEFINE -> "MESSAGE_IDENTITY_DEFINE",
    schema.constants.Messages.IDENTITY_DEPUTIZE -> "MESSAGE_IDENTITY_DEPUTIZE",
    schema.constants.Messages.IDENTITY_ISSUE -> "MESSAGE_IDENTITY_ISSUE",
    schema.constants.Messages.IDENTITY_MUTATE -> "MESSAGE_IDENTITY_MUTATE",
    schema.constants.Messages.IDENTITY_NAME -> "MESSAGE_IDENTITY_NAME",
    schema.constants.Messages.IDENTITY_PROVISION -> "MESSAGE_IDENTITY_PROVISION",
    schema.constants.Messages.IDENTITY_QUASH -> "MESSAGE_IDENTITY_QUASH",
    schema.constants.Messages.IDENTITY_REVOKE -> "MESSAGE_IDENTITY_REVOKE",
    schema.constants.Messages.IDENTITY_UNPROVISION -> "MESSAGE_IDENTITY_UNPROVISION",
    schema.constants.Messages.IDENTITY_UPDATE -> "MESSAGE_IDENTITY_UPDATE",
    //orders
    schema.constants.Messages.ORDER_CANCEL -> "MESSAGE_ORDER_CANCEL",
    schema.constants.Messages.ORDER_DEFINE -> "MESSAGE_ORDER_DEFINE",
    schema.constants.Messages.ORDER_DEPUTIZE -> "MESSAGE_ORDER_DEPUTIZE",
    schema.constants.Messages.ORDER_IMMEDIATE -> "MESSAGE_ORDER_IMMEDIATE",
    schema.constants.Messages.ORDER_MAKE -> "MESSAGE_ORDER_MAKE",
    schema.constants.Messages.ORDER_MODIFY -> "MESSAGE_ORDER_MODIFY",
    schema.constants.Messages.ORDER_REVOKE -> "MESSAGE_ORDER_REVOKE",
    schema.constants.Messages.ORDER_TAKE -> "MESSAGE_ORDER_TAKE",
    schema.constants.Messages.ORDER_PUT -> "MESSAGE_ORDER_PUT",
    schema.constants.Messages.ORDER_GET -> "MESSAGE_ORDER_GET",
    //metas
    schema.constants.Messages.META_REVEAL -> "MESSAGE_META_REVEAL",
  )

}
