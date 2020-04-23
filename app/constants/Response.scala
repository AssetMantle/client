package constants


import controllers.routes
import play.api.routing.JavaScriptReverseRoute

object Response {

  lazy val PREFIX = "RESPONSE."
  lazy val FAILURE_PREFIX = "FAILURE."
  lazy val WARNING_PREFIX = "WARNING."
  lazy val SUCCESS_PREFIX = "SUCCESS."
  lazy val INFO_PREFIX = "INFO."
  val KEY_ASSET = "asset"
  val KEY_FIAT = "fiat"
  val KEY_NEGOTIATION_ID = "negotiation_id"
  val KEY_ORDER_ID = "order_id"
  val KEY_EXECUTED = "executed"
  val NULL_POINTER_EXCEPTION = new Failure("NULL_POINTER_EXCEPTION")
  val GENERIC_EXCEPTION = new Failure("GENERIC_EXCEPTION")
  val I_O_EXCEPTION = new Failure("I_O_EXCEPTION")

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
  val FILE_UPLOAD_SUCCESSFUL = new Success("FILE_UPLOAD_SUCCESSFUL")
  val FILE_UPDATE_SUCCESSFUL = new Success("FILE_UPDATE_SUCCESSFUL")
  val DOCUMENT_APPROVED = new Success("DOCUMENT_APPROVED")
  val PASSWORD_UPDATED = new Success("PASSWORD_UPDATED")
  val OTP_SENT = new Success("OTP_SENT")
  val INVITATION_EMAIL_SENT = new Success("INVITATION_EMAIL_SENT")
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
  val NEGOTIATION_PAYMENT_TERMS_UPDATED = new Success("NEGOTIATION_PAYMENT_TERMS_UPDATED")
  val NEGOTIATION_DOCUMENT_CHECKLISTS_UPDATED = new Success("NEGOTIATION_DOCUMENT_CHECKLISTS_UPDATED")

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
  val ALL_ASSET_FILES_NOT_VERIFIED = new Failure("ALL_KYC_FILES_NOT_VERIFIED")
  val SMS_SEND_FAILED = new Failure("SMS_SEND_FAILED")
  val SMS_SERVICE_CONNECTION_FAILURE = new Failure("SMS_SERVICE_CONNECTION_FAILURE")
  val UNVERIFIED_IDENTIFICATION = new Failure("UNVERIFIED_IDENTIFICATION")
  val SFTP_SCHEDULER_FAILED = new Failure("SFTP_SCHEDULER_FAILED")
  val FORM_FIELDS_CANNOT_BE_EMPTY = new Failure("FORM_FIELDS_CANNOT_BE_EMPTY")
  val CANNOT_FILL_ALL_FIELDS = new Failure("CANNOT_FILL_ALL_FIELDS")
  val COUNTERPARTY_CANNOT_BE_SELF = new Failure("COUNTERPARTY_CANNOT_BE_SELF")
  val COUNTERPARTY_TRADER_FROM_SAME_ORGANIZATION = new Failure("COUNTERPARTY_TRADER_FROM_SAME_ORGANIZATION")
  val INVITATION_EMAIL_ALREADY_SENT = new Failure("INVITATION_EMAIL_ALREADY_SENT")
  val BUYER_FROM_SAME_ORGANIZATION = new Failure("BUYER_FROM_SAME_ORGANIZATION")
  val NOT_PRESENT_AS_COUNTERPARTY = new Failure("NOT_PRESENT_AS_COUNTERPARTY")
  val UBO_TOTAL_SHARE_PERCENTAGE_EXCEEDS_MAXIMUM_VALUE = new Failure("UBO_TOTAL_SHARE_PERCENTAGE_EXCEEDS_MAXIMUM_VALUE")
  val ASSET_PEG_NOT_FOUND = new Failure("ASSET_PEG_NOT_FOUND")
  val CHAT_ROOM_NOT_FOUND = new Failure("CHAT_ROOM_NOT_FOUND")
  val ALL_ORGANIZATION_BACKGROUND_CHECK_FILES_NOT_VERFIED = new Failure("ALL_ORGANIZATION_BACKGROUND_CHECK_FILES_NOT_VERFIED")
  val ALL_TRADER_BACKGROUND_CHECK_FILES_NOT_VERFIED = new Failure("ALL_TRADER_BACKGROUND_CHECK_FILES_NOT_VERFIED")
  val ZONE_ID_MISMATCH = new Failure("ZONE_ID_MISMATCH")
  val ORGANIZATION_ID_MISMATCH = new Failure("ORGANIZATION_ID_MISMATCH")
  val ZONE_INVITATION_NOT_FOUND = new Failure("ZONE_INVITATION_NOT_FOUND")
  val DOCUMENT_LIST_EMPTY = new Failure("DOCUMENT_LIST_EMPTY")
  val DOCUMENT_LIST_LESS_THAN_REQUIRED = new Failure("DOCUMENT_LIST_LESS_THAN_REQUIRED")
  val NEGOTIATION_NOT_FOUND = new Failure("NEGOTIATION_NOT_FOUND")
  val INVALID_PAGE_NUMBER = new Failure("INVALID_PAGE_NUMBER")
  val USERNAME_OR_PASSWORD_INCORRECT = new Failure("USERNAME_OR_PASSWORD_INCORRECT", routes.javascript.AccountController.loginForm)
  val INCORRECT_PASSWORD = new Failure("INCORRECT_PASSWORD")

  class Failure(private val response: String, private val actionController: JavaScriptReverseRoute = null) {
    val message: String = PREFIX + FAILURE_PREFIX + response
    val action: String = utilities.String.getJsRouteString(actionController)
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
