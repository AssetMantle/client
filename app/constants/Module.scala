package constants

object Module {
  //Master model
  val MASTER_ACCOUNT = "MASTER_ACCOUNT"
  val MASTER_ACCOUNT_KYC = "MASTER_ACCOUNT_KYC"
  val MASTER_ASSET = "MASTER_ASSET"
  val MASTER_CONTACT = "MASTER_CONTACT"
  val MASTER_NEGOTIATION = "MASTER_NEGOTIATION"
  val MASTER_ORGANIZATION = "MASTER_ORGANIZATION"
  val MASTER_ORGANIZATION_BANK_ACCOUNT = "MASTER_ORGANIZATION_BANK_ACCOUNT"
  val MASTER_ORGANIZATION_KYC = "MASTER_ORGANIZATION_KYC"
  val MASTER_ORGANIZATION_BACKGROUND_CHECK = "MASTER_ORGANIZATION_BACKGROUND_CHECK"
  val MASTER_TRADER = "MASTER_TRADER"
  val MASTER_TRADER_BACKGROUND_CHECK = "MASTER_TRADER_BACKGROUND_CHECK"
  val MASTER_TRADER_KYC = "MASTER_TRADER_KYC"
  val MASTER_ZONE = "MASTER_ZONE"
  val MASTER_ZONE_KYC = "MASTER_ZONE_KYC"
  val MASTER_ACCOUNT_FILE = "MASTER_ACCOUNT_FILE"
  val MASTER_TRADE_ROOM = "MASTER_TRADE_ROOM"
  val MASTER_IDENTIFICATION = "MASTER_IDENTIFICATION"
  val MASTER_TRADER_RELATION = "MASTER_TRADER_RELATION"

  val FILE_RESOURCE_MANAGER = "FILE_RESOURCE_MANAGER"
  val FILE_OPERATIONS = "FILE_OPERATIONS"
  val IMAGE_PROCESS = "IMAGE_PROCESS"
  val FILE_CONTROLLER = "FILE_CONTROLLER"
  val SFTP_SCHEDULER = "SFTP_SCHEDULER"

  //BlockchainTransaction model
  val BLOCKCHAIN_TRANSACTION_ADD_ORGANIZATION = "BLOCKCHAIN_TRANSACTION_ADD_ORGANIZATION"
  val BLOCKCHAIN_TRANSACTION_ADD_ZONE = "BLOCKCHAIN_TRANSACTION_ADD_ZONE"
  val BLOCKCHAIN_TRANSACTION_BUYER_EXECUTE_ORDER = "BLOCKCHAIN_TRANSACTION_BUYER_EXECUTE_ORDER"
  val BLOCKCHAIN_TRANSACTION_CHANGE_BUYER_BID = "BLOCKCHAIN_TRANSACTION_CHANGE_BUYER_BID"
  val BLOCKCHAIN_TRANSACTION_CHANGE_SELLER_BID = "BLOCKCHAIN_TRANSACTION_CHANGE_SELLER_BID"
  val BLOCKCHAIN_TRANSACTION_CONFIRM_BUYER_BID = "BLOCKCHAIN_TRANSACTION_CONFIRM_BUYER_BID"
  val BLOCKCHAIN_TRANSACTION_CONFIRM_SELLER_BID = "BLOCKCHAIN_TRANSACTION_CONFIRM_SELLER_BID"
  val BLOCKCHAIN_TRANSACTION_ISSUE_ASSET = "BLOCKCHAIN_TRANSACTION_BUYER_ISSUE_ASSET"
  val BLOCKCHAIN_TRANSACTION_ISSUE_FIAT = "BLOCKCHAIN_TRANSACTION_ISSUE_FIAT"
  val BLOCKCHAIN_TRANSACTION_REDEEM_ASSET = "BLOCKCHAIN_TRANSACTION_REDEEM_ASSET"
  val BLOCKCHAIN_TRANSACTION_REDEEM_FIAT = "BLOCKCHAIN_TRANSACTION_REDEEM_FIAT"
  val BLOCKCHAIN_TRANSACTION_RELEASE_ASSET = "BLOCKCHAIN_TRANSACTION_RELEASE_ASSET"
  val BLOCKCHAIN_TRANSACTION_SELLER_EXECUTE_ORDER = "BLOCKCHAIN_TRANSACTION_SELLER_EXECUTE_ORDER"
  val BLOCKCHAIN_TRANSACTION_SEND_ASSET = "BLOCKCHAIN_TRANSACTION_BUYER_SEND_ASSET"
  val BLOCKCHAIN_TRANSACTION_SEND_COIN = "BLOCKCHAIN_TRANSACTION_SEND_COIN"
  val BLOCKCHAIN_TRANSACTION_SEND_FIAT = "BLOCKCHAIN_TRANSACTION_BUYER_SEND_FIAT"
  val BLOCKCHAIN_TRANSACTION_SET_ACL = "BLOCKCHAIN_TRANSACTION_BUYER_SET_ACL"
  val BLOCKCHAIN_TRANSACTION_SET_BUYER_FEEDBACK = "BLOCKCHAIN_TRANSACTION_SET_BUYER_FEEDBACK"
  val BLOCKCHAIN_TRANSACTION_SET_SELLER_FEEDBACK = "BLOCKCHAIN_TRANSACTION_SET_SELLER_FEEDBACK"

  //Blockchain model
  val BLOCKCHAIN_ACCOUNT = "BLOCKCHAIN_ACCOUNT"
  val BLOCKCHAIN_ACL_ACCOUNT = "BLOCKCHAIN_ACL_ACCOUNT"
  val BLOCKCHAIN_ACL_HASH = "BLOCKCHAIN_ACL_HASH"
  val BLOCKCHAIN_ASSET = "BLOCKCHAIN_ASSET"
  val BLOCKCHAIN_FIAT = "BLOCKCHAIN_FIAT"
  val BLOCKCHAIN_NEGOTIATION = "BLOCKCHAIN_NEGOTIATION"
  val BLOCKCHAIN_ORDER = "BLOCKCHAIN_ORDER"
  val BLOCKCHAIN_ORGANIZATION = "BLOCKCHAIN_ORGANIZATION"
  val BLOCKCHAIN_OWNER = "BLOCKCHAIN_OWNER"
  val BLOCKCHAIN_ZONE = "BLOCKCHAIN_ZONE"
  val BLOCKCHAIN_TRADER_FEEDBACK_HISTORY = "BLOCKCHAIN_TRADER_FEEDBACK_HISTORY"
  val BLOCKCHAIN_TRANSACTION_FEEDBACK = "BLOCKCHAIN_TRANSACTION_FEEDBACK"

  //Transactions
  val TRANSACTIONS_ADD_KEY = "TRANSACTIONS_ADD_KEY"
  val TRANSACTIONS_ADD_ORGANIZATION = "TRANSACTIONS_ADD_ORGANIZATION"
  val TRANSACTIONS_ADD_ZONE = "TRANSACTIONS_ADD_ZONE"
  val TRANSACTIONS_CHANGE_PASSWORD = "TRANSACTIONS_CHANGE_PASSWORD"
  val TRANSACTIONS_FORGOT_PASSWORD = "TRANSACTIONS_FORGOT_PASSWORD"
  val TRANSACTIONS_BUYER_EXECUTE_ORDER = "TRANSACTIONS_BUYER_EXECUTE_ORDER"
  val TRANSACTIONS_CHANGE_BUYER_BID = "TRANSACTIONS_CHANGE_BUYER_BID"
  val TRANSACTIONS_CHANGE_SELLER_BID = "TRANSACTIONS_CHANGE_SELLER_BID"
  val TRANSACTIONS_CONFIRM_BUYER_BID = "TRANSACTIONS_CONFIRM_BUYER_BID"
  val TRANSACTIONS_CONFIRM_SELLER_BID = "TRANSACTIONS_CONFIRM_SELLER_BID"
  val TRANSACTIONS_ISSUE_ASSET = "TRANSACTIONS_ISSUE_ASSET"
  val MASTER_TRANSACTION_ISSUE_ASSET_REQUEST = "MASTER_TRANSACTION_ISSUE_ASSET_REQUEST"
  val TRANSACTIONS_ISSUE_FIAT = "TRANSACTIONS_ISSUE_FIAT"
  val TRANSACTIONS_REDEEM_ASSET = "TRANSACTIONS_REDEEM_ASSET"
  val TRANSACTIONS_REDEEM_FIAT = "TRANSACTIONS_REDEEM_FIAT"
  val TRANSACTIONS_RELEASE_ASSET = "TRANSACTIONS_RELEASE_ASSET"
  val TRANSACTIONS_SELLER_EXECUTE_ORDER = "TRANSACTIONS_SELLER_EXECUTE_ORDER"
  val TRANSACTIONS_SEND_ASSET = "TRANSACTIONS_SEND_ASSET"
  val TRANSACTIONS_SEND_COIN = "TRANSACTIONS_SEND_COIN"
  val TRANSACTIONS_SEND_FIAT = "TRANSACTIONS_SEND_FIAT"
  val TRANSACTIONS_SET_ACL = "TRANSACTIONS_SET_ACL"
  val TRANSACTIONS_SET_BUYER_FEEDBACK = "TRANSACTIONS_SET_BUYER_FEEDBACK"
  val TRANSACTIONS_SET_SELLER_FEEDBACK = "TRANSACTIONS_SET_SELLER_FEEDBACK"
  val TRANSACTIONS_TRULIOO_VERIFY = "TRANSACTIONS_TRULIOO_VERIFY"

  //QUERIES
  val QUERIES_GET_ACCOUNT = "QUERIES_GET_ACCOUNT"
  val QUERIES_GET_FEEDBACK = "QUERIES_GET_FEEDBACK"
  val QUERIES_GET_BLOCK_DETAILS = "QUERIES_GET_BLOCK_DETAILS"
  val QUERIES_GET_ACL = "QUERIES_GET_ACL"
  val QUERIES_GET_ASSET = "QUERIES_GET_ASSET"
  val QUERIES_GET_FIAT = "QUERIES_GET_FIAT"
  val QUERIES_GET_NEGOTIATION = "QUERIES_GET_NEGOTIATION"
  val QUERIES_GET_ORDER = "QUERIES_GET_ORDER"
  val QUERIES_GET_TRANSACTION_HASH = "QUERIES_GET_TRANSACTION_HASH"
  val QUERIES_GET_ORGANIZATION = "QUERIES_GET_ORGANIZATION"
  val QUERIES_GET_RESPONSE = "QUERIES_GET_RESPONSE"
  val QUERIES_GET_MNEMONIC = "QUERIES_GET_MNEMONIC"
  val QUERIES_GET_TRADER_REPUTATION = "QUERIES_GET_TRADER_REPUTATION"
  val QUERIES_GET_ZONE = "QUERIES_GET_ZONE"
  val QUERIES_GET__TRANSACTION_HASH_RESPONSE = "QUERIES_GET__TRANSACTION_HASH_RESPONSE"
  val QUERIES_GET_TRULIOO_COUNTRY_CODES = "QUERIES_GET_TRULIOO_COUNTRY_CODES"
  val QUERIES_GET_TRULIOO_AUTHENTICATION = "QUERIES_GET_TRULIOO_AUTHENTICATION"
  val QUERIES_GET_TRULIOO_ENTITIES = "QUERIES_GET_TRULIOO_ENTITIES"
  val QUERIES_GET_TRULIOO_FIELDS= "QUERIES_GET_TRULIOO_FIELDS"
  val QUERIES_GET_TRULIOO_RECOMMENDED_FIELDS = "QUERIES_GET_TRULIOO_RECOMMENDED_FIELDS"
  val QUERIES_GET_TRULIOO_CONSENTS = "QUERIES_GET_TRULIOO_CONSENTS"
  val QUERIES_GET_TRULIOO_DETAILED_CONSENTS = "QUERIES_GET_TRULIOO_DETAILED_CONSENTS"
  val QUERIES_GET_TRULIOO_COUNTRY_SUBDIVISIONS = "QUERIES_GET_TRULIOO_COUNTRY_SUBDIVISIONS"
  val QUERIES_GET_TRULIOO_DATA_SOURCES = "QUERIES_GET_TRULIOO_DATA_SOURCES"
  val QUERIES_GET_TRULIOO_TRANSACTION_RECORD = "QUERIES_GET_TRULIOO_TRANSACTION_RECORD"
  val QUERIES_GET_ABCI_INFO = "QUERIES_GET_ABCI_INFO"

  //MasterTransactions
  val MASTER_TRANSACTION_SESSION_TOKEN = "MASTER_TRANSACTION_SESSION_TOKEN"
  val MASTER_TRANSACTION_ADD_TRADER_REQUEST = "MASTER_TRANSACTION_ADD_TRADER_REQUEST"
  val MASTER_TRANSACTION_TRADER_INVITATION = "MASTER_TRANSACTION_TRADER_INVITATION"
  val MASTER_TRANSACTION_ASSET_FILE = "MASTER_TRANSACTION_ASSET_FILE"
  val MASTER_TRANSACTION_EMAIL_OTP = "MASTER_TRANSACTION_EMAIL_OTP"
  val MASTER_TRANSACTION_FAUCET_REQUEST = "MASTER_TRANSACTION_FAUCET_REQUEST"
  val MASTER_TRANSACTION_ISSUE_ASSET = "MASTER_TRANSACTION_ISSUE_ASSET"
  val MASTER_TRANSACTION_ISSUE_FIAT_REQUEST = "MASTER_TRANSACTION_ISSUE_FIAT_REQUEST"
  val MASTER_TRANSACTION_NEGOTIATION_FILE = "MASTER_TRANSACTION_NEGOTIATION_FILE"
  val MASTER_TRANSACTION_NOTIFICATION = "MASTER_TRANSACTION_NOTIFICATION"
  val MASTER_TRANSACTION_PUSH_NOTIFICATION_TOKEN = "MASTER_TRANSACTION_PUSH_NOTIFICATION_TOKEN"
  val MASTER_TRANSACTION_SMS_OTP = "MASTER_TRANSACTION_SMS_OTP"
  val MASTER_TRANSACTION_TRADE_TERM = "MASTER_TRANSACTION_TRADE_TERMS"
  val MASTER_TRANSACTION_WURTCB_REQUEST = "MASTER_TRANSACTION_WURTCB_REQUEST"
  val MASTER_TRANSACTION_WU_SFTP_FILE_TRANSACTION = "MASTER_TRANSACTION_WU_SFTP_FILE_TRANSACTION"
  val MASTER_TRANSACTION_CHAT = "MASTER_TRANSACTION_CHAT"
  val MASTER_TRANSACTION_MESSAGE = "MASTER_TRANSACTION_MESSAGE"
  val MASTER_TRANSACTION_MESSAGE_READ = "MASTER_TRANSACTION_MESSAGE_READ"
  val MASTER_TRANSACTION_TRADE_ACTIVITY = "MASTER_TRANSACTION_TRADE_ACTIVITY"
  val MASTER_TRANSACTION_ZONE_INVITATION = "MASTER_TRANSACTION_ZONE_INVITATION"

  //Controllers
  val CONTROLLERS_SIGN_UP = "CONTROLLERS_SIGN_UP"
  val CONTROLLERS_ADD_ORGANIZATION = "CONTROLLERS_ADD_ORGANIZATION"
  val CONTROLLERS_ADD_ZONE = "CONTROLLERS_ADD_ZONE"
  val CONTROLLERS_LOGIN = "CONTROLLERS_LOGIN"
  val CONTROLLERS_LOGOUT = "CONTROLLERS_LOGOUT"
  val CONTROLLERS_EMAIL = "CONTROLLERS_EMAIL"
  val CONTROLLERS_NOTIFICATION = "CONTROLLERS_NOTIFICATION"
  val CONTROLLERS_SMS = "CONTROLLERS_SMS"
  val CONTROLLERS_ACCOUNT = "CONTROLLERS_ACCOUNT"
  val CONTROLLERS_ASSET = "CONTROLLERS_ASSET"
  val CONTROLLERS_TRADER = "CONTROLLERS_TRADER"
  val CONTROLLERS_BUYER_EXECUTE_ORDER = "CONTROLLERS_BUYER_EXECUTE_ORDER"
  val CONTROLLERS_SELLER_EXECUTE_ORDER = "CONTROLLERS_SELLER_EXECUTE_ORDER"
  val CONTROLLERS_CHANGE_BUYER_BID = "CONTROLLERS_CHANGE_BUYER_BID"
  val CONTROLLERS_CHANGE_SELLER_BID = "CONTROLLERS_CHANGE_SELLER_BID"
  val CONTROLLERS_CONFIRM_BUYER_BID = "CONTROLLERS_CONFIRM_BUYER_BID"
  val CONTROLLERS_CONFIRM_SELLER_BID = "CONTROLLERS_CONFIRM_SELLER_BID"
  val CONTROLLERS_ISSUE_ASSET = "CONTROLLERS_ISSUE_ASSET"
  val CONTROLLERS_ISSUE_FIAT = "CONTROLLERS_ISSUE_FIAT"
  val CONTROLLERS_REDEEM_ASSET = "CONTROLLERS_REDEEM_ASSET"
  val CONTROLLERS_REDEEM_FIAT = "CONTROLLERS_REDEEM_FIAT"
  val CONTROLLERS_RELEASE_ASSET = "CONTROLLERS_RELEASE_ASSET"
  val CONTROLLERS_SEND_ASSET = "CONTROLLERS_SEND_ASSET"
  val CONTROLLERS_SEND_COIN = "CONTROLLERS_SEND_COIN"
  val CONTROLLERS_SEND_FIAT = "CONTROLLERS_SEND_FIAT"
  val CONTROLLERS_SET_ACL = "CONTROLLERS_SET_ACL"
  val CONTROLLERS_SET_BUYER_FEEDBACK = "CONTROLLERS_SET_BUYER_FEEDBACK"
  val CONTROLLERS_SET_SELLER_FEEDBACK = "CONTROLLERS_SET_SELLER_FEEDBACK"
  val CONTROLLERS_CONTACT = "CONTROLLERS_CONTACT"
  val CONTROLLERS_INDEX = "CONTROLLERS_INDEX"
  val CONTROLLERS_WESTERN_UNION = "CONTROLLERS_WESTERN_UNION"
  val CONTROLLERS_PROFILE = "CONTROLLERS_PROFILE"
  val CONTROLLERS_NEGOTIATION = "CONTROLLERS_NEGOTIATION"
  val CONTROLLERS_CHAT = "CONTROLLERS_CHAT"
  val CONTROLLERS_BACKGROUND_CHECK = "CONTROLLERS_BACKGROUND_CHECK"
  val CONTROLLERS_COMPONENT_VIEW = "CONTROLLERS_COMPONENT_VIEW"

  //Actions
  val ACTIONS_WITH_LOGIN_ACTION = "ACTIONS_WITH_LOGIN_ACTION"
  val ACTIONS_WITH_UNKNOWN_LOGIN_ACTION = "ACTIONS_WITH_UNKNOWN_LOGIN_ACTION"
  val ACTIONS_WITH_GENESIS_LOGIN_ACTION = "ACTIONS_WITH_GENESIS_LOGIN_ACTION"
  val ACTIONS_WITH_ORGANIZATION_LOGIN_ACTION = "ACTIONS_WITH_ORGANIZATION_LOGIN_ACTION"
  val ACTIONS_WITH_USER_LOGIN_ACTION = "ACTIONS_WITH_USER_LOGIN_ACTION"
  val ACTIONS_WITH_TRADER_LOGIN_ACTION = "ACTIONS_WITH_TRADER_LOGIN_ACTION"
  val ACTIONS_WITH_ZONE_LOGIN_ACTION = "ACTIONS_WITH_ZONE_LOGIN_ACTION"

  //Utilities
  val UTILITIES_TRANSACTION = "UTILITIES_TRANSACTION"
  val UTILITIES_NOTIFICATION = "UTILITIES_NOTIFICATION"

  //actors
  val ACTOR_COMET = "ACTOR_COMET"
  val ACTOR_SERVICE = "ACTOR_SERVICE"
}
