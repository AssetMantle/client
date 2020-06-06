package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

class BackgroundCheckControllerTest extends Simulation {

  setUp(
    blockchainTest.blockChainTestSignUp4.inject(atOnceUsers(4)),
    blockchainTest.loginMain.inject(atOnceUsers(1)),
    blockchainTest.blockChainTestSendCoinFromMainTo4.inject(atOnceUsers(1), nothingFor(14), atOnceUsers(1), nothingFor(14), atOnceUsers(1), nothingFor(14), atOnceUsers(1), nothingFor(14))
  ).protocols(http.baseUrl(Test.BASE_URL))
}

object backgroundCheckControllerTest {

  val corporateScan: ScenarioBuilder = scenario("CorporateScan")
    .exec(http("Member_Check_Corporate_Scan_Form_Get")
      .get(session=>routes.BackgroundCheckController.corporateScanForm(session(Test.TEST_ORGANIZATION_ID).as[String],session(Test.TEST_NAME).as[String]).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Member_Check_Corporate_Scan_POST")
      .post(routes.BackgroundCheckController.corporateScan().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        constants.FormField.ORGANIZATION_ID.name -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        constants.FormField.COMPANY_NAME.name -> "${%s}".format(Test.TEST_NAME),
      ))
    )
    .pause(2)
    .exec { session => session.set(Test.TEST_SCAN_ID, getCorporateScanID(session(Test.TEST_NAME).as[String])) }
    .exec(http("AddOrganizationMemberCheckForm_GET")
      .get(session=>routes.BackgroundCheckController.addOrganizationMemberCheckForm(session(Test.TEST_ORGANIZATION_ID).as[String],session(Test.TEST_SCAN_ID).as[Int],None).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("AddOrganizationMemberCheck_POST")
      .post(routes.BackgroundCheckController.addOrganizationMemberCheck().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        constants.FormField.ORGANIZATION_ID.name -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        constants.FormField.SCAN_ID.name -> "${%s}".format(Test.TEST_SCAN_ID),
        constants.FormField.RESULT_ID.name -> "",
        constants.FormField.STATUS.name -> true
      ))
      .check(substring("Decision Updated").exists)
    )

  val vesselScan: ScenarioBuilder = scenario("VesselScan")
    .exec(http("Member_Check_Vessel_Scan_Form_Get")
      .get(session=>routes.BackgroundCheckController.vesselScanForm(session(Test.TEST_ASSET_ID).as[String],session(Test.TEST_VESSEL_NAME).as[String]).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Member_Check_Vessel_Scan_POST")
      .post(routes.BackgroundCheckController.vesselScan().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        constants.FormField.ASSET_ID.name -> "${%s}".format(Test.TEST_ASSET_ID),
        constants.FormField.VESSEL_NAME.name -> "${%s}".format(Test.TEST_VESSEL_NAME),
      ))
    )
    .pause(2)
    .exec { session => session.set(Test.TEST_SCAN_ID, getVesselScanID(session(Test.TEST_VESSEL_NAME).as[String])) }
    .exec(http("AddAssetMemberCheckForm_GET")
      .get(session=>routes.BackgroundCheckController.addAssetMemberCheckForm(session(Test.TEST_ASSET_ID).as[String],session(Test.TEST_SCAN_ID).as[Int],None).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("AddAssetMemberCheck_POST")
      .post(routes.BackgroundCheckController.addAssetMemberCheck().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        constants.FormField.ASSET_ID.name -> "${%s}".format(Test.TEST_ASSET_ID),
        constants.FormField.SCAN_ID.name -> "${%s}".format(Test.TEST_SCAN_ID),
        constants.FormField.RESULT_ID.name -> "",
        constants.FormField.STATUS.name -> true
      ))
      .check(substring("Decision Updated").exists)
    )
    .pause(2)

  def getVesselScanID(query: String) = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://18.136.170.155:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "scanID" FROM member_check."VesselScan" WHERE "vesselName" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getCorporateScanID(query: String) = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://18.136.170.155:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "scanID" FROM member_check."CorporateScan" WHERE "companyName" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }
}
