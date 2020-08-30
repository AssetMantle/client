package constants

object Module {
  //Master model
  val MASTER_ACCOUNT = "MASTER_ACCOUNT"
  val MASTER_ACCOUNT_FILE = "MASTER_ACCOUNT_FILE"
  val MASTER_ACCOUNT_KYC = "MASTER_ACCOUNT_KYC"
  val MASTER_EMAIL_ADDRESS = "MASTER_EMAIL_ADDRESS"
  val MASTER_IDENTIFICATION = "MASTER_IDENTIFICATION"
  val MASTER_MOBILE_NUMBER = "MASTER_MOBILE_NUMBER"
  val MASTER_ORGANIZATION_UBO = "MASTER_ORGANIZATION_UBO"

  val FILE_RESOURCE_MANAGER = "FILE_RESOURCE_MANAGER"
  val FILE_OPERATIONS = "FILE_OPERATIONS"
  val IMAGE_PROCESS = "IMAGE_PROCESS"
  val FILE_CONTROLLER = "FILE_CONTROLLER"
  val SFTP_SCHEDULER = "SFTP_SCHEDULER"
  val SERIALIZABLE = "SERIALIZABLE"
  val DATA = "DATA"
  val TRANSACTION_MESSAGE = "TRANSACTION_MESSAGE"

  val TRANSACTION_MESSAGE_RESPONSES = "TRANSACTION_MESSAGE_RESPONSES"

  //BlockchainTransaction model
  val BLOCKCHAIN_TRANSACTION_SEND_COIN = "BLOCKCHAIN_TRANSACTION_SEND_COIN"

  //Blockchain model
  val BLOCKCHAIN_ACCOUNT = "BLOCKCHAIN_ACCOUNT"
  val BLOCKCHAIN_ASSET = "BLOCKCHAIN_ASSET"
  val BLOCKCHAIN_CLASSIFICATION = "BLOCKCHAIN_CLASSIFICATION"
  val BLOCKCHAIN_IDENTITY = "BLOCKCHAIN_IDENTITY"
  val BLOCKCHAIN_AVERAGE_BLOCK_TIME = "BLOCKCHAIN_AVERAGE_BLOCK_TIME"
  val BLOCKCHAIN_ACCOUNT_BALANCE = "BLOCKCHAIN_ACCOUNT_BALANCE"
  val BLOCKCHAIN_BLOCK = "BLOCKCHAIN_BLOCK"
  val BLOCKCHAIN_DELEGATION = "BLOCKCHAIN_DELEGATION"
  val BLOCKCHAIN_MAINTAINER = "BLOCKCHAIN_MAINTAINER"
  val BLOCKCHAIN_ORDER = "BLOCKCHAIN_ORDER"
  val BLOCKCHAIN_REDELEGATION = "BLOCKCHAIN_REDELEGATION"
  val BLOCKCHAIN_SIGNING_INFO = "BLOCKCHAIN_SIGNING_INFO"
  val BLOCKCHAIN_TRANSACTION = "BLOCKCHAIN_TRANSACTION"
  val BLOCKCHAIN_TOKEN = "BLOCKCHAIN_TOKEN"
  val BLOCKCHAIN_SPLIT = "BLOCKCHAIN_SPLIT"
  val BLOCKCHAIN_META = "BLOCKCHAIN_META"
  val BLOCKCHAIN_UNDELEGATION = "BLOCKCHAIN_UNDELEGATION"
  val BLOCKCHAIN_VALIDATOR = "BLOCKCHAIN_VALIDATOR"
  val BLOCKCHAIN_WITHDRAW_ADDRESS = "BLOCKCHAIN_WITHDRAW_ADDRESS"
  val BLOCKCHAIN_VALIDATOR_REWARD = "BLOCKCHAIN_VALIDATOR_REWARD"

  //Transactions
  val TRANSACTIONS_ADD_KEY = "TRANSACTIONS_ADD_KEY"
  val TRANSACTIONS_CHANGE_PASSWORD = "TRANSACTIONS_CHANGE_PASSWORD"
  val TRANSACTIONS_FORGOT_PASSWORD = "TRANSACTIONS_FORGOT_PASSWORD"
  val TRANSACTIONS_MEMBER_CHECK_MEMBER_SCAN = "TRANSACTIONS_MEMBER_CHECK_MEMBER_SCAN"
  val TRANSACTIONS_MEMBER_CHECK_MEMBER_SCAN_RESULT_DECISION = "TRANSACTIONS_MEMBER_CHECK_MEMBER_SCAN_RESULT_DECISION"
  val TRANSACTIONS_MEMBER_CHECK_CORPORATE_SCAN = "TRANSACTIONS_MEMBER_CHECK_CORPORATE_SCAN"
  val TRANSACTIONS_MEMBER_CHECK_CORPORATE_SCAN_RESULT_DECISION = "TRANSACTIONS_MEMBER_CHECK_CORPORATE_SCAN_RESULT_DECISION"
  val TRANSACTIONS_SEND_COIN = "TRANSACTIONS_SEND_COIN"
  val TRANSACTIONS_TRULIOO_VERIFY = "TRANSACTIONS_TRULIOO_VERIFY"
  val TRANSACTIONS_DOCUSIGN_REGENERATE_TOKEN = "TRANSACTIONS_DOCUSIGN_REGENERATE_TOKEN"

  //QUERIES
  val QUERIES_GET_ACCOUNT = "QUERIES_GET_ACCOUNT"
  val QUERIES_GET_ACCOUNT_BALANCE = "QUERIES_GET_ACCOUNT_BALANCE"
  val QUERIES_GET_ALL_SIGNING_INFOS = "QUERIES_GET_ALL_SIGNING_INFOS"
  val QUERIES_GET_ALL_VALIDATOR_DELEGATIONS = "QUERIES_GET_ALL_VALIDATOR_DELEGATIONS"
  val QUERIES_GET_ALL_VALIDATOR_UNDELEGATIONS = "QUERIES_GET_ALL_VALIDATOR_UNDELEGATIONS"
  val QUERIES_GET_ASSET = "QUERIES_GET_ASSET"
  val QUERIES_GET_BLOCK = "QUERIES_GET_BLOCK"
  val QUERIES_GET_BLOCK_COMMIT = "QUERIES_GET_BLOCK_COMMIT"
  val QUERIES_GET_BONDED_VALIDATORS = "QUERIES_GET_BONDED_VALIDATORS"
  val QUERIES_GET_CLASSIFICATION = "QUERIES_GET_CLASSIFICATION"
  val QUERIES_GET_DELEGATOR_REWARDS = "QUERIES_GET_DELEGATOR_REWARDS"
  val QUERIES_GET_GENESIS = "QUERIES_GET_GENESIS"
  val QUERIES_GET_IDENTITY = "QUERIES_GET_IDENTITY"
  val QUERIES_GET_MEMBER_CHECK_MEMBER_SCAN = "QUERIES_GET_MEMBER_CHECK_MEMBER_SCAN"
  val QUERIES_GET_MEMBER_CHECK_MEMBER_SCAN_RESULT = "QUERIES_GET_MEMBER_CHECK_MEMBER_SCAN_RESULT"
  val QUERIES_GET_MEMBER_CHECK_CORPORATE_SCAN = "QUERIES_GET_MEMBER_CHECK_CORPORATE_SCAN"
  val QUERIES_GET_MEMBER_CHECK_CORPORATE_SCAN_RESULT = "QUERIES_GET_MEMBER_CHECK_CORPORATE_SCAN_RESULT"
  val QUERIES_GET_TRANSACTION_HASH = "QUERIES_GET_TRANSACTION_HASH"
  val QUERIES_GET_RESPONSE = "QUERIES_GET_RESPONSE"
  val QUERIES_GET_MNEMONIC = "QUERIES_GET_MNEMONIC"
  val QUERIES_GET_ORDER = "QUERIES_GET_ORDER"
  val QUERIES_GET_SIGNING_INFO = "QUERIES_GET_SIGNING_INFO"
  val QUERIES_GET_SPLIT = "QUERIES_GET_SPLIT"
  val QUERIES_GET_META = "QUERIES_GET_META"
  val QUERIES_GET_TRANSACTION = "QUERIES_GET_TRANSACTION"
  val QUERIES_GET_TRANSACTION_BY_HEIGHT = "QUERIES_GET_TRANSACTION_BY_HEIGHT"
  val QUERIES_GET_UNBONDED_VALIDATORS = "QUERIES_GET_UNBONDED_VALIDATORS"
  val QUERIES_GET_UNBONDING_VALIDATORS = "QUERIES_GET_UNBONDING_VALIDATORS"
  val QUERIES_GET_VALIDATOR = "QUERIES_GET_VALIDATOR"
  val QUERIES_GET_VALIDATOR_DELEGATOR_DELEGATIONS = "QUERIES_GET_VALIDATOR_DELEGATOR_DELEGATIONS"
  val QUERIES_GET_VALIDATOR_DELEGATOR_REDELEGATIONS = "QUERIES_GET_VALIDATOR_DELEGATOR_REDELEGATIONS"
  val QUERIES_GET_VALIDATOR_DELEGATOR_UNDELEGATIONS = "QUERIES_GET_VALIDATOR_DELEGATOR_UNDELEGATIONS"
  val QUERIES_GET_DISTRIBUTION_REWARDS = "QUERIES_GET_DISTRIBUTION_REWARDS"
  val QUERIES_GET_VALIDATOR_KEY_BASE_ACCOUNT = "QUERIES_GET_VALIDATOR_KEY_BASE_ACCOUNT"
  val QUERIES_GET_VALIDATOR_OUTSTANDING_REWARDS = "QUERIES_GET_VALIDATOR_OUTSTANDING_REWARDS"
  val QUERIES_GET_BAND_ORACLE_SCRIPT = "QUERIES_GET_BAND_ORACLE_SCRIPT"

  val QUERIES_GET_TRANSACTION_HASH_RESPONSE = "QUERIES_GET_TRANSACTION_HASH_RESPONSE"
  val QUERIES_GET_TRULIOO_COUNTRY_CODES = "QUERIES_GET_TRULIOO_COUNTRY_CODES"
  val QUERIES_GET_TRULIOO_AUTHENTICATION = "QUERIES_GET_TRULIOO_AUTHENTICATION"
  val QUERIES_GET_TRULIOO_ENTITIES = "QUERIES_GET_TRULIOO_ENTITIES"
  val QUERIES_GET_TRULIOO_FIELDS = "QUERIES_GET_TRULIOO_FIELDS"
  val QUERIES_GET_TRULIOO_RECOMMENDED_FIELDS = "QUERIES_GET_TRULIOO_RECOMMENDED_FIELDS"
  val QUERIES_GET_TRULIOO_CONSENTS = "QUERIES_GET_TRULIOO_CONSENTS"
  val QUERIES_GET_TRULIOO_DETAILED_CONSENTS = "QUERIES_GET_TRULIOO_DETAILED_CONSENTS"
  val QUERIES_GET_TRULIOO_COUNTRY_SUBDIVISIONS = "QUERIES_GET_TRULIOO_COUNTRY_SUBDIVISIONS"
  val QUERIES_GET_TRULIOO_DATA_SOURCES = "QUERIES_GET_TRULIOO_DATA_SOURCES"
  val QUERIES_GET_TRULIOO_TRANSACTION_RECORD = "QUERIES_GET_TRULIOO_TRANSACTION_RECORD"
  val QUERIES_GET_ABCI_INFO = "QUERIES_GET_ABCI_INFO"
  val QUERIES_GET_TOTAL_SUPPLY = "QUERIES_GET_TOTAL_SUPPLY"
  val QUERIES_GET_STAKING_POOL = "QUERIES_GET_STAKING_POOL"
  val QUERIES_GET_MINTING_INFLATION = "QUERIES_GET_MINTING_INFLATION"
  val QUERIES_GET_COMMUNITY_POOL = "QUERIES_GET_COMMUNITY_POOL"

  val QUERIES_RESPONSE_DATA = "QUERIES_RESPONSE_DATA"

  //MasterTransactions
  val MASTER_TRANSACTION_SESSION_TOKEN = "MASTER_TRANSACTION_SESSION_TOKEN"
  val MASTER_TRANSACTION_EMAIL_OTP = "MASTER_TRANSACTION_EMAIL_OTP"
  val MASTER_TRANSACTION_NOTIFICATION = "MASTER_TRANSACTION_NOTIFICATION"
  val MASTER_TRANSACTION_EVENT = "MASTER_TRANSACTION_EVENT"
  val MASTER_TRANSACTION_PUSH_NOTIFICATION_TOKEN = "MASTER_TRANSACTION_PUSH_NOTIFICATION_TOKEN"
  val MASTER_TRANSACTION_SMS_OTP = "MASTER_TRANSACTION_SMS_OTP"
  val MASTER_TRANSACTION_TRADE_TERM = "MASTER_TRANSACTION_TRADE_TERMS"
  val MASTER_TRANSACTION_CHAT = "MASTER_TRANSACTION_CHAT"
  val MASTER_TRANSACTION_MESSAGE = "MASTER_TRANSACTION_MESSAGE"
  val MASTER_TRANSACTION_MESSAGE_READ = "MASTER_TRANSACTION_MESSAGE_READ"
  val MASTER_TRANSACTION_TOKEN_PRICE = "MASTER_TRANSACTION_TOKEN_PRICE"

  //Western Union
  val WESTERN_UNION_RTCB = "WESTERN_UNION_RTCB"
  val WESTERN_UNION_SFTP_FILE_TRANSACTION = "WESTERN_UNION_SFTP_FILE_TRANSACTION"
  val WESTERN_UNION_FIAT_REQUEST = "WESTERN_UNION_FIAT_REQUEST"

  //Docusign
  val DOCUSIGN_ENVELOPE = "DOCUSIGN_ENVELOPE"
  val DOCUSIGN_ENVELOPE_HISTORY = "DOCUSIGN_ENVELOPE_HISTORY"
  val DOCUSIGN_OAUTH_TOKEN = "DOCUSIGN_OAUTH_TOKEN"

  //Member Check
  val MEMBER_CHECK_MEMBER_SCAN = "MEMBER_CHECK_MEMBER_SCAN"
  val MEMBER_CHECK_MEMBER_SCAN_DECISION = "MEMBER_CHECK_MEMBER_SCAN_DECISION"
  val MEMBER_CHECK_CORPORATE_SCAN = "MEMBER_CHECK_CORPORATE_SCAN"
  val MEMBER_CHECK_CORPORATE_SCAN_DECISION = "MEMBER_CHECK_CORPORATE_SCAN_DECISION"
  val MEMBER_CHECK_VESSEL_SCAN = "MEMBER_CHECK_VESSEL_SCAN"
  val MEMBER_CHECK_VESSEL_SCAN_DECISION = "MEMBER_CHECK_VESSEL_SCAN_DECISION"
  val MEMBER_CHECK_VESSEL_SCAN_DECISION_HISTORY = "MEMBER_CHECK_VESSEL_SCAN_DECISION_HISTORY"

  //Controllers
  val CONTROLLERS_SIGN_UP = "CONTROLLERS_SIGN_UP"
  val CONTROLLERS_LOGIN = "CONTROLLERS_LOGIN"
  val CONTROLLERS_LOGOUT = "CONTROLLERS_LOGOUT"
  val CONTROLLERS_NOTIFICATION = "CONTROLLERS_NOTIFICATION"
  val CONTROLLERS_ACCOUNT = "CONTROLLERS_ACCOUNT"
  val CONTROLLERS_SEND_COIN = "CONTROLLERS_SEND_COIN"
  val CONTROLLERS_CONTACT = "CONTROLLERS_CONTACT"
  val CONTROLLERS_INDEX = "CONTROLLERS_INDEX"
  val CONTROLLERS_VIEW = "CONTROLLERS_VIEW"
  val CONTROLLERS_WESTERN_UNION = "CONTROLLERS_WESTERN_UNION"
  val CONTROLLERS_CHAT = "CONTROLLERS_CHAT"
  val CONTROLLERS_BACKGROUND_CHECK = "CONTROLLERS_BACKGROUND_CHECK"
  val CONTROLLERS_COMPONENT_VIEW = "CONTROLLERS_COMPONENT_VIEW"
  val CONTROLLERS_DOCUSIGN = "CONTROLLERS_DOCUSIGN"
  val CONTROLLERS_WEB_SOCKET = "CONTROLLERS_WEB_SOCKET"

  //Actions
  val ACTIONS_WITH_LOGIN_ACTION = "ACTIONS_WITH_LOGIN_ACTION"
  val ACTIONS_WITH_UNKNOWN_LOGIN_ACTION = "ACTIONS_WITH_UNKNOWN_LOGIN_ACTION"
  val ACTIONS_WITH_USER_LOGIN_ACTION = "ACTIONS_WITH_USER_LOGIN_ACTION"

  //Utilities
  val UTILITIES_TRANSACTION = "UTILITIES_TRANSACTION"
  val UTILITIES_NOTIFICATION = "UTILITIES_NOTIFICATION"
  val UTILITIES_DOCUSIGN = "UTILITIES_DOCUSIGN"
  val UTILITIES_KEY_STORE = "UTILITIES_KEY_STORE"
  val UTILITIES_DATE = "UTILITIES_DATE"
  val UTILITIES_MICRO_NUMBER = "UTILITIES_MICRO_NUMBER"

  //actors
  val ACTOR_COMET = "ACTOR_COMET"
  val ACTOR_SERVICE = "ACTOR_SERVICE"
  val ACTOR_EMAIL = "ACTOR_EMAIL"
  val ACTOR_SMS = "ACTOR_SMS"
  val ACTOR_PUSH_NOTIFICATION = "ACTOR_PUSH_NOTIFICATION"

  val SERVICES_BLOCK = "SERVICES_BLOCK"
  val SERVICES_STARTUP = "SERVICES_STARTUP"
}
