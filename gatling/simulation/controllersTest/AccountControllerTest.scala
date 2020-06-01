package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

class AccountControllerTest extends Simulation {
  val scenarioBuilder: ScenarioBuilder = accountControllerTest.signUpScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object accountControllerTest {

  val signUpScenario: ScenarioBuilder = scenario("SignUp")
    .exec(http("SignUp_GET")
      .get(routes.AccountController.signUpForm().url)
      .check(substring("Register").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("SignUp_POST")
      .post(routes.AccountController.signUp().url)
      .formParamMap(Map(
        constants.FormField.USERNAME.name -> "${%s}".format(Test.TEST_USERNAME),
        constants.FormField.USERNAME_AVAILABLE.name -> true,
        constants.FormField.SIGNUP_PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        constants.FormField.SIGNUP_CONFIRM_PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("Blockchain Passphrase").exists)
    )
    .pause(2)
    .exec(http("CreateWallet_GET")
      .get(session=>routes.AccountController.createWalletForm(session(Test.TEST_USERNAME).as[String]).url)
      .check(substring("Blockchain Passphrase").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
      .check(css("[name=%s]".format(Test.MNEMONICS), "value").saveAs(Test.MNEMONICS))
    )
    .pause(2)
    .exec(http("CreateWallet_POST")
      .post(routes.AccountController.createWallet().url)
      .formParamMap(Map(
        constants.FormField.USERNAME.name -> ("${%s}".format(Test.TEST_USERNAME)),
        constants.FormField.MNEMONICS.name->"${%s}".format(Test.MNEMONICS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("Signed Up Successfully!").exists)
    )
    .pause(5)

  val loginScenario: ScenarioBuilder = scenario("Login")
    .exec(http("LoginForm_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format("Login")).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Login_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        constants.FormField.USERNAME.name -> "${%s}".format(Test.TEST_USERNAME),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        constants.FormField.PUSH_NOTIFICATION_TOKEN.name -> "",
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("${%s}".format(Test.TEST_USERNAME)).exists)
      .check(substring("Dashboard").exists)
      .check(substring("Trades").exists)
      .check(substring("Transactions").exists)
      .check(substring("Account").exists)

    )
    .pause(5)


  val loginMain: ScenarioBuilder = scenario("LoginMain")
    .feed(GenesisFeeder.genesisFeed)
    .exec(http("Login_Main_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format("Login")).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .exec(http("Login_Main_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        constants.FormField.USERNAME.name -> "${%s}".format(Test.TEST_MAIN_USERNAME),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        constants.FormField.PUSH_NOTIFICATION_TOKEN.name -> "",
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("${%s}".format(Test.TEST_MAIN_USERNAME)).exists)
    )
    .pause(5)

  val logoutScenario: ScenarioBuilder = scenario("Logout")
    .exec(http("Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format("Logout")).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Test.RECEIVE_NOTIFICATIONS -> false,
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("Logged Out Successfully").exists)
    )
    .pause(2)

  val addIdentification: ScenarioBuilder = scenario("AddIdentification")
    .exec(http("Add_Identification_Form")
      .get(routes.AccountController.addIdentificationForm().url)
      .check(css("legend:contains(%s)".format("Provide your details below")).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .feed(NameFeeder.nameFeed)
    .feed(IdentificationFeeder.identificationFeed)
    .feed(AddressDataFeeder.addressDataFeed)
    .pause(2)
    .exec(http("AddIdentification_Post")
      .post(routes.AccountController.addIdentification().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        constants.FormField.FIRST_NAME.name -> "${%s}".format(Test.TEST_FIRST_NAME),
        constants.FormField.LAST_NAME.name -> "${%s}".format(Test.TEST_LAST_NAME),
        constants.FormField.DATE_OF_BIRTH.name -> "2019-11-11",
        constants.FormField.ID_NUMBER.name -> "${%s}".format(Test.TEST_ID_NUMBER),
        constants.FormField.ID_TYPE.name -> "${%s}".format(Test.TEST_ID_TYPE),
        Form.ADDRESS_ADDRESS_LINE_1 -> "${%s}".format(Test.TEST_ADDRESS_LINE_1),
        Form.ADDRESS_ADDRESS_LINE_2 -> "${%s}".format(Test.TEST_ADDRESS_LINE_2),
        Form.ADDRESS_LANDMARK -> "${%s}".format(Test.TEST_LANDMARK),
        Form.ADDRESS_CITY -> "${%s}".format(Test.TEST_CITY),
        Form.ADDRESS_COUNTRY ->"${%s}".format(Test.TEST_COUNTRY),
        Form.ADDRESS_ZIP_CODE -> "${%s}".format(Test.TEST_ZIP_CODE),
        Form.ADDRESS_PHONE -> "${%s}".format(Test.TEST_PHONE)
      ))
    )
    .pause(2)
    .exec(http("UploadIdentificationForm")
      .get(routes.FileController.uploadAccountKYCForm("IDENTIFICATION").url)
      .check(substring("BROWSE").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .feed(ImageFeeder.imageFeed)
    .exec(http("IdentificationUpload")
      .post(routes.FileController.uploadAccountKYC("IDENTIFICATION").url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        Form.RESUMABLE_CHUNK_NUMBER -> "1",
        Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
        Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
        Form.RESUMABLE_IDENTIFIER -> "document",
        Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED+"${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm)
    .exec(
      http("Store_Identification")
        .get(session=>routes.FileController.storeAccountKYC(session(Test.TEST_FILE_NAME).as[String],"IDENTIFICATION").url)
    )
    .pause(2)
    .exec(http("ReviewIdentificationForm")
      .get(routes.AccountController.userReviewIdentificationForm().url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("ReviewIdentification_Post")
      .post(routes.AccountController.userReviewIdentification().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        Form.COMPLETION -> true
      ))
      .check(substring("Identity Details updated successfully").exists)
    )
    .pause(2)

  def getUserType(query: String):String={
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "userType" FROM master."Account" WHERE id = '$query'),'0') AS "userType";""")
    sqlQueryFeeder.apply().next()("userType").toString
  }
}