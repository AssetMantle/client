package constants

import controllers.routes
import play.api.mvc.Call
import play.api.routing.JavaScriptReverseRoute

class Form(template: String, val route: Call, val get: JavaScriptReverseRoute) {
  val legend: String = Seq("FORM", template, "LEGEND").mkString(".")
  val submit: String = Seq("FORM", template, "SUBMIT").mkString(".")
  val button: String = Seq("FORM", template, "BUTTON").mkString(".")
}

object Form {
  //ContactController
  val ADD_OR_UPDATE_EMAIL_ADDRESS = new Form("ADD_OR_UPDATE_EMAIL_ADDRESS", routes.ContactController.addOrUpdateEmailAddress, routes.javascript.ContactController.addOrUpdateEmailAddressForm)
  val ADD_OR_UPDATE_MOBILE_NUMBER = new Form("ADD_OR_UPDATE_MOBILE_NUMBER", routes.ContactController.addOrUpdateMobileNumber, routes.javascript.ContactController.addOrUpdateMobileNumberForm)
  val VERIFY_EMAIL_ADDRESS = new Form("VERIFY_EMAIL_ADDRESS", routes.ContactController.verifyEmailAddress, routes.javascript.ContactController.verifyEmailAddressForm)
  val VERIFY_MOBILE_NUMBER = new Form("VERIFY_MOBILE_NUMBER", routes.ContactController.verifyMobileNumber, routes.javascript.ContactController.verifyMobileNumberForm)

  //Gatling Test
  val ADDRESS_ADDRESS_LINE_1 = "ADDRESS.ADDRESS_LINE_1"
  val ADDRESS_ADDRESS_LINE_2 = "ADDRESS.ADDRESS_LINE_2"
  val ADDRESS_LANDMARK = "ADDRESS.LANDMARK"
  val ADDRESS_CITY = "ADDRESS.CITY"
  val ADDRESS_COUNTRY = "ADDRESS.COUNTRY"
  val ADDRESS_ZIP_CODE = "ADDRESS.ZIP_CODE"
  val ADDRESS_PHONE = "ADDRESS.PHONE"
  val COMPLETION = "COMPLETION"
  val MODE = "MODE"

  val RESUMABLE_CHUNK_NUMBER = "resumableChunkNumber"
  val RESUMABLE_CHUNK_SIZE = "resumableChunkSize"
  val RESUMABLE_TOTAL_SIZE = "resumableTotalSize"
  val RESUMABLE_IDENTIFIER = "resumableIdentifier"
  val RESUMABLE_FILE_NAME = "resumableFilename"

}
