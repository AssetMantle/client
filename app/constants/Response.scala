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
  val CONTACT_UPDATED = new Success("CONTACT_UPDATED")
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
  val SIGNED_UP = new Success("SIGNED_UP", routes.javascript.LoginController.loginForm)
  val LOGGED_OUT = new Success("LOGGED_OUT")
  val KEY_ADDED = new Success("KEY_ADDED")
  val MOBILE_NUMBER_VERIFIED = new Success("MOBILE_NUMBER_VERIFIED")
  val ORGANIZATION_VERIFIED = new Success("ORGANIZATION_VERIFIED")
  val ZONE_VERIFIED = new Success("ZONE_VERIFIED")
  val VERIFY_ZONE_REJECTED = new Success("VERIFY_ZONE_REJECTED")
  val ORGANIZATION_ADDED = new Success("ORGANIZATION_ADDED")
  val ORGANIZATION_ADDED_FOR_VERIFICATION = new Success("ORGANIZATION_ADDED_FOR_VERIFICATION")
  val SELLER_BID_CHANGED = new Success("SELLER_BID_CHANGED")
  val SELLER_BID_CONFIRMED = new Success("SELLER_BID_CONFIRMED")
  val SELLER_FEEDBACK_SET = new Success("SELLER_FEEDBACK_SET")
  val SELLER_ORDER_EXECUTED = new Success("SELLER_ORDER_EXECUTED")
  val TRADER_ADDED = new Success("TRADER_ADDED")
  val VERIFY_TRADER_REQUEST_REJECTED = new Success("VERIFY_TRADER_REQUEST_REJECTED")
  val COINS_SENT = new Success("COINS_SENT")
  val ZONE_ADDED = new Success("ZONE_ADDED")
  val ZONE_REQUEST_SENT = new Success("ZONE_REQUEST_SENT")
  val VERIFY_ORGANIZATION_REQUEST_REJECTED = new Success("VERIFY_ORGANIZATION_REQUEST_REJECTED")
  val FILE_UPLOAD_SUCCESSFUL = new Success("FILE_UPLOAD_SUCCESSFUL")
  val FILE_UPDATE_SUCCESSFUL = new Success("FILE_UPDATE_SUCCESSFUL")
  val DOCUMENT_APPROVED = new Success("DOCUMENT_APPROVED")
  val PASSWORD_UPDATED = new Success("PASSWORD_UPDATED")
  val OTP_SENT = new Success("OTP_SENT")

  //Warning- for telling that something important is not done and ask to do it
  val VERIFY_MOBILE_NUMBER = new Warning("VERIFY_MOBILE_NUMBER", routes.javascript.VerifyMobileNumberController.verifyMobileNumberForm)
  val VERIFY_EMAIL_ADDRESS = new Warning("VERIFY_EMAIL_ADDRESS", routes.javascript.VerifyEmailAddressController.verifyEmailAddressForm)
  val UPDATE_CONTACT_DETAILS = new Warning("UPDATE_CONTACT_DETAILS", routes.javascript.ContactController.updateContactForm)

  //Failure- for telling that something failed
  val FAILURE = new Failure("FAILURE")
  val NO_SUCH_ELEMENT_EXCEPTION = new Failure("NO_SUCH_ELEMENT_EXCEPTION")
  val NO_SUCH_FILE_EXCEPTION = new Failure("NO_SUCH_FILE_EXCEPTION")
  val PSQL_EXCEPTION = new Failure("PSQL_EXCEPTION")
  val INVALID_OTP = new Failure("INVALID_OTP")
  val EMAIL_ADDRESS_NOT_FOUND = new Failure("EMAIL_ADDRESS_NOT_FOUND")
  val MOBILE_NUMBER_NOT_FOUND = new Failure("MOBILE_NUMBER_NOT_FOUND")
  val CONNECT_EXCEPTION = new Failure("CONNECT_EXCEPTION")
  val EMAIL_NOT_FOUND = new Failure("EMAIL_NOT_FOUND")
  val NO_RESPONSE = new Failure("NO_RESPONSE")
  val NOT_LOGGED_IN = new Failure("NOT_LOGGED_IN")
  val INCORRECT_LOG_IN = new Failure("INCORRECT_LOG_IN")
  val UNVERIFIED_ZONE = new Failure("UNVERIFIED_ZONE")
  val UNVERIFIED_ORGANIZATION = new Failure("UNVERIFIED_ORGANIZATION")
  val REQUEST_ALREADY_APPROVED_OR_REJECTED = new Failure("REQUEST_ALREADY_APPROVED_OR_REJECTED")
  val USERNAME_NOT_FOUND = new Failure("USERNAME_NOT_FOUND")
  val TOKEN_NOT_FOUND = new Failure("TOKEN_NOT_FOUND")
  val TOKEN_TIMEOUT = new Failure("TOKEN_TIMEOUT")
  val INVALID_TOKEN = new Failure("INVALID_TOKEN")
  val UNAUTHORIZED = new Failure("UNAUTHORIZED")
  val DOCUMENT_REJECTED = new Failure("DOCUMENT_REJECTED")
  val PASSWORDS_DO_NOT_MATCH = new Failure("PASSWORDS_DO_NOT_MATCH")
  val USERNAME_UNAVAILABLE = new Failure("USERNAME_UNAVAILABLE")
  val INVALID_USERNAME = new Failure("INVALID_USERNAME")
  val INVALID_PASSWORD = new Failure("INVALID_PASSWORD")
  val NO_FILE = new Failure("NO_FILE")
  val PASSWORD_NOT_GIVEN =  new Failure("PASSWORD_NOT_GIVEN")
  val GAS_NOT_GIVEN =  new Failure("GAS_NOT_GIVEN")
  val ALL_KYC_FILES_NOT_VERIFIED = new Failure("ALL_KYC_FILES_NOT_VERIFIED")

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
