package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder
import feeders.JDBCFeeder._

object BackgroundCheckControllerTest {

  val corporateScan: ScenarioBuilder = scenario("CorporateScan")
    .exec(http("Member_Check_Corporate_Scan_Form_Get")
      .get(session => routes.BackgroundCheckController.corporateScanForm(session(Test.TEST_ORGANIZATION_ID).as[String], session(Test.TEST_NAME).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Member Check Corporate Scan)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Member_Check_Corporate_Scan_POST")
      .post(routes.BackgroundCheckController.corporateScan().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        constants.FormField.ORGANIZATION_ID.name -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        constants.FormField.COMPANY_NAME.name -> "${%s}".format(Test.TEST_NAME),
      ))
      .check(status.is(206))
      .check(substring("Member Check Corporate Scan Response").exists)
    )
    .pause(Test.REQUEST_DELAY)
    .exec { session => session.set(Test.TEST_SCAN_ID, getCorporateScanID(session(Test.TEST_NAME).as[String])) }
    .exec(http("Add_Organization_Member_Check_Form_GET")
      .get(session => routes.BackgroundCheckController.addOrganizationMemberCheckForm(session(Test.TEST_ORGANIZATION_ID).as[String], session(Test.TEST_SCAN_ID).as[Int], None).url)
      .check(status.is(200))
      .check(css("legend:contains(Add Organization Member Check)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Add_Organization_Member_Check_POST")
      .post(routes.BackgroundCheckController.addOrganizationMemberCheck().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        constants.FormField.ORGANIZATION_ID.name -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        constants.FormField.SCAN_ID.name -> "${%s}".format(Test.TEST_SCAN_ID),
        constants.FormField.RESULT_ID.name -> "",
        constants.FormField.STATUS.name -> true
      ))
      .check(status.is(200))
      .check(substring("Decision Updated").exists)
    )

  val vesselScan: ScenarioBuilder = scenario("VesselScan")
    .exec(http("Member_Check_Vessel_Scan_Form_Get")
      .get(session => routes.BackgroundCheckController.vesselScanForm(session(Test.TEST_ASSET_ID).as[String], session(Test.TEST_VESSEL_NAME).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Member Check Vessel Scan)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Member_Check_Vessel_Scan_POST")
      .post(routes.BackgroundCheckController.vesselScan().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        constants.FormField.ASSET_ID.name -> "${%s}".format(Test.TEST_ASSET_ID),
        constants.FormField.VESSEL_NAME.name -> "${%s}".format(Test.TEST_VESSEL_NAME),
      ))
      .check(status.is(206))
      .check(substring("Member Check Vessel Scan Response").exists)
    )
    .pause(Test.REQUEST_DELAY)
    .exec { session => session.set(Test.TEST_SCAN_ID, getVesselScanID(session(Test.TEST_VESSEL_NAME).as[String])) }
    .exec(http("Add_Asset_Member_Check_Form_GET")
      .get(session => routes.BackgroundCheckController.addAssetMemberCheckForm(session(Test.TEST_ASSET_ID).as[String], session(Test.TEST_SCAN_ID).as[Int], None).url)
      .check(status.is(200))
      .check(css("legend:contains(Add Asset Member Check)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("AddAssetMemberCheck_POST")
      .post(routes.BackgroundCheckController.addAssetMemberCheck().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        constants.FormField.ASSET_ID.name -> "${%s}".format(Test.TEST_ASSET_ID),
        constants.FormField.SCAN_ID.name -> "${%s}".format(Test.TEST_SCAN_ID),
        constants.FormField.RESULT_ID.name -> "",
        constants.FormField.STATUS.name -> true
      ))
      .check(status.is(200))
      .check(substring("Decision Updated").exists)
    )
    .pause(Test.REQUEST_DELAY)

}
