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

  val scenarioBuilder: ScenarioBuilder = addZoneControllerTest.blockchainAddZoneScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object addZoneControllerTest {

  val zoneKYCs=Seq("BANK_ACCOUNT_DETAIL","IDENTIFICATION")

  val addZoneRequestScenario: ScenarioBuilder = scenario("AddZoneRequest")

    .exec(http("Add_Zone_Form_GET")
      .get(routes.AddZoneController.addZoneForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.ADD_ZONE.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .feed(NameFeeder.nameFeed)
    .feed(CurrencyFeeder.currencyFeed)
    .feed(AddressDataFeeder.addressDataFeed)
    .exec(http("Add_Zone_POST")
      .post(routes.AddZoneController.addZone().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.NAME -> "${%s}".format(Test.TEST_NAME),
        Form.CURRENCY -> "${%s}".format(Test.TEST_CURRENCY),
        Form.ADDRESS_ADDRESS_LINE_1 -> "${%s}".format(Test.TEST_ADDRESS_LINE_1),
        Form.ADDRESS_ADDRESS_LINE_2 -> "${%s}".format(Test.TEST_ADDRESS_LINE_2),
        Form.ADDRESS_LANDMARK -> "${%s}".format(Test.TEST_LANDMARK),
        Form.ADDRESS_CITY -> "${%s}".format(Test.TEST_CITY),
        Form.ADDRESS_COUNTRY ->"${%s}".format(Test.TEST_COUNTRY),
        Form.ADDRESS_ZIP_CODE -> "${%s}".format(Test.TEST_ZIP_CODE),
        Form.ADDRESS_PHONE -> "${%s}".format(Test.TEST_PHONE)
      ))
      .check(substring("ZONE_KYC_FILES").exists)
    )
    .pause(2)
    .foreach(zoneKYCs,"documentType"){
         feed(ImageFeeder.imageFeed)
        .exec(http("ZoneKYC_Upload_"+"${documentType}"+"_Form_GET")
          .get(session=>routes.AddZoneController.userUploadZoneKYCForm(session("documentType").as[String]).url)
          .check(substring("BROWSE").exists)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
        )
        .pause(2)
           .exec(http("ZoneKYC_Upload_"+"${documentType}")
             .post(session=>routes.AddZoneController.userUploadZoneKYC(session("documentType").as[String]).url)
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
             http("Store_ZoneKYC_"+"${documentType}")
               .get(session=>routes.AddZoneController.userStoreZoneKYC(session(Test.TEST_FILE_NAME).as[String],session("documentType").as[String]).url)
               .check(substring("ZONE_KYC_FILES").exists)
           )
        .pause(2)
    }
    .pause(1)
    .exec(http("User_Review_Add_Zone_Request_Form")
        .get(routes.AddZoneController.userReviewAddZoneRequestForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.REVIEW_ADD_ZONE_ON_COMPLETION.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("User_Review_Add_Zone_Request")
        .post(routes.AddZoneController.userReviewAddZoneRequest().url)
        .formParamMap(Map(
          Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
          Form.COMPLETION -> true
      ))
      .check(substring("SUCCESS ZONE_ADDED_FOR_VERIFICATION").exists)
    )
    .pause(2)

  val verifyZoneScenario: ScenarioBuilder = scenario("VerifyZone")

    .exec(http("Get_Pending_Verify_Zone_Request")
      .get(routes.AddZoneController.viewPendingVerifyZoneRequests().url)
      .check(substring("${%s}".format(Test.TEST_ZONE_ID)).exists)
    )
    .pause(1)
    .foreach(zoneKYCs,"documentType"){
      exec(http("Zone_KYC_update_Status"+"${documentType}")
        .post(routes.AddZoneController.updateZoneKYCDocumentStatus().url)
        .formParamMap(Map(
          Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
          Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
          Form.DOCUMENT_TYPE -> "${documentType}",
          Form.STATUS -> true
        ))
      )
        .pause(2)
    }
    .exec(http("Verify_Zone_GET")
      .get(session=>routes.AddZoneController.verifyZoneForm(session(Test.TEST_ZONE_ID).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.VERIFY_ZONE.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("Verify_Zone_POST")
      .post(routes.AddZoneController.verifyZone().url)
      .formParamMap(Map(
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD ->"${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS ZONE_VERIFIED").exists)
    )
    .pause(2)


  val rejectVerifyZoneScenario: ScenarioBuilder = scenario("RejectVerifyZone")
    .exec(http("RejectVerifyZone_GET")
      .get(routes.AddZoneController.rejectVerifyZoneRequestForm(Test.TEST_ZONE_ID).url)
      .check(css("legend:contains(%s)".format(constants.Form.REJECT_VERIFY_ZONE_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Form.ZONE_ID), "value").saveAs(Test.TEST_ZONE_ID))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))

    .pause(2)
    .exec(http("RejectVerifyZone_POST")
      .post(routes.AddZoneController.rejectVerifyZoneRequest().url)
      .formParamMap(Map(
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS VERIFY_ZONE_REJECTED").exists)
    )
    .pause(5)

  val blockchainAddZoneScenario: ScenarioBuilder = scenario("BlockchainAddZone")
    .feed(FromFeeder.fromFeed)
    .feed(ToFeeder.toFeed)
    .feed(ZoneIDFeeder.zoneIDFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(GasFeeder.gasFeed)
    .feed(ModeFeeder.modeFeed)
    .exec(http("BlockchainAddZone_GET")
      .get(routes.AddZoneController.blockchainAddZoneForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.BLOCKCHAIN_ADD_ZONE.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainAddZone_POST")
      .post(routes.AddZoneController.blockchainAddZone().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.MODE ->"${%s}".format(Test.TEST_MODE),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      check(substring("SUCCESS ZONE_ADDED").exists)
    )

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

  def getUnverifiedZone()={
    val sqlQueryFeeder=jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT "id" FROM master."Zone" WHERE "verificationStatus" = null;"""
    )
    sqlQueryFeeder.apply().next().toSeq
  }
}