package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.AssetControllerTest.imageFeed
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

object AccountControllerTest {

  val signUpScenario: ScenarioBuilder = scenario("SignUp")
    .exec(http("Sign_Up_GET")
      .get(routes.AccountController.signUpForm().url)
      .check(status.is(200))
      .check(css("legend:contains(Register)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Sign_Up_POST")
      .post(routes.AccountController.signUp().url)
      .formParamMap(Map(
        constants.FormField.USERNAME.name -> "${%s}".format(Test.TEST_USERNAME),
        constants.FormField.USERNAME_AVAILABLE.name -> true,
        constants.FormField.SIGNUP_PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        constants.FormField.SIGNUP_CONFIRM_PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(206))
      .check(css("legend:contains(Blockchain Passphrase)").exists)
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Create_Wallet_Form_GET")
      .get(session => routes.AccountController.createWalletForm(session(Test.TEST_USERNAME).as[String]).url)
      .check(css("legend:contains(Blockchain Passphrase)").exists)
      .check(status.is(200))
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
      .check(css("[name=%s]".format(Test.MNEMONICS), "value").saveAs(Test.MNEMONICS))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Create_Wallet_POST")
      .post(routes.AccountController.createWallet().url)
      .formParamMap(Map(
        constants.FormField.USERNAME.name -> "${%s}".format(Test.TEST_USERNAME),
        constants.FormField.MNEMONICS.name -> "${%s}".format(Test.MNEMONICS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Signed Up Successfully!").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val loginScenario: ScenarioBuilder = scenario("Login")
    .exec(http("Login_Form_GET")
      .get(routes.AccountController.loginForm().url)
      .check(status.is(200))
      .check(css("legend:contains(Login)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Login_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        constants.FormField.USERNAME.name -> "${%s}".format(Test.TEST_USERNAME),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        constants.FormField.PUSH_NOTIFICATION_TOKEN.name -> "",
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("${%s}".format(Test.TEST_USERNAME)).exists)
      .check(substring("Dashboard").exists)
      .check(substring("Trades").exists)
      .check(substring("Transactions").exists)
      .check(substring("Account").exists)
    )
    .pause(Test.REQUEST_DELAY)


  val loginMain: ScenarioBuilder = scenario("LoginMain")
    .feed(GenesisFeeder.genesisFeed)
    .exec(http("Login_Form_GET")
      .get(routes.AccountController.loginForm().url)
      .check(status.is(200))
      .check(css("legend:contains(%s)".format("Login")).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Login_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        constants.FormField.USERNAME.name -> "${%s}".format(Test.TEST_MAIN_USERNAME),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        constants.FormField.PUSH_NOTIFICATION_TOKEN.name -> "",
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("${%s}".format(Test.TEST_MAIN_USERNAME)).exists)
      .check(substring("Dashboard").exists)
      .check(substring("Trades").exists)
      .check(substring("Transactions").exists)
      .check(substring("Account").exists)

    )
    .pause(Test.REQUEST_DELAY)

  val logoutScenario: ScenarioBuilder = scenario("Logout")
    .exec(http("Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(status.is(200))
      .check(css("legend:contains(Logout)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Test.RECEIVE_NOTIFICATIONS -> false,
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Logged Out Successfully").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val addIdentification: ScenarioBuilder = scenario("AddIdentification")
    .exec(http("Add_Identification_Form")
      .get(routes.AccountController.addIdentificationForm().url)
      .check(status.is(200))
      .check(css("legend:contains(Provide your details below)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .feed(NameFeeder.nameFeed)
    .feed(IdentificationFeeder.identificationFeed)
    .feed(AddressDataFeeder.addressDataFeed)
    .feed(DateFeeder.dateFeed)
    .pause(Test.REQUEST_DELAY)
    .exec(http("Add_Identification_Post")
      .post(routes.AccountController.addIdentification().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        constants.FormField.FIRST_NAME.name -> "${%s}".format(Test.TEST_FIRST_NAME),
        constants.FormField.LAST_NAME.name -> "${%s}".format(Test.TEST_LAST_NAME),
        constants.FormField.DATE_OF_BIRTH.name -> "${%s}".format(Test.TEST_DATE),
        constants.FormField.ID_NUMBER.name -> "${%s}".format(Test.TEST_ID_NUMBER),
        constants.FormField.ID_TYPE.name -> "${%s}".format(Test.TEST_ID_TYPE),
        Form.ADDRESS_ADDRESS_LINE_1 -> "${%s}".format(Test.TEST_ADDRESS_LINE_1),
        Form.ADDRESS_ADDRESS_LINE_2 -> "${%s}".format(Test.TEST_ADDRESS_LINE_2),
        Form.ADDRESS_LANDMARK -> "${%s}".format(Test.TEST_LANDMARK),
        Form.ADDRESS_CITY -> "${%s}".format(Test.TEST_CITY),
        Form.ADDRESS_COUNTRY -> "${%s}".format(Test.TEST_COUNTRY),
        Form.ADDRESS_ZIP_CODE -> "${%s}".format(Test.TEST_ZIP_CODE),
        Form.ADDRESS_PHONE -> "${%s}".format(Test.TEST_PHONE)
      ))
      .check(status.is(206))
      .check(substring("Provide proof of identity").exists)
      .check(css("button:contains(Upload Identification)").exists)
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Upload_Identification_Form")
      .get(routes.FileController.uploadAccountKYCForm("IDENTIFICATION").url)
      .check(status.is(200))
      .check(css("button:contains(Browse)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
  .exec(imageFeed)
    .exec(http("Identification_Upload")
      .post(routes.FileController.uploadAccountKYC("IDENTIFICATION").url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        Form.RESUMABLE_CHUNK_NUMBER -> "1",
        Form.RESUMABLE_CHUNK_SIZE -> "1048576",
        Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
        Form.RESUMABLE_IDENTIFIER -> "document",
        Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED + "${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm
      .check(status.is(200))
    )
    .exec(
      http("Store_Identification")
        .get(session => routes.FileController.storeAccountKYC(session(Test.TEST_FILE_NAME).as[String], "IDENTIFICATION").url)
        .check(status.is(206))
        .check(substring("Provide proof of identity").exists)
        .check(css("button:contains(Update Identification)").exists)
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Review_Identification_Form_GET")
      .get(routes.AccountController.userReviewIdentificationForm().url)
      .check(status.is(200))
      .check(css("legend:contains(User Review Identification)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Review_Identification_POST")
      .post(routes.AccountController.userReviewIdentification().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        Form.COMPLETION -> true
      ))
      .check(status.is(200))
      .check(substring("Identity Details updated successfully").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val changePassword: ScenarioBuilder = scenario("ChangePassword")
    .exec(http("Change_Password_Form_GET")
      .get(routes.AccountController.changePasswordForm().url)
      .check(status.is(200))
      .check(css("legend:contains(Change Password)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Change_Password_POST")
      .post(routes.AccountController.changePassword().url)
      .formParamMap(Map(
        constants.FormField.OLD_PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        constants.FormField.NEW_PASSWORD.name -> ("${%s}".format(Test.TEST_PASSWORD) + constants.Test.NEW_PASSOWRD_SUFFIX),
        constants.FormField.CONFIRM_NEW_PASSWORD.name -> ("${%s}".format(Test.TEST_PASSWORD) + constants.Test.NEW_PASSOWRD_SUFFIX),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Password has been updated").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val forgotPassword: ScenarioBuilder = scenario("Forgot Passowrd")
    .exec(http("Email_OTP_Forgot_Password_Form_GET")
      .get(routes.AccountController.emailOTPForgotPasswordForm().url)
        .check(status.is(200))
      .check(css("legend:contains(Forgot Password)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Email_OTP_Forgot_Password_POST")
      .post(routes.AccountController.emailOTPForgotPassword().url)
      .formParamMap(Map(
        constants.FormField.USERNAME.name -> "${%s}".format(Test.TEST_USERNAME),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(206))
      .check(css("legend:contains(Forgot Password)").exists)
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Forgot_Password_Form_GET")
      .get(session => routes.AccountController.forgotPasswordForm(session(Test.TEST_USERNAME).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Forgot Password)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .feed(OTPFeeder.otpFeed)
    .exec(http("Forgot_Password_POST")
      .post(routes.AccountController.forgotPassword().url)
      .formParamMap(Map(
        constants.FormField.USERNAME.name -> "${%s}".format(Test.TEST_USERNAME),
        constants.FormField.MNEMONICS.name -> "${%s}".format(Test.MNEMONICS),
        constants.FormField.OTP.name -> "${%s}".format(Test.TEST_OTP),
        constants.FormField.NEW_PASSWORD.name -> ("${%s}".format(Test.TEST_PASSWORD) + constants.Test.NEW_PASSOWRD_SUFFIX),
        constants.FormField.CONFIRM_NEW_PASSWORD.name -> ("${%s}".format(Test.TEST_PASSWORD) + constants.Test.NEW_PASSOWRD_SUFFIX),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Password has been updated").exists)
    )
    .pause(Test.REQUEST_DELAY)

}