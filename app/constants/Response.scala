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
  //Success- for telling if something is done and the further steps opened up because of it
  val LOGGED_IN = new Success("LOGGED_IN")
  val SIGNED_UP = new Success("SIGNED_UP", routes.javascript.LoginController.loginForm)
  val LOGGED_OUT = new Success("LOGGED_OUT")
  val KEY_ADDED = new Success("KEY_ADDED")
  val MOBILE_NUMBER_VERIFIED = new Success("MOBILE_NUMBER_VERIFIED")
  val EMAIL_ADDRESS_VERIFIED = new Success("EMAIL_ADDRESS_VERIFIED")
  val CONTACT_UPDATED = new Success("CONTACT_UPDATED")
  val VERIFY_ORGANIZATION = new Success("VERIFY_ORGANIZATION")
  val VERIFY_ZONE = new Success("VERIFY_ZONE")
  val VERIFY_ZONE_REJECTED = new Success("VERIFY_ZONE_REJECTED")
  val ADD_ORGANIZATION = new Success("ADD_ORGANIZATION")
  val ADD_ZONE = new Success("ADD_ZONE")
  val REQUEST_COINS = new Success("REQUEST_COINS")
  val APPROVED_FAUCET_REQUEST = new Success("APPROVED_FAUCET_REQUEST")
  val ISSUE_ASSET_REQUEST = new Success("ISSUE_ASSET_REQUEST")
  val ISSUE_FIAT_REQUEST = new Success("ISSUE_FIAT_REQUEST")
  val ISSUE_ASSET_REQUEST_REJECTED = new Success("ISSUE_ASSET_REQUEST_REJECTED")
  val ISSUE_FIAT_REQUEST_REJECTED = new Success("ISSUE_FIAT_REQUEST_REJECTED")
  val VERIFY_ORGANIZATION_REQUEST_REJECTED = new Success("VERIFY_ORGANIZATION_REQUEST_REJECTED")

  //Warning- for telling that something important is not done and ask to do it
  val VERIFY_MOBILE_NUMBER = new Warning("VERIFY_MOBILE_NUMBER", routes.javascript.VerifyMobileNumberController.verifyMobileNumberForm)
  val VERIFY_EMAIL_ADDRESS = new Warning("VERIFY_EMAIL_ADDRESS", routes.javascript.VerifyEmailAddressController.verifyEmailAddressForm)
  val UPDATE_CONTACT_DETAILS = new Warning("UPDATE_CONTACT_DETAILS", routes.javascript.UpdateContactController.updateContactForm)

  //Failure- for telling that something failed
  val NO_SUCH_ELEMENT_EXCEPTION = new Failure("NO_SUCH_ELEMENT_EXCEPTION")
  val PSQL_EXCEPTION = new Failure("PSQL_EXCEPTION")
  val INVALID_OTP = new Failure("INVALID_OTP")
  val EMAIL_ADDRESS_NOT_FOUND = new Failure("EMAIL_ADDRESS_NOT_FOUND")
  val MOBILE_NUMBER_NOT_FOUND = new Failure("MOBILE_NUMBER_NOT_FOUND")
  val BASE_EXCEPTION = new Failure("BASE_EXCEPTION")
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
