package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

import scala.util.Random

class AddZoneControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = addZoneControllerTest.inviteZoneScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object addZoneControllerTest {

  val inviteZoneScenario: ScenarioBuilder = scenario("InviteZone")
    .feed(EmailAddressFeeder.emailAddressFeed)
    .exec(http("InviteZoneForm_GET")
      .get(routes.AddZoneController.inviteZoneForm().url)
      .check(css("legend:contains(%s)".format("Add Zone")).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("InviteZone_POST")
      .post(routes.AddZoneController.inviteZone().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        constants.FormField.EMAIL_ADDRESS.name -> "${%s}".format(Test.TEST_EMAIL_ADDRESS)
      ))
    )

  val addZoneRequestScenario: ScenarioBuilder = scenario("AddZoneRequest")
    .exec(http("Add_Zone_Form_GET")
      .get(routes.AddZoneController.addZoneForm().url)
      .check(css("legend:contains(%s)".format("Add Zone Details")).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .feed(NameFeeder.nameFeed)
    .feed(CurrencyFeeder.currencyFeed)
    .feed(AddressDataFeeder.addressDataFeed)
    .exec(http("Add_Zone_POST")
      .post(routes.AddZoneController.addZone().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        constants.FormField.NAME.name -> "${%s}".format(Test.TEST_NAME),
        constants.FormField.CURRENCY.name -> "${%s}".format(Test.TEST_CURRENCY),
        Form.ADDRESS_ADDRESS_LINE_1 -> "${%s}".format(Test.TEST_ADDRESS_LINE_1),
        Form.ADDRESS_ADDRESS_LINE_2 -> "${%s}".format(Test.TEST_ADDRESS_LINE_2),
        Form.ADDRESS_LANDMARK -> "${%s}".format(Test.TEST_LANDMARK),
        Form.ADDRESS_CITY -> "${%s}".format(Test.TEST_CITY),
        Form.ADDRESS_COUNTRY -> "${%s}".format(Test.TEST_COUNTRY),
        Form.ADDRESS_ZIP_CODE -> "${%s}".format(Test.TEST_ZIP_CODE),
        Form.ADDRESS_PHONE -> "${%s}".format(Test.TEST_PHONE)
      ))
      .check(substring("Zone KYC").exists)
    )
    .pause(2)
    .foreach(constants.File.ZONE_KYC_DOCUMENT_TYPES, "documentType") {
      feed(ImageFeeder.imageFeed)
        .exec(http("ZoneKYC_Upload_" + "${documentType}" + "_Form_GET")
          .get(session => routes.AddZoneController.userUploadZoneKYCForm(session("documentType").as[String]).url)
          .check(substring("Browse").exists)
          .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
        )
        .pause(2)
        .exec(http("ZoneKYC_Upload_" + "${documentType}")
          .post(session => routes.AddZoneController.userUploadZoneKYC(session("documentType").as[String]).url)
          .formParamMap(Map(
            Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
            Form.RESUMABLE_CHUNK_NUMBER -> "1",
            Form.RESUMABLE_CHUNK_SIZE -> "1048576",
            Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
            Form.RESUMABLE_IDENTIFIER -> "document",
            Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
          .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED + "${%s}".format(Test.TEST_FILE_NAME))
            .transferEncoding("binary")).asMultipartForm)
        .exec(
          http("Store_ZoneKYC_" + "${documentType}")
            .get(session => routes.AddZoneController.userStoreZoneKYC(session(Test.TEST_FILE_NAME).as[String], session("documentType").as[String]).url)
            .check(substring("Zone KYC").exists)
        )
        .pause(2)
    }
    .pause(1)
    .exec(http("User_Review_Add_Zone_Request_Form")
      .get(routes.AddZoneController.userReviewAddZoneRequestForm().url)
      .check(css("legend:contains(%s)".format("Review and Submit Zone details")).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("User_Review_Add_Zone_Request")
      .post(routes.AddZoneController.userReviewAddZoneRequest().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        Form.COMPLETION -> true
      ))
      .check(substring("Zone details submitted for review").exists)
    )
    .pause(2)

  val verifyZoneScenario: ScenarioBuilder = scenario("VerifyZone")
    .foreach(constants.File.ZONE_KYC_DOCUMENT_TYPES, "documentType") {
      exec(http("ZoneKYCUpdate"+"${documentType}"+"StatusForm_GET")
        .get(session => routes.AddZoneController.updateZoneKYCDocumentStatusForm(session(Test.TEST_ZONE_ID).as[String],session("documentType").as[String]).url)
        .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
        .pause(2)
        .exec(http("Zone_KYC_update_Status_" + "${documentType}")
          .post(routes.AddZoneController.updateZoneKYCDocumentStatus().url)
          .formParamMap(Map(
            Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
            constants.FormField.ZONE_ID.name -> "${%s}".format(Test.TEST_ZONE_ID),
            constants.FormField.DOCUMENT_TYPE.name -> "${documentType}",
            constants.FormField.STATUS.name -> true
          ))
        )
        .pause(2)
    }
    .exec(http("Verify_Zone_GET")
      .get(session => routes.AddZoneController.verifyZoneForm(session(Test.TEST_ZONE_ID).as[String]).url)
      .check(css("legend:contains(%s)".format("Verify Zone")).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("Verify_Zone_POST")
      .post(routes.AddZoneController.verifyZone().url)
      .formParamMap(Map(
        constants.FormField.ZONE_ID.name -> "${%s}".format(Test.TEST_ZONE_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        Test.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("Zone approved successfully").exists)
    )
    .pause(2)


  val rejectVerifyZoneScenario: ScenarioBuilder = scenario("RejectVerifyZone")
    .exec(http("RejectVerifyZone_GET")
      .get(routes.AddZoneController.rejectVerifyZoneRequestForm(Test.TEST_ZONE_ID).url)
      .check(css("legend:contains(%s)".format(constants.Form.REJECT_VERIFY_ZONE_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(constants.FormField.ZONE_ID.name), "value").saveAs(Test.TEST_ZONE_ID))
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))

    .pause(2)
    .exec(http("RejectVerifyZone_POST")
      .post(routes.AddZoneController.rejectVerifyZoneRequest().url)
      .formParamMap(Map(
        constants.FormField.ZONE_ID.name -> "${%s}".format(Test.TEST_ZONE_ID),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("SUCCESS VERIFY_ZONE_REJECTED").exists)
    )
    .pause(5)

  def getZoneInvitationID(emailAddress: String)={
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s""" SELECT COALESCE((SELECT "id" FROM master_transaction."ZoneInvitation" WHERE "emailAddress" = '$emailAddress'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getZoneID(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s""" SELECT COALESCE((SELECT "id" FROM master."Zone" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getZoneStatus(query: String): Boolean = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT "status" FROM master."Zone" WHERE "accountID" = '$query';""")
    if (sqlQueryFeeder.apply().next()("status") == true) true
    else false
  }

  def getUnverifiedZone() = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT "id" FROM master."Zone" WHERE "verificationStatus" = null;"""
    )
    sqlQueryFeeder.apply().next().toSeq
  }
}