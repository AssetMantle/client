package constants


import controllers.routes
import play.api.routing.JavaScriptReverseRoute

object Response {

  lazy val PREFIX = "RESPONSE."
  lazy val FAILURE_PREFIX = "FAILURE."
  lazy val WARNING_PREFIX = "WARNING."
  lazy val SUCCESS_PREFIX = "SUCCESS."
  lazy val INFO_PREFIX = "INFO."
  lazy val LOG_PREFIX = "LOG."
  val KEY_ASSET = "asset"
  val KEY_FIAT = "fiat"
  val KEY_NEGOTIATION_ID = "negotiation_id"
  val KEY_ORDER_ID = "order_id"
  val KEY_EXECUTED = "executed"
  val NULL_POINTER_EXCEPTION = new Failure("NULL_POINTER_EXCEPTION")
  val INVALID_FILE_PATH_EXCEPTION = new Failure("INVALID_FILE_PATH_EXCEPTION")
  val FILE_SECURITY_EXCEPTION = new Failure("FILE_SECURITY_EXCEPTION")
  val GENERIC_EXCEPTION = new Failure("GENERIC_EXCEPTION")
  val I_O_EXCEPTION = new Failure("I_O_EXCEPTION")
  val FILE_NOT_FOUND_EXCEPTION = new Failure("FILE_NOT_FOUND_EXCEPTION")
  val FILE_ILLEGAL_ARGUMENT_EXCEPTION = new Failure("FILE_ILLEGAL_ARGUMENT_EXCEPTION")
  val CLASS_CAST_EXCEPTION = new Failure("CLASS_CAST_EXCEPTION")
  val FILE_UNSUPPORTED_OPERATION_EXCEPTION = new Failure("FILE_UNSUPPORTED_OPERATION_EXCEPTION")

  //Success- for telling if something is done and the further steps opened up because of it
  val SUCCESS = new Success("SUCCESS")
  val ACL_SET = new Success("ACL_SET")
  val ASSET_ISSUED = new Success("ASSET_ISSUED")
  val ASSET_REDEEMED = new Success("ASSET_REDEEMED")
  val ASSET_RELEASED = new Success("ASSET_RELEASED")
  val ASSET_SENT = new Success("ASSET_SENT")
  val BUYER_BID_CHANGED = new Success("BUYER_BID_CHANGED")
  val BUYER_BID_CONFIRMED = new Success("BUYER_BID_CONFIRMED")
  val BUYER_FEEDBACK_SET = new Success("BUYER_FEEDBACK_SET")
  val BUYER_ORDER_EXECUTED = new Success("BUYER_ORDER_EXECUTED")
  val COINS_REQUESTED = new Success("COINS_REQUESTED")
  val DECISION_UPDATED = new Success("DECISION_UPDATED")
  val EMAIL_ADDRESS_UPDATED = new Success("EMAIL_ADDRESS_UPDATED")
  val MOBILE_NUMBER_UPDATED = new Success("MOBILE_NUMBER_UPDATED")
  val EMAIL_ADDRESS_VERIFIED = new Success("EMAIL_ADDRESS_VERIFIED")
  val FAUCET_REQUEST_APPROVED = new Success("FAUCET_REQUEST_APPROVED")
  val FAUCET_REQUEST_REJECTED = new Success("FAUCET_REQUEST_REJECTED")
  val FIAT_ISSUED = new Success("FIAT_ISSUED")
  val FIAT_REDEEMED = new Success("FIAT_REDEEMED")
  val FIAT_SENT = new Success("FIAT_SENT")
  val ISSUE_ASSET_REQUEST_SENT = new Success("ISSUE_ASSET_REQUEST_SENT")
  val ISSUE_FIAT_REQUEST_SENT = new Success("ISSUE_FIAT_REQUEST_SENT")
  val ISSUE_ASSET_REQUEST_REJECTED = new Success("ISSUE_ASSET_REQUEST_REJECTED")
  val ISSUE_FIAT_REQUEST_REJECTED = new Success("ISSUE_FIAT_REQUEST_REJECTED")
  val LOGGED_IN = new Success("LOGGED_IN")
  val SIGNED_UP = new Success("SIGNED_UP", routes.javascript.AccountController.loginForm)
  val LOGGED_OUT = new Success("LOGGED_OUT")
  val KEY_ADDED = new Success("KEY_ADDED")
  val MOBILE_NUMBER_VERIFIED = new Success("MOBILE_NUMBER_VERIFIED")
  val ORGANIZATION_REQUEST_ACCEPTED = new Success("ORGANIZATION_REQUEST_ACCEPTED")
  val ZONE_VERIFIED = new Success("ZONE_VERIFIED")
  val VERIFY_ZONE_REJECTED = new Success("VERIFY_ZONE_REJECTED")
  val ORGANIZATION_ADDED = new Success("ORGANIZATION_ADDED")
  val ORGANIZATION_ADDED_FOR_VERIFICATION = new Success("ORGANIZATION_ADDED_FOR_VERIFICATION")
  val TRADER_ADDED_FOR_VERIFICATION = new Success("TRADER_ADDED_FOR_VERIFICATION")
  val ZONE_ADDED_FOR_VERIFICATION = new Success("ZONE_ADDED_FOR_VERIFICATION")
  val SELLER_BID_CHANGED = new Success("SELLER_BID_CHANGED")
  val SELLER_BID_CONFIRMED = new Success("SELLER_BID_CONFIRMED")
  val SELLER_FEEDBACK_SET = new Success("SELLER_FEEDBACK_SET")
  val SELLER_ORDER_EXECUTED = new Success("SELLER_ORDER_EXECUTED")
  val TRADER_ADDED = new Success("TRADER_ADDED")
  val ZONE_REJECT_TRADER_REQUEST_SUCCESSFUL = new Success("ZONE_REJECT_TRADER_REQUEST_SUCCESSFUL")
  val ORGANIZATION_REJECT_TRADER_REQUEST_SUCCESSFUL = new Success("ORGANIZATION_REJECT_TRADER_REQUEST_SUCCESSFUL")
  val COINS_SENT = new Success("COINS_SENT")
  val ZONE_ADDED = new Success("ZONE_ADDED")
  val ZONE_REQUEST_SENT = new Success("ZONE_REQUEST_SENT")
  val ORGANIZATION_REQUEST_REJECTED = new Success("ORGANIZATION_REQUEST_REJECTED")
  val FIATS_EXCEED_PENDING_AMOUNT = new Success("FIATS_EXCEED_PENDING_AMOUNT")
  val FILE_UPLOAD_SUCCESSFUL = new Success("FILE_UPLOAD_SUCCESSFUL")
  val FILE_UPDATE_SUCCESSFUL = new Success("FILE_UPDATE_SUCCESSFUL")
  val DOCUMENT_APPROVED = new Success("DOCUMENT_APPROVED")
  val PASSWORD_UPDATED = new Success("PASSWORD_UPDATED")
  val OTP_SENT = new Success("OTP_SENT")
  val TRADER_INVITATION_EMAIL_SENT = new Success("TRADER_INVITATION_EMAIL_SENT")
  val ZONE_INVITATION_EMAIL_SENT = new Success("ZONE_INVITATION_EMAIL_SENT")
  val IDENTIFICATION_ADDED_FOR_VERIFICATION = new Success("IDENTIFICATION_ADDED_FOR_VERIFICATION")
  val TRADER_RELATION_REQUEST_SEND_SUCCESSFUL = new Success("TRADER_RELATION_REQUEST_SEND_SUCCESSFUL")
  val SALES_QUOTE_CREATED = new Success("SALES_QUOTE_CREATED")
  val MESSAGE_READ = new Success("MESSAGE_READ")
  val BANK_ACCOUNT_DETAILS_UPDATED = new Success("BANK_ACCOUNT_DETAILS_UPDATED")
  val UBO_ADDED = new Success("UBO_ADDED")
  val UBO_DELETED = new Success("UBO_DELETED")
  val NEGOTIATION_REQUEST_SENT = new Success("NEGOTIATION_REQUEST_SENT")
  val NEGOTIATION_REQUEST_ACCEPTED_BLOCKCHAIN_TRANSACTION_PENDING = new Success("NEGOTIATION_REQUEST_ACCEPTED_BLOCKCHAIN_TRANSACTION_PENDING")
  val NEGOTIATION_REQUEST_REJECTED = new Success("NEGOTIATION_REQUEST_REJECTED")
  val NEGOTIATION_ASSET_TERMS_UPDATED = new Success("NEGOTIATION_ASSET_TERMS_UPDATED")
  val NEGOTIATION_ASSET_OTHER_DETAILS_UPDATED = new Success("NEGOTIATION_ASSET_OTHER_DETAILS_UPDATED")
  val NEGOTIATION_PAYMENT_TERMS_UPDATED = new Success("NEGOTIATION_PAYMENT_TERMS_UPDATED")
  val NEGOTIATION_DOCUMENT_CHECKLISTS_UPDATED = new Success("NEGOTIATION_DOCUMENT_CHECKLISTS_UPDATED")
  val DOCUSIGN_AUTHORIZED = new Success("DOCUSIGN_AUTHORIZED")
  val ACCESS_TOKEN_UPDATED = new Success("ACCESS_TOKEN_UPDATED")
  val BLOCKCHAIN_TRANSACTION_BUYER_CONFIRM_NEGOTIATION_TRANSACTION_SENT = new Success("BLOCKCHAIN_TRANSACTION_BUYER_CONFIRM_NEGOTIATION_TRANSACTION_SENT")
  val BLOCKCHAIN_TRANSACTION_SELLER_CONFIRM_NEGOTIATION_TRANSACTION_SENT = new Success("BLOCKCHAIN_TRANSACTION_SELLER_CONFIRM_NEGOTIATION_TRANSACTION_SENT")
  val ZONE_RELEASED_ASSET = new Success("ZONE_RELEASED_ASSET")
  val ACCOUNT_CREATED = new Success("ACCOUNT_CREATED")

  //Warning- for telling that something important is not done and ask to do it
  val VERIFY_MOBILE_NUMBER = new Warning("VERIFY_MOBILE_NUMBER", routes.javascript.ContactController.verifyMobileNumberForm)
  val VERIFY_EMAIL_ADDRESS = new Warning("VERIFY_EMAIL_ADDRESS", routes.javascript.ContactController.verifyEmailAddressForm)
  val UPDATE_MOBILE_NUMBER = new Warning("UPDATE_CONTACT_DETAILS", routes.javascript.ContactController.addOrUpdateMobileNumberForm)
  val UPDATE_EMAIL_ADDRESS = new Warning("UPDATE_CONTACT_DETAILS", routes.javascript.ContactController.addOrUpdateEmailAddressForm)

  //Failure- for telling that something failed
  val FAILURE = new Failure("FAILURE")
  val NO_SUCH_ELEMENT_EXCEPTION = new Failure("NO_SUCH_ELEMENT_EXCEPTION")
  val NO_SUCH_FILE_EXCEPTION = new Failure("NO_SUCH_FILE_EXCEPTION")
  val NO_SUCH_DOCUMENT_TYPE_EXCEPTION = new Failure("NO_SUCH_DOCUMENT_TYPE_EXCEPTION")
  val PSQL_EXCEPTION = new Failure("PSQL_EXCEPTION")
  val JSON_PARSE_EXCEPTION = new Failure("JSON_PARSE_EXCEPTION")
  val JSON_MAPPING_EXCEPTION = new Failure("JSON_MAPPING_EXCEPTION")
  val INVALID_OTP = new Failure("INVALID_OTP")
  val EMAIL_ADDRESS_NOT_FOUND = new Failure("EMAIL_ADDRESS_NOT_FOUND")
  val MOBILE_NUMBER_NOT_FOUND = new Failure("MOBILE_NUMBER_NOT_FOUND")
  val EMAIL_ADDRESS_TAKEN = new Failure("EMAIL_ADDRESS_TAKEN")
  val MOBILE_NUMBER_TAKEN = new Failure("MOBILE_NUMBER_TAKEN")
  val CONNECT_EXCEPTION = new Failure("CONNECT_EXCEPTION")
  val EMAIL_NOT_FOUND = new Failure("EMAIL_NOT_FOUND")
  val NO_RESPONSE = new Failure("NO_RESPONSE")
  val INCORRECT_LOG_IN = new Failure("INCORRECT_LOG_IN")
  val UNVERIFIED_ZONE = new Failure("UNVERIFIED_ZONE")
  val UNVERIFIED_TRADER = new Failure("UNVERIFIED_TRADER")
  val UNVERIFIED_ORGANIZATION = new Failure("UNVERIFIED_ORGANIZATION")
  val REQUEST_ALREADY_APPROVED_OR_REJECTED = new Failure("REQUEST_ALREADY_APPROVED_OR_REJECTED")
  val USERNAME_NOT_FOUND = new Failure("USERNAME_NOT_FOUND", routes.javascript.AccountController.loginForm)
  val TOKEN_NOT_FOUND = new Failure("TOKEN_NOT_FOUND", routes.javascript.AccountController.loginForm)
  val TOKEN_TIMEOUT = new Failure("TOKEN_TIMEOUT")
  val INVALID_TOKEN = new Failure("INVALID_TOKEN")
  val UNAUTHORIZED = new Failure("UNAUTHORIZED")
  val DOCUMENT_REJECTED = new Failure("DOCUMENT_REJECTED")
  val DOCUMENT_NOT_FOUND = new Failure("DOCUMENT_NOT_FOUND")
  val PASSWORDS_DO_NOT_MATCH = new Failure("PASSWORDS_DO_NOT_MATCH")
  val USERNAME_UNAVAILABLE = new Failure("USERNAME_UNAVAILABLE")
  val INVALID_USERNAME = new Failure("INVALID_USERNAME")
  val INVALID_PASSWORD = new Failure("INVALID_PASSWORD")
  val INVALID_INPUT = new Failure("INVALID_INPUT")
  val NO_FILE = new Failure("NO_FILE")
  val PASSWORD_NOT_GIVEN = new Failure("PASSWORD_NOT_GIVEN")
  val GAS_NOT_GIVEN = new Failure("GAS_NOT_GIVEN")
  val ALL_KYC_FILES_NOT_VERIFIED = new Failure("ALL_KYC_FILES_NOT_VERIFIED")
  val ALL_KYC_FILES_NOT_FOUND = new Failure("ALL_KYC_FILES_NOT_FOUND")
  val ALL_ASSET_FILES_NOT_VERIFIED = new Failure("ALL_ASSET_FILES_NOT_VERIFIED")
  val SMS_SEND_FAILED = new Failure("SMS_SEND_FAILED")
  val SMS_SERVICE_CONNECTION_FAILURE = new Failure("SMS_SERVICE_CONNECTION_FAILURE")
  val UNVERIFIED_IDENTIFICATION = new Failure("UNVERIFIED_IDENTIFICATION")
  val SFTP_SCHEDULER_FAILED = new Failure("SFTP_SCHEDULER_FAILED")
  val FORM_FIELDS_CANNOT_BE_EMPTY = new Failure("FORM_FIELDS_CANNOT_BE_EMPTY")
  val CANNOT_FILL_ALL_FIELDS = new Failure("CANNOT_FILL_ALL_FIELDS")
  val COUNTERPARTY_CANNOT_BE_SELF = new Failure("COUNTERPARTY_CANNOT_BE_SELF")
  val COUNTERPARTY_TRADER_FROM_SAME_ORGANIZATION = new Failure("COUNTERPARTY_TRADER_FROM_SAME_ORGANIZATION")
  val TRADER_RELATION_REQUEST_ALREADY_EXISTS = new Failure("TRADER_RELATION_REQUEST_ALREADY_EXISTS")
  val COUNTERPARTY_ALREADY_EXISTS = new Failure("COUNTERPARTY_ALREADY_EXISTS")
  val INVITATION_EMAIL_ALREADY_SENT = new Failure("INVITATION_EMAIL_ALREADY_SENT")
  val BUYER_FROM_SAME_ORGANIZATION = new Failure("BUYER_FROM_SAME_ORGANIZATION")
  val NOT_PRESENT_AS_COUNTERPARTY = new Failure("NOT_PRESENT_AS_COUNTERPARTY")
  val UBO_TOTAL_SHARE_PERCENTAGE_EXCEEDS_MAXIMUM_VALUE = new Failure("UBO_TOTAL_SHARE_PERCENTAGE_EXCEEDS_MAXIMUM_VALUE")
  val CHAT_ROOM_NOT_FOUND = new Failure("CHAT_ROOM_NOT_FOUND")
  val MEMBER_CHECK_NOT_VERIFIED = new Failure("MEMBER_CHECK_NOT_VERIFIED")
  val ALL_TRADER_BACKGROUND_CHECK_FILES_NOT_VERFIED = new Failure("ALL_TRADER_BACKGROUND_CHECK_FILES_NOT_VERFIED")
  val ZONE_ID_MISMATCH = new Failure("ZONE_ID_MISMATCH")
  val ORGANIZATION_ID_MISMATCH = new Failure("ORGANIZATION_ID_MISMATCH")
  val ZONE_INVITATION_NOT_FOUND = new Failure("ZONE_INVITATION_NOT_FOUND")
  val DOCUMENT_LIST_EMPTY = new Failure("DOCUMENT_LIST_EMPTY")
  val NEGOTIATION_NOT_FOUND = new Failure("NEGOTIATION_NOT_FOUND")
  val INVALID_PAGE_NUMBER = new Failure("INVALID_PAGE_NUMBER")
  val USERNAME_OR_PASSWORD_INCORRECT = new Failure("USERNAME_OR_PASSWORD_INCORRECT", routes.javascript.AccountController.loginForm)
  val INCORRECT_PASSWORD = new Failure("INCORRECT_PASSWORD")
  val NEGOTIATION_TERMS_NOT_CONFIRMED = new Failure("NEGOTIATION_TERMS_NOT_CONFIRMED")
  val NEW_PASSWORD_SAME_AS_OLD_PASSWORD = new Failure("NEW_PASSWORD_SAME_AS_OLD_PASSWORD")
  val COMET_ACTOR_ERROR = new Failure("COMET_ACTOR_ERROR")
  val ENVELOPE_CREATION_FAILED = new Failure("ENVELOPE_CREATION_FAILED")
  val SENDER_VIEW_CREATION_FAILED = new Failure("SENDER_VIEW_CREATION_FAILED")
  val RECEPIENT_VIEW_CREATION_FAILED = new Failure("RECEPIENT_VIEW_CREATION_FAILED")
  val FAILED_TO_FETCH_SIGNED_DOCUMENT = new Failure("FAILED_TO_FETCH_SIGNED_DOCUMENT")
  val ACCESS_TOKEN_GENERATION_FAILED = new Failure("ACCESS_TOKEN_GENERATION_FAILED")
  val AUTHORISATION_CODE_NOT_FOUND = new Failure("AUTHORISATION_CODE_NOT_FOUND")
  val UNEXPECTED_EVENT = new Failure("UNEXPECTED_EVENT")
  val ACCOUNT_KYC_PENDING = new Failure("ACCOUNT_KYC_PENDING")
  val CONTACT_VERIFICATION_PENDING = new Failure("CONTACT_VERIFICATION_PENDING")

  val INDEX_OUT_OF_BOUND = new Failure("INDEX_OUT_OF_BOUND")
  val JSON_UNMARSHALLING_ERROR = new Failure("JSON_UNMARSHALLING_ERROR")
  val DATE_FORMAT_ERROR = new Failure("DATE_FORMAT_ERROR")
  val SECURITY_TOKEN_NOT_FOUND = new Failure("SECURITY_TOKEN_NOT_FOUND", routes.javascript.AccountController.loginForm)
  val WALLET_NOT_FOUND = new Failure("WALLET_NOT_FOUND")
  val WALLET_UPSERT_FAILED = new Failure("WALLET_UPSERT_FAILED")
  val WALLET_INSERT_FAILED = new Failure("WALLET_INSERT_FAILED")
  val TRANSACTION_TYPE_NOT_FOUND = new Failure("TRANSACTION_TYPE_NOT_FOUND")
  val TRANSACTION_STRUCTURE_CHANGED = new Failure("TRANSACTION_STRUCTURE_CHANGED")
  val NUMBER_FORMAT_EXCEPTION = new Failure("NUMBER_FORMAT_EXCEPTION")
  val PRECISION_MORE_THAN_REQUIRED = new Failure("PRECISION_MORE_THAN_REQUIRED")
  val CONTENT_CONVERSION_ERROR = new Failure("CONTENT_CONVERSION_ERROR")
  val CRYPTO_TOKEN_INSERT_FAILED = new Failure("CRYPTO_TOKEN_INSERT_FAILED")
  val CRYPTO_TOKEN_UPDATE_FAILED = new Failure("CRYPTO_TOKEN_UPDATE_FAILED")
  val CRYPTO_TOKEN_UPSERT_FAILED = new Failure("CRYPTO_TOKEN_UPSERT_FAILED")
  val CRYPTO_TOKEN_NOT_FOUND = new Failure("CRYPTO_TOKEN_NOT_FOUND")
  val DOCUMENT_UPLOAD_FAILED = new Failure("DOCUMENT_UPLOAD_FAILED")
  val DOCUMENT_UPDATE_FAILED = new Failure("DOCUMENT_UPDATE_FAILED")
  val TRANSACTION_INSERT_FAILED = new Failure("TRANSACTION_INSERT_FAILED")
  val TRANSACTION_UPSERT_FAILED = new Failure("TRANSACTION_UPSERT_FAILED")
  val DELEGATION_INSERT_FAILED = new Failure("DELEGATION_INSERT_FAILED")
  val DELEGATION_UPSERT_FAILED = new Failure("DELEGATION_UPSERT_FAILED")
  val DELEGATION_UPDATE_FAILED = new Failure("DELEGATION_UPDATE_FAILED")
  val DELEGATION_DELETE_FAILED = new Failure("DELEGATION_DELETE_FAILED")
  val DELEGATION_NOT_FOUND = new Failure("DELEGATION_NOT_FOUND")
  val STAKING_TOKEN_NOT_FOUND = new Failure("STAKING_TOKEN_NOT_FOUND")
  val REDELEGATION_NOT_FOUND = new Failure("REDELEGATION_NOT_FOUND")
  val REDELEGATION_INSERT_FAILED = new Failure("REDELEGATION_INSERT_FAILED")
  val REDELEGATION_UPSERT_FAILED = new Failure("REDELEGATION_UPSERT_FAILED")
  val REDELEGATION_UPDATE_FAILED = new Failure("REDELEGATION_UPDATE_FAILED")
  val REDELEGATION_DELETE_FAILED = new Failure("REDELEGATION_DELETE_FAILED")
  val REDELEGATION_RESPONSE_NOT_FOUND = new Failure("REDELEGATION_RESPONSE_NOT_FOUND")
  val UNDELEGATION_INSERT_FAILED = new Failure("UNDELEGATION_INSERT_FAILED")
  val UNDELEGATION_UPSERT_FAILED = new Failure("UNDELEGATION_UPSERT_FAILED")
  val UNDELEGATION_UPDATE_FAILED = new Failure("UNDELEGATION_UPDATE_FAILED")
  val UNDELEGATION_DELETE_FAILED = new Failure("UNDELEGATION_DELETE_FAILED")
  val TIME_NOT_FOUND = new Failure("TIME_NOT_FOUND")
  val SEARCH_QUERY_NOT_FOUND = new Failure("SEARCH_QUERY_NOT_FOUND")
  val SIGNING_INFO_INSERT_FAILED = new Failure("SIGNING_INFO_INSERT_FAILED")
  val SIGNING_INFO_UPSERT_FAILED = new Failure("SIGNING_INFO_UPSERT_FAILED")
  val SIGNING_INFO_UPDATE_FAILED = new Failure("SIGNING_INFO_UPDATE_FAILED")
  val SIGNING_INFO_DELETE_FAILED = new Failure("SIGNING_INFO_DELETE_FAILED")
  val SIGNING_INFO_NOT_FOUND = new Failure("SIGNING_INFO_NOT_FOUND")
  val KEY_BASE_ACCOUNTS_UPDATE_FAILED = new Failure("KEY_BASE_ACCOUNTS_UPDATE_FAILED")
  val BLOCK_INSERT_FAILED = new Failure("BLOCK_INSERT_FAILED")
  val BLOCK_UPSERT_FAILED = new Failure("BLOCK_UPSERT_FAILED")
  val BLOCK_NOT_FOUND = new Failure("BLOCK_NOT_FOUND")
  val TRANSACTION_NOT_FOUND = new Failure("TRANSACTION_NOT_FOUND")
  val VALIDATOR_NOT_FOUND = new Failure("VALIDATOR_NOT_FOUND")
  val ASSET_INSERT_FAILED = new Failure("ASSET_INSERT_FAILED")
  val ASSET_UPSERT_FAILED = new Failure("ASSET_UPSERT_FAILED")
  val ASSET_DELETE_FAILED = new Failure("ASSET_DELETE_FAILED")
  val ORDER_NOT_FOUND = new Failure("ORDER_NOT_FOUND")
  val ORDER_INSERT_FAILED = new Failure("ORDER_INSERT_FAILED")
  val ORDER_UPSERT_FAILED = new Failure("ORDER_UPSERT_FAILED")
  val ORDER_UPDATE_FAILED = new Failure("ORDER_UPDATE_FAILED")
  val ORDER_DELETE_FAILED = new Failure("ORDER_DELETE_FAILED")
  val IDENTITY_NOT_FOUND = new Failure("IDENTITY_NOT_FOUND")
  val IDENTITY_INSERT_FAILED = new Failure("IDENTITY_INSERT_FAILED")
  val IDENTITY_UPSERT_FAILED = new Failure("IDENTITY_UPSERT_FAILED")
  val IDENTITY_UPDATE_FAILED = new Failure("IDENTITY_UPDATE_FAILED")
  val IDENTITY_DELETE_FAILED = new Failure("IDENTITY_DELETE_FAILED")
  val META_NOT_FOUND = new Failure("META_NOT_FOUND")
  val META_INSERT_FAILED = new Failure("META_INSERT_FAILED")
  val META_UPSERT_FAILED = new Failure("META_UPSERT_FAILED")
  val META_UPDATE_FAILED = new Failure("META_UPDATE_FAILED")
  val META_DELETE_FAILED = new Failure("META_DELETE_FAILED")
  val SPLIT_NOT_FOUND = new Failure("SPLIT_NOT_FOUND")
  val SPLIT_INSERT_FAILED = new Failure("SPLIT_INSERT_FAILED")
  val SPLIT_UPSERT_FAILED = new Failure("SPLIT_UPSERT_FAILED")
  val SPLIT_UPDATE_FAILED = new Failure("SPLIT_UPDATE_FAILED")
  val SPLIT_DELETE_FAILED = new Failure("SPLIT_DELETE_FAILED")
  val DATA_TYPE_NOT_FOUND = new Failure("DATA_TYPE_NOT_FOUND")
  val PROPERTY_NOT_FOUND = new Failure("PROPERTY_NOT_FOUND")
  val TRANSACTION_PROCESSING_FAILED = new Failure("TRANSACTION_PROCESSING_FAILED")
  val TRANSACTION_HASH_QUERY_FAILED = new Failure("TRANSACTION_HASH_QUERY_FAILED")
  val BLOCK_QUERY_FAILED = new Failure("BLOCK_QUERY_FAILED")
  val TRANSACTION_BY_HEIGHT_QUERY_FAILED = new Failure("TRANSACTION_BY_HEIGHT_QUERY_FAILED")
  val INVALID_DATA_VALUE = new Failure("INVALID_DATA_VALUE")
  val INVALID_DATA_TYPE = new Failure("INVALID_DATA_TYPE")

  val BLOCKCHAIN_CONNECTION_LOST = new Failure("BLOCKCHAIN_CONNECTION_LOST")

  val NEGOTIATION_TERMS_NOT_ACCEPTED = new Failure("NEGOTIATION_TERMS_NOT_ACCEPTED")
  val CONTRACT_NOT_VERIFIED = new Failure("CONTRACT_NOT_VERIFIED")
  val CONTRACT_REJECTED = new Failure("CONTRACT_REJECTED")
  val ASSET_NOT_FOUND = new Failure("ASSET_NOT_FOUND")
  val ASSET_LOCKED = new Failure("ASSET_LOCKED")
  val CONFIRM_TRANSACTION_PENDING = new Failure("CONFIRM_TRANSACTION_PENDING")
  val ASSET_PEG_WALLET_NOT_FOUND = new Failure("ASSET_PEG_WALLET_NOT_FOUND")
  val ASSET_ALREADY_UNLOCKED = new Failure("ASSET_ALREADY_UNLOCKED")
  val BILL_OF_LADING_VERIFICATION_STATUS_PENDING = new Failure("BILL_OF_LADING_VERIFICATION_STATUS_PENDING")
  val BILL_OF_LADING_NOT_FOUND = new Failure("BILL_OF_LADING_NOT_FOUND")
  val BILL_OF_LADING_REJECTED = new Failure("BILL_OF_LADING_REJECTED")
  val FIAT_PEG_WALLET_NOT_FOUND = new Failure("FIAT_PEG_WALLET_NOT_FOUND")
  val ORGANIZATION_NOT_VERIFIED = new Failure("ORGANIZATION_NOT_VERIFIED")
  val INVALID_PAYMENT_TERMS = new Failure("INVALID_PAYMENT_TERMS")
  val TENURE_AND_TENTATIVE_DATE_BOTH_FOUND = new Failure("TENURE_AND_TENTATIVE_DATE_BOTH_FOUND")
  val REFRENCE_REQUIRED_WITH_TENURE = new Failure("REFRENCE_REQUIRED_WITH_TENURE")
  val REFRENCE_NOT_REQUIRED = new Failure("REFRENCE_NOT_REQUIRED")
  val PHYSICAL_DOCUMENTS_HANDLED_VIA_REQUIRED = new Failure("PHYSICAL_DOCUMENTS_HANDLED_VIA_REQUIRED")
  val FILE_UPLOAD_ERROR = new Failure("FILE_UPLOAD_ERROR")
  val BLOCKCHAIN_ACCOUNT_NOT_FOUND = new Failure("BLOCKCHAIN_ACCOUNT_NOT_FOUND")
  val FIAT_PEG_NOT_FOUND = new Failure("FIAT_PEG_NOT_FOUND")
  val ALL_TRADE_DOCUMENTS_NOT_UPLOADED = new Failure("ALL_TRADE_DOCUMENTS_NOT_UPLOADED")
  val TRANSACTION_HASH_NOT_FOUND = new Failure("TRANSACTION_HASH_NOT_FOUND")
  val KEY_STORE_ERROR = new Failure("KEY_STORE_ERROR")

  class Failure(private val response: String, private val actionController: JavaScriptReverseRoute = null) {
    val message: String = PREFIX + FAILURE_PREFIX + response
    val action: String = utilities.String.getJsRouteString(actionController)
    val logMessage: String = LOG_PREFIX + response
  }

  class Warning(private val response: String, private val actionController: JavaScriptReverseRoute = null) {
    val message: String = PREFIX + WARNING_PREFIX + response
    val action: String = utilities.String.getJsRouteString(actionController)
  }

  class Success(private val response: String, private val actionController: JavaScriptReverseRoute = null) {
    val message: String = Response.PREFIX + Response.SUCCESS_PREFIX + response
    val action: String = utilities.String.getJsRouteString(actionController)
  }

  class Info(private val response: String, private val actionController: JavaScriptReverseRoute = null) {
    val message: String = PREFIX + INFO_PREFIX + response
    val action: String = utilities.String.getJsRouteString(actionController)
  }

}
