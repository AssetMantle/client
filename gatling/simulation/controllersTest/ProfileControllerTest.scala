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
    .exec(http("Add_IdentificationDocument")
        .get(routes.ProfileController.identificationDocument().url)
      .check(substring("ID Proof").exists)
    )
    .pause(2)
    .exec(http("AddIdentificationForm")
        .get(routes.FileController.uploadAccountKYCForm("IDENTIFICATION").url)
      .check(substring("BROWSE").exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(ImageFeeder.imageFeed)
    .exec(http("IdentificationUpload")
        .post(routes.FileController.uploadAccountKYC("IDENTIFICATION").url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
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
        .check(substring("ID Proof").exists)
    )
    .pause(2)
    .feed(NameFeeder.nameFeed)
    .feed(IdentificationFeeder.identificationFeed)
    .exec(http("IdentificationDetailForm_GET")
        .get(routes.ProfileController.identificationForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.IDENTIFICATION.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .exec(http("IdentificationDetail_Post")
        .post(routes.ProfileController.identification().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.FIRST_NAME -> "${%s}".format(Test.TEST_FIRST_NAME),
        Form.LAST_NAME -> "${%s}".format(Test.TEST_LAST_NAME),
        Form.DATE_OF_BIRTH -> "2019-11-11",
        Form.ID_NUMBER -> "${%s}".format(Test.TEST_ID_NUMBER),
        Form.ID_TYPE -> "${%s}".format(Test.TEST_ID_TYPE)
      ))
      .check(substring("SUCCESS RESPONSE.SUCCESS.IDENTIFICATION_ADDED").exists)
    )



  def getAddressFromAccountID(accountID: String):String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "accountAddress" FROM master."Account" WHERE "id" = '$accountID'),'0') AS "accountAddress";""")
    sqlQueryFeeder.apply().next()("accountAddress").toString
  }
}
