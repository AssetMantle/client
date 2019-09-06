package constants

object Form {

  val INDEX = "INDEX"
  val PROFILE = "PROFILE"
  val MARKET = "MARKET"
  val REQUEST = "REQUEST"
  val ACCEPT_OFFER = "ACCEPT_OFFER"
  val ASSETS="ASSETS"
  val BLOCKS = "BLOCKS"
  val BLOCK = "BLOCK"
  val BUY = "BUY"
  val NEXT = "NEXT"
  val BLOCK_TIMES = "BLOCK_TIMES"
  val SEARCH = "SEARCH"
  val PREVIOUS = "PREVIOUS"
  val ORGANIZATION_VERIFICATION = "ORGANIZATION_VERIFICATION"
  val ZONE_VERIFICATION = "ZONE_VERIFICATION"
  val EVIDENCE_HASH = "EVIDENCE_HASH"
  val SEE_ALL = "SEE_ALL"
  val ACL = "ACL"
  val LATEST_BLOCK_HEIGHT = "LATEST_BLOCK_HEIGHT"
  val AVERAGE_BLOCK_TIME = "AVERAGE_BLOCK_TIME"
  val BACK = "BACK"
  val INVALID_BLOCK_HEIGHT = "INVALID_BLOCK_HEIGHT"
  val INVALID_TRANSACTION_HASH = "INVALID_TRANSACTION_HASH"
  val LOAD_MORE_NOTIFICATIONS = "LOAD_MORE_NOTIFICATIONS"
  val ORGANIZATIONS = "ORGANIZATIONS"
  val ZONES = "ZONES"
  val TRADERS = "TRADERS"
  val INBOX = "INBOX"
  val NEGOTIATE = "NEGOTIATE"
  val ON_GOING_NEGOTIATIONS = "ON_GOING_NEGOTIATIONS"
  val ON_GOING_ORDERS = "ON_GOING_ORDERS"
  val SHOW_HIDE = "SHOW_HIDE"
  val SUBMIT_FEEDBACK = "SUBMIT_FEEDBACK"
  val BUY_ORDER_FEEDBACKS = "BUY_ORDER_FEEDBACKS"
  val SELL_ORDER_FEEDBACKS = "SELL_ORDER_FEEDBACKS"
  val SELL = "SELL"
  val PROFILE_PICTURE = "PROFILE_PICTURE"
  val CHANGE_PASSWORD = "CHANGE_PASSWORD"
  val FORGOT_PASSWORD = "FORGOT_PASSWORD"
  val GET_OTP = "GET_OTP"
  val SET_PASSWORD = "SET_PASSWORD"

  //File Upload
  val BROWSE = "BROWSE"
  val OR = "OR"
  val DROP_FILE = "DROP_FILE"
  val UPLOAD = "UPLOAD"
  val UPDATE = "UPDATE"
  val DOCUMENTS = "DOCUMENTS"
  val UPLOAD_BANK_DETAILS = "UPLOAD_BANK_DETAILS"
  val UPLOAD_IDENTIFICATION = "UPLOAD_IDENTIFICATION"
  val UPDATE_BANK_DETAILS = "UPDATE_BANK_DETAILS"
  val UPDATE_IDENTIFICATION = "UPDATE_IDENTIFICATION"
  val UPLOAD_ZONE_BANK_DETAILS = "UPLOAD_ZONE_BANK_DETAILS"
  val UPLOAD_ZONE_IDENTIFICATION = "UPLOAD_ZONE_IDENTIFICATION"
  val UPLOAD_ORGANIZATION_BANK_DETAILS = "UPLOAD_ORGANIZATION_BANK_DETAILS"
  val UPLOAD_ORGANIZATION_IDENTIFICATION = "UPLOAD_ORGANIZATION_IDENTIFICATION"
  val UPLOAD_TRADER_IDENTIFICATION = "UPLOAD_TRADER_IDENTIFICATION"
  val UPDATE_TRADER_IDENTIFICATION = "UPDATE_TRADER_IDENTIFICATION"
  val UPDATE_ZONE_BANK_DETAILS = "UPDATE_ZONE_BANK_DETAILS"
  val UPDATE_ZONE_IDENTIFICATION = "UPDATE_ZONE_IDENTIFICATION"
  val UPDATE_ORGANIZATION_BANK_DETAILS = "UPDATE_ORGANIZATION_BANK_DETAILS"
  val UPDATE_ORGANIZATION_IDENTIFICATION = "UPDATE_ORGANIZATION_IDENTIFICATION"

  val HEIGHT = "HEIGHT"
  val FEE = "FEE"
  val TRANSACTION_HASH = "TRANSACTION_HASH"
  val TYPE = "TYPE"
  val NUM_TXS = "NUM_TXS"
  val SEARCH_TX_HASH_HEIGHT = "SEARCH_TX_HASH_HEIGHT"
  val TRANSACTIONS = "TRANSACTIONS"
  val VALIDATORS = "VALIDATORS"
  val VALIDATORS_HASH = "VALIDATORS_HASH"
  val OPERATOR = "OPERATOR"
  val TOKENS = "TOKENS"
  val DELEGATOR_SHARES = "DELEGATOR_SHARES"

  //Blockchain
  val ADD_KEY = "ADD_KEY"
  val ADD_ZONE = "ADD_ZONE"
  val ADD_ORGANIZATION = "ADD_ORGANIZATION"
  val ADD_TRADER = "ADD_TRADER"
  val SEND_COIN = "SEND_COIN"
  val SET_ACL = "SET_ACL"
  val SET_BUYER_FEEDBACK = "SET_BUYER_FEEDBACK"
  val SET_SELLER_FEEDBACK = "SET_SELLER_FEEDBACK"
  val TAKER_ADDRESS = "TAKER_ADDRESS"
  val BLOCKCHAIN_ADDRESS = "BLOCKCHAIN_ADDRESS"
  val NAME = "NAME"
  val SEED = "SEED"
  val PASSWORD = "PASSWORD"
  val CONFIRM_PASSWORD="CONFIRM_PASSWORD"
  val FROM = "FROM"
  val TO = "TO"
  val ORGANIZATION = "ORGANIZATION"
  val ZONE = "ZONE"
  val GENESIS = "GENESIS"
  val GENESIS_ADDRESS = "GENESIS_ADDRESS"
  val ORGANIZATION_NAME = "ORGANIZATION_NAME"
  val ORGANIZATION_USERNAME = "ORGANIZATION_USERNAME"
  val ZONE_ID = "ZONE_ID"
  val BUYER_ADDRESS = "BUYER_ADDRESS"
  val SELLER_ADDRESS = "SELLER_ADDRESS"
  val OWNER_ADDRESS = "OWNER_ADDRESS"
  val FIAT_PROOF_HASH = "FIAT_PROOF_HASH"
  val PEG_HASH = "PEG_HASH"
  val GAS = "GAS"
  val STATUS = "STATUS"
  val BID = "BID"
  val TIME = "TIME"
  val BUYER_CONTRACT_HASH = "BUYER_CONTRACT_HASH"
  val SELLER_CONTRACT_HASH = "SELLER_CONTRACT_HASH"
  val BUYER_BLOCK_HEIGHT = "BUYER_BLOCK_HEIGHT"
  val SELLER_BLOCK_HEIGHT = "SELLER_BLOCK_HEIGHT"
  val DOCUMENT_HASH = "DOCUMENT_HASH"
  val ASSET_TYPE = "ASSET_TYPE"
  val LOCKED = "LOCKED"
  val ASSET_PRICE = "ASSET_PRICE"
  val QUANTITY_UNIT = "QUANTITY_UNIT"
  val ASSET_QUANTITY = "ASSET_QUANTITY"
  val MODERATED = "MODERATED"
  val TRANSACTION_ID = "TRANSACTION_ID"
  val TRANSACTION_AMOUNT = "TRANSACTION_AMOUNT"
  val REDEEM_AMOUNT = "REDEEM_AMOUNT"
  val AWB_PROOF_HASH = "AWB_PROOF_HASH"
  val AMOUNT = "AMOUNT"
  val ACL_ADDRESS = "ACL_ADDRESS"
  val ISSUE_ASSET = "ISSUE_ASSET"
  val ISSUE_ASSET_REQUEST = "ISSUE_ASSET_REQUEST"
  val ISSUE_FIAT = "ISSUE_FIAT"
  val ISSUE_FIAT_REQUEST = "ISSUE_FIAT_REQUEST"
  val SEND_ASSET = "SEND_ASSET"
  val SEND_FIAT = "SEND_FIAT"
  val REDEEM_ASSET = "REDEEM_ASSET"
  val REDEEM_FIAT = "REDEEM_FIAT"
  val RECEIVE_NOTIFICATIONS = "RECEIVE_NOTIFICATIONS"
  val SELLER_EXECUTE_ORDER = "SELLER_EXECUTE_ORDER"
  val BUYER_EXECUTE_ORDER = "BUYER_EXECUTE_ORDER"
  val MODERATED_SELLER_EXECUTE_ORDER = "MODERATED_SELLER_EXECUTE_ORDER"
  val MODERATED_BUYER_EXECUTE_ORDER = "MODERATED_BUYER_EXECUTE_ORDER"
  val CHANGE_BUYER_BID = "CHANGE_BUYER_BID"
  val CHANGE_SELLER_BID = "CHANGE_SELLER_BID"
  val CONFIRM_BUYER_BID = "CONFIRM_BUYER_BID"
  val CONFIRM_SELLER_BID = "CONFIRM_SELLER_BID"
  val NEGOTIATION = "NEGOTIATION"
  val RELEASE_ASSET = "RELEASE_ASSET"
  val NEGOTIATION_ID = "NEGOTIATION_ID"
  val BUYER_SIGNATURE = "BUYER_SIGNATURE"
  val SELLER_SIGNATURE = "SELLER_SIGNATURE"

  //Master
  val ADDRESS = "ADDRESS"
  val PHONE = "PHONE"
  val EMAIL = "EMAIL"
  val ID = "ID"
  val ACCOUNT_ID = "ACCOUNT_ID"
  val TRADER_ID = "TRADER_ID"
  val ORGANIZATION_ID = "ORGANIZATION_ID"
  val CURRENCY = "CURRENCY"
  val LOGIN = "LOGIN"
  val LOGOUT = "LOGOUT"
  val USERNAME = "USERNAME"
  val USERNAME_AVAILABLE = "USERNAME_AVAILABLE"
  val NOTIFICATION_TOKEN = "NOTIFICATION_TOKEN"
  val CSRF_TOKEN = "csrfToken"
  val SIGNUP = "SIGNUP"
  val UPDATE_CONTACT = "UPDATE_CONTACT"
  val MOBILE_NUMBER = "MOBILE_NUMBER"
  val EMAIL_ADDRESS = "EMAIL_ADDRESS"
  val VERIFY_EMAIL_ADDRESS = "VERIFY_EMAIL_ADDRESS"
  val OTP = "OTP"
  val VERIFY_MOBILE_NUMBER = "VERIFY_MOBILE_NUMBER"
  val VERIFY_TRADER = "VERIFY_TRADER"
  val VERIFY_ORGANIZATION = "VERIFY_ORGANIZATION"
  val VERIFIED_STATUS = "VERIFIED_STATUS"
  val VERIFY_ZONE = "VERIFY_ZONE"
  val LOADING = "LOADING"
  val FAILURE = "FAILURE"
  val WARNING = "WARNING"
  val SUCCESS = "SUCCESS"
  val INFORMATION = "INFORMATION"

  //MasterTransaction
  val APPROVE_FAUCET_REQUEST = "APPROVE_FAUCET_REQUEST"
  val REJECT_FAUCET_REQUEST = "REJECT_FAUCET_REQUEST"
  val PENDING_ISSUE_ASSET_REQUESTS = "PENDING_ISSUE_ASSET_REQUESTS"
  val PENDING_ISSUE_FIAT_REQUESTS = "PENDING_ISSUE_FIAT_REQUESTS"
  val PENDING_VERIFY_ORGANIZATION_REQUESTS = "PENDING_VERIFY_ORGANIZATION_REQUESTS"
  val PENDING_VERIFY_ZONE_REQUESTS = "PENDING_VERIFY_ZONE_REQUESTS"
  val COUPON = "COUPON"
  val REQUEST_COIN = "REQUEST_COIN"
  val REQUEST_ID = "REQUEST_ID"
  val APPROVE = "APPROVE"
  val REJECT = "REJECT"
  val REJECT_ISSUE_ASSET_REQUEST = "REJECT_ISSUE_ASSET_REQUEST"
  val REJECT_ISSUE_FIAT_REQUEST = "REJECT_ISSUE_FIAT_REQUEST"
  val REJECT_VERIFY_ORGANIZATION_REQUEST = "REJECT_VERIFY_ORGANIZATION_REQUEST"
  val REJECT_VERIFY_TRADER_REQUEST = "REJECT_VERIFY_TRADER_REQUEST"
  val REJECT_VERIFY_ZONE_REQUEST = "REJECT_VERIFY_ZONE_REQUEST"
}
