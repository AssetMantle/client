package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.addOrganizationControllerTest._
import controllersTest.addZoneControllerTest._
import controllersTest.sendCoinControllerTest.getRequestID
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class MasterTest extends Simulation {

  setUp(
    masterTest.masterTestSignUp4.inject(atOnceUsers(4)),
    masterTest.masterLoginAndRequestCoin4.inject(nothingFor(15), atOnceUsers(4)),
    masterTest.masterLoginMainAndApproveFaucetRequest.inject(nothingFor(40), atOnceUsers(1), nothingFor(15), atOnceUsers(1), nothingFor(15), atOnceUsers(1), nothingFor(15), atOnceUsers(1)),
    masterTest.masterZoneLogin1.inject(nothingFor(90), atOnceUsers(1)),
    masterTest.masterLoginMainAndApproveZone1.inject(nothingFor(115), atOnceUsers(1)),
    masterTest.masterOrganizationLogin1.inject(nothingFor(130), atOnceUsers(2)),
    masterTest.masterLoginZoneAndApproveOrganization1.inject(nothingFor(155), atOnceUsers(4)),
  ).protocols(http.baseUrl(Test.BASE_URL))
}

object masterTest {

  val masterTestSignUp4: ScenarioBuilder = scenario("masterTestSignUp4")
    .exec(controllersTest.signUpControllerTest.signUpScenario)

  val masterLoginAndRequestCoin4: ScenarioBuilder = scenario("masterLoginAndRequestCoin4")
    .feed(UsernameFeeder.usernameFeed)
    .feed(PasswordFeeder.passwordFeed)
    .exec(controllersTest.loginControllerTest.loginWithoutSignUpScenario)
    .pause(2)
    .exec(controllersTest.sendCoinControllerTest.requestCoinScenario)

  val masterLoginMainAndApproveFaucetRequest: ScenarioBuilder = scenario("masterLoginMainAndApproveFaucetRequest")
    .feed(GenesisFeeder.genesisFeed)
    .feed(AccountIDFeeder.accountIDFeed)
    .feed(RequestIDFeeder.requestIDFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("MainLogin_GET")
      .get(routes.LoginController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("MainLogin_POST")
      .post(routes.LoginController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_MAIN_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
    .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestID(session(Test.TEST_ACCOUNT_ID).as[String])) }
    .exec { session => println(session); session }
    .exec(http("ApproveFaucetRequest_GET")
      .get(routes.SendCoinController.approveFaucetRequestsForm("${%s}".format(Test.TEST_REQUEST_ID), "${%s}".format(Test.TEST_ACCOUNT_ID)).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("ApproveFaucetRequest_POST")
      .post(routes.SendCoinController.approveFaucetRequests().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_ACCOUNT_ID),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))

  val masterZoneLogin1: ScenarioBuilder = scenario("masterZoneLogin1")
    .feed(UsernameFeeder.usernameFeed)
    .feed(PasswordFeeder.passwordFeed)
    .exec(controllersTest.loginControllerTest.loginWithoutSignUpScenario)
    .exec(controllersTest.addZoneControllerTest.addZoneScenario)

  val masterLoginMainAndApproveZone1: ScenarioBuilder = scenario("masterLoginMainAndApproveZone1")
    .feed(GenesisFeeder.genesisFeed)
    .feed(UsernameFeeder.usernameFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(ZoneIDFeeder.zoneIDFeed)
    .exec(http("MainLogin_GET")
      .get(routes.LoginController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("MainLogin_POST")
      .post(routes.LoginController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_MAIN_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_USERNAME).as[String])) }
    .exec(http("VerifyZone_GET")
      .get(routes.AddZoneController.verifyZoneForm("${%s}".format(Test.TEST_ZONE_ID)).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec { session => println(session); session }
    .exec(http("VerifyZone_POST")
      .post(routes.AddZoneController.verifyZone().url)
      .formParamMap(Map(
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

  val masterOrganizationLogin1: ScenarioBuilder = scenario("masterOrganizationZoneLogin1")
    .feed(UsernameFeeder.usernameFeed)
    .feed(PasswordFeeder.passwordFeed)
    .exec { session => println(session);session }
    .doIfOrElse(session => session(Test.ID).as[String].toInt % 4==1) {
      exec { session => println("InsideLogin" + session); session }
        .exec { session => session.set(Test.TEST_ZONE_ID, getVerifiedZoneID()) }
        .exec(controllersTest.loginControllerTest.loginWithoutSignUpScenario)
        .exec(controllersTest.addOrganizationControllerTest.addOrganizationScenario)
    }{exec { session => println("OutsideLogin"+session); session }}

  val masterLoginZoneAndApproveOrganization1: ScenarioBuilder = scenario("masterLoginZoneAndApproveOrganization1")
    .feed(GenesisFeeder.genesisFeed)
    .feed(UsernameFeeder.usernameFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(ZoneIDFeeder.zoneIDFeed)
    .feed(OrganizationIDFeeder.organizationIDFeed)
    .exec { session => session.set(Test.TEST_ZONE_ID, getVerifiedZoneID()) }
    // .doIfOrElse(session => session(Test.ID).as[String].toInt==1) {
    .doIfOrElse(session => session(Test.TEST_ZONE_ID).as[String]==getZoneID(session(Test.TEST_USERNAME).as[String])) {
        exec { session => println("InsideApprove"+session); session }
        .exec(http("VerifiedZoneLogin_GET")
          .get(routes.LoginController.loginForm().url)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .exec(http("VerifiedZoneLogin_POST")
          .post(routes.LoginController.login().url)
          .formParamMap(Map(
            Form.USERNAME -> "${%s}".format(Test.TEST_USERNAME),
            Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
            Form.NOTIFICATION_TOKEN -> "",
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(2)
        .exec { session => session.set(Test.TEST_ORGANIZATION_ID, getUnverifiedOrganizationID(session(Test.TEST_ZONE_ID).as[String])) }
        .exec(http("VerifyOrganization_GET")
          .get(routes.AddOrganizationController.verifyOrganizationForm("${%s}".format(Test.TEST_ORGANIZATION_ID), "${%s}".format(Test.TEST_ZONE_ID)).url)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .pause(2)
        .exec { session => println("InsideVerification"+session); session }
        .exec(http("VerifyOrganization_POST")
          .post(routes.AddOrganizationController.verifyOrganization().url)
          .formParamMap(Map(
            Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
            Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
            Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(5)
    }{exec { session => println("OutsideApprove"+session); session }}

}

