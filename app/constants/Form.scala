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

  //AccountController
  val SIGN_UP = new Form("SIGN_UP", routes.AccountController.signUp(), routes.javascript.AccountController.signUpForm)
  val CREATE_WALLET = new Form("CREATE_WALLET", routes.AccountController.createWallet(), routes.javascript.AccountController.createWalletForm)
  val LOGIN = new Form("LOGIN", routes.AccountController.login(), routes.javascript.AccountController.loginForm)
  val LOGOUT = new Form("LOGOUT", routes.AccountController.logout(), routes.javascript.AccountController.logoutForm)
  val CHANGE_PASSWORD = new Form("CHANGE_PASSWORD", routes.AccountController.changePassword(), routes.javascript.AccountController.changePasswordForm)
  val EMAIL_OTP_FORGOT_PASSWORD = new Form("EMAIL_OTP_FORGOT_PASSWORD", routes.AccountController.emailOTPForgotPassword(), routes.javascript.AccountController.emailOTPForgotPasswordForm)
  val FORGOT_PASSWORD = new Form("FORGOT_PASSWORD", routes.AccountController.forgotPassword(), routes.javascript.AccountController.forgotPasswordForm)
  val ADD_IDENTIFICATION = new Form("ADD_IDENTIFICATION", routes.AccountController.addIdentification(), routes.javascript.AccountController.addIdentificationForm)
  val USER_REVIEW_IDENTIFICATION = new Form("USER_REVIEW_IDENTIFICATION", routes.AccountController.userReviewIdentification(), routes.javascript.AccountController.userReviewIdentificationForm)

  //BackgroundCheckController
  val MEMBER_CHECK_MEMBER_SCAN = new Form("MEMBER_CHECK_MEMBER_SCAN", routes.BackgroundCheckController.memberScan(), routes.javascript.BackgroundCheckController.memberScanForm)
  val MEMBER_CHECK_MEMBER_SCAN_RESULT_DECISION = new Form("MEMBER_CHECK_MEMBER_SCAN_RESULT_DECISION", routes.BackgroundCheckController.memberScanResultDecision(), routes.javascript.BackgroundCheckController.memberScanResultDecisionForm)
  val ADD_UBO_MEMBER_CHECK = new Form("ADD_UBO_MEMBER_CHECK", routes.BackgroundCheckController.addUBOMemberCheck(), routes.javascript.BackgroundCheckController.addUBOMemberCheckForm)
  val MEMBER_CHECK_CORPORATE_SCAN = new Form("MEMBER_CHECK_CORPORATE_SCAN", routes.BackgroundCheckController.corporateScan(), routes.javascript.BackgroundCheckController.corporateScanForm)
  val MEMBER_CHECK_CORPORATE_SCAN_RESULT_DECISION = new Form("MEMBER_CHECK_CORPORATE_SCAN_RESULT_DECISION", routes.BackgroundCheckController.corporateScanResultDecision(), routes.javascript.BackgroundCheckController.corporateScanResultDecisionForm)
  val ADD_ORGANIZATION_MEMBER_CHECK = new Form("ADD_ORGANIZATION_MEMBER_CHECK", routes.BackgroundCheckController.addOrganizationMemberCheck(), routes.javascript.BackgroundCheckController.addOrganizationMemberCheckForm)
  val MEMBER_CHECK_VESSEL_SCAN = new Form("MEMBER_CHECK_VESSEL_SCAN", routes.BackgroundCheckController.vesselScan(), routes.javascript.BackgroundCheckController.vesselScanForm)
  val MEMBER_CHECK_VESSEL_SCAN_RESULT_DECISION = new Form("MEMBER_CHECK_VESSEL_SCAN_RESULT_DECISION", routes.BackgroundCheckController.vesselScanResultDecision(), routes.javascript.BackgroundCheckController.vesselScanResultDecisionForm)
  val ADD_ASSET_MEMBER_CHECK = new Form("ADD_ASSET_MEMBER_CHECK", routes.BackgroundCheckController.addAssetMemberCheck(), routes.javascript.BackgroundCheckController.addAssetMemberCheckForm)

  //ChatController
  val SEND_MESSAGE = new Form("SEND_MESSAGE", routes.ChatController.sendMessage(), routes.javascript.ChatController.sendMessageForm)

  //ContactController
  val ADD_OR_UPDATE_EMAIL_ADDRESS = new Form("ADD_OR_UPDATE_EMAIL_ADDRESS", routes.ContactController.addOrUpdateEmailAddress(), routes.javascript.ContactController.addOrUpdateEmailAddressForm)
  val ADD_OR_UPDATE_MOBILE_NUMBER = new Form("ADD_OR_UPDATE_MOBILE_NUMBER", routes.ContactController.addOrUpdateMobileNumber(), routes.javascript.ContactController.addOrUpdateMobileNumberForm)
  val VERIFY_EMAIL_ADDRESS = new Form("VERIFY_EMAIL_ADDRESS", routes.ContactController.verifyEmailAddress(), routes.javascript.ContactController.verifyEmailAddressForm)
  val VERIFY_MOBILE_NUMBER = new Form("VERIFY_MOBILE_NUMBER", routes.ContactController.verifyMobileNumber(), routes.javascript.ContactController.verifyMobileNumberForm)

  //SendCoinController
  val SEND_COIN = new Form("SEND_COIN", routes.SendCoinController.sendCoin(), routes.javascript.SendCoinController.sendCoinForm)
  val BLOCKCHAIN_SEND_COIN = new Form("BLOCKCHAIN_SEND_COIN", routes.SendCoinController.blockchainSendCoin(), routes.javascript.SendCoinController.blockchainSendCoinForm)

  //Western Union - Please Do not change.
  val WU_RTCB_ID = "id"
  val REFERENCE = "reference"
  val EXTERNAL_REFERENCE = "externalReference"
  val INVOICE_NUMBER = "invoiceNumber"
  val BUYER_BUSINESS_ID = "buyerBusinessId"
  val BUYER_FIRST_NAME = "buyerFirstName"
  val BUYER_LAST_NAME = "buyerLastName"
  val CREATED_DATE = "createdDate"
  val LAST_UPDATED_DATE = "lastUpdatedDate"
  val WU_RTCB_STATUS = "status"
  val DEAL_TYPE = "dealType"
  val PAYMENT_TYPE_ID = "paymentTypeId"
  val PAID_OUT_AMOUNT = "paidOutAmount"
  val REQUEST_SIGNATURE = "requestSignature"
  val CLIENT_ID = "clientId"
  val CLIENT_REFERENCE = "clientReference"
  val WU_SFTP_BUYER_ID = "buyer.id"
  val WU_SFTP_BUYER_FIRST_NAME = "buyer.firstName"
  val WU_SFTP_BUYER_LAST_NAME = "buyer.lastName"
  val WU_SFTP_BUYER_ADDRESS = "buyer.address"
  val BUYER_CITY = "buyer.city"
  val BUYER_ZIP = "buyer.zip"
  val BUYER_EMAIL = "buyer.email"
  val SERVICE_ID = "service.id"
  val SERVICE_AMOUNT = "service.amount"

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
