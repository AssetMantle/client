package constants

object Module {
  //Master model
  val MASTER_ACCOUNT = "MASTER_ACCOUNT"
  val MASTER_ORGANIZATION = "MASTER_ORGANIZATION"
  val MASTER_ZONE = "MASTER_ZONE"

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
  val BLOCKCHAIN_OWNER = "BLOCKCHAIN_OWNER"
  val BLOCKCHAIN_ORGANIZATION = "BLOCKCHAIN_ORGANIZATION"
  val BLOCKCHAIN_ZONE = "BLOCKCHAIN_ZONE"


  //Transactions
  val TRANSACTIONS_ADD_KEY = "TRANSACTIONS_ADD_KEY"
  val TRANSACTIONS_ADD_ORGANIZATION = "TRANSACTIONS_ADD_ORGANIZATION"
  val TRANSACTIONS_ADD_ZONE = "TRANSACTIONS_ADD_ZONE"
  val TRANSACTIONS_BUYER_EXECUTE_ORDER = "TRANSACTIONS_BUYER_EXECUTE_ORDER"
  val TRANSACTIONS_CHANGE_BUYER_BID = "TRANSACTIONS_CHANGE_BUYER_BID"
  val TRANSACTIONS_CHANGE_SELLER_BID = "TRANSACTIONS_CHANGE_SELLER_BID"
  val TRANSACTIONS_CONFIRM_BUYER_BID = "TRANSACTIONS_CONFIRM_BUYER_BID"
  val TRANSACTIONS_CONFIRM_SELLER_BID = "TRANSACTIONS_CONFIRM_SELLER_BID"
  val TRANSACTIONS_GET_ACCOUNT = "TRANSACTIONS_GET_ACCOUNT"
  val TRANSACTIONS_GET_ACL = "TRANSACTIONS_GET_ACL"
  val TRANSACTIONS_GET_ASSET = "TRANSACTIONS_GET_ASSET"
  val TRANSACTIONS_GET_FIAT = "TRANSACTIONS_GET_FIAT"
  val TRANSACTIONS_GET_NEGOTIATION = "TRANSACTIONS_GET_NEGOTIATION"
  val TRANSACTIONS_GET_ORDER = "TRANSACTIONS_GET_ORDER"
  val TRANSACTIONS_GET_ORGANIZATION = "TRANSACTIONS_GET_ORGANIZATION"
  val TRANSACTIONS_GET_RESPONSE = "TRANSACTIONS_GET_RESPONSE"
  val TRANSACTIONS_GET_SEED = "TRANSACTIONS_GET_SEED"
  val TRANSACTIONS_GET_TRADER_REPUTATION = "TRANSACTIONS_GET_TRADER_REPUTATION"
  val TRANSACTIONS_GET_ZONE = "TRANSACTIONS_GET_ZONE"
  val TRANSACTIONS_ISSUE_ASSET = "TRANSACTIONS_ISSUE_ASSET"
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

  //MasterTransactions
  val MASTER_TRANSACTION_FAUCET_REQUESTS = "MASTER_TRANSACTION_FAUCET_REQUESTS"
  val MASTER_TRANSACTION_ISSUE_ASSET_REQUESTS = "MASTER_TRANSACTION_ISSUE_ASSET_REQUESTS"
  val MASTER_TRANSACTION_ISSUE_FIAT_REQUESTS = "MASTER_TRANSACTION_ISSUE_FIAT_REQUESTS"

  //Controllers
  val CONTROLLERS_SIGN_UP = "CONTROLLERS_SIGN_UP"
  val CONTROLLERS_ADD_ORGANIZATION = "CONTROLLERS_ADD_ORGANIZATION"
  val CONTROLLERS_ADD_ZONE = "CONTROLLERS_ADD_ZONE"
  val CONTROLLERS_LOGIN = "CONTROLLERS_LOGIN"
  val CONTROLLERS_LOGOUT = "CONTROLLERS_LOGOUT"
  val CONTROLLERS_EMAIL = "CONTROLLERS_EMAIL"
  val CONTROLLERS_NOTIFICATION = "CONTROLLERS_NOTIFICATION"
  val CONTROLLERS_SMS = "CONTROLLERS_SMS"

  //Actions
  val ACTIONS_WITH_LOGIN_ACTION = "ACTIONS_WITH_LOGIN_ACTION"
  val ACTIONS_WITH_UNKNOWN_LOGIN_ACTION = "ACTIONS_WITH_UNKNOWN_LOGIN_ACTION"

}
