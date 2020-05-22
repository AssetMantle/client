package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

class ProfileControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = profileControllerTest.addIdentification
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object profileControllerTest {

  val addIdentification: ScenarioBuilder = scenario("AddIdentification")
    .exec(http("Add_Identification_Detail_Form")
        .get(routes.AccountController.addIdentificationForm().url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .feed(NameFeeder.nameFeed)
    .feed(IdentificationFeeder.identificationFeed)
    .feed(AddressDataFeeder.addressDataFeed)
    .pause(2)
    .exec(http("IdentificationDetail_Post")
      .post(routes.AccountController.addIdentification().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        Form.FIRST_NAME -> "${%s}".format(Test.TEST_FIRST_NAME),
        Form.LAST_NAME -> "${%s}".format(Test.TEST_LAST_NAME),
        Form.DATE_OF_BIRTH -> "2019-11-11",
        Form.ID_NUMBER -> "${%s}".format(Test.TEST_ID_NUMBER),
        Form.ID_TYPE -> "${%s}".format(Test.TEST_ID_TYPE),
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
    .exec(http("AddIdentificationForm")
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
    .exec(http("AddIdentificationForm")
      .get(routes.AccountController.userReviewIdentificationForm().url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("IdentificationDetail_Post")
      .post(routes.AccountController.userReviewIdentification().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        Form.COMPLETION -> true
      ))
    )
    .pause(2)







  def getAddressFromAccountID(accountID: String):String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "accountAddress" FROM master."Account" WHERE "id" = '$accountID'),'0') AS "accountAddress";""")
    sqlQueryFeeder.apply().next()("accountAddress").toString
  }
}
