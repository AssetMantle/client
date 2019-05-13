package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class AddOrganizationControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = addOrganizationControllerTest.addOrganizationScenario
  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
}

object addOrganizationControllerTest {

  val addOrganizationScenario: ScenarioBuilder = scenario("AddOrganization")
    .feed(ZoneIDFeeder.zoneIDFeed)
    .feed(NameFeeder.nameFeed)
    .feed(AddressFeeder.addressFeed)
    .feed(EmailAddressFeeder.emailAddressFeed)
    .feed(MobileNumberFeeder.mobileNumberFeed)
    .exec(http("AddOrganization_GET")
      .get(routes.AddOrganizationController.addOrganizationForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("AddOrganization_POST")
      .post(routes.AddOrganizationController.addOrganizationForm().url)
      .formParamMap(Map(
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.NAME -> "${%s}".format(Test.TEST_NAME),
        Form.ADDRESS -> "${%s}".format(Test.TEST_ADDRESS),
        Form.EMAIL -> "${%s}".format(Test.TEST_EMAIL_ADDRESS),
        Form.PHONE -> "${%s}".format(Test.TEST_MOBILE_NUMBER),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

  val verifyOrganizationScenario: ScenarioBuilder = scenario("VerifyOrganization")
    .feed(ZoneIDFeeder.zoneIDFeed)
    .feed(OrganizationIDFeeder.organizationIDFeed)
    .feed(PasswordFeeder.passwordFeed)
  /*
      .exec(http("VerifyOrganization_GET")
        .get(routes.AddOrganizationController.verifyOrganizationForm().url)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
      .pause(2)
      .exec(http("VerifyOrganization_POST")
        .post(routes.AddOrganizationController.verifyOrganization().url)
    .formParamMap(Map(
      Form.ZONE_ID ->  "${%s}".format(Test.TEST_ZONE_ID),
      Form.ORGANIZATION_ID ->  "${%s}".format(Test.TEST_NAME),
      Form.PASSWORD ->  "${%s}".format(Test.TEST_ADDRESS),
      Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
  */
  val rejectVerifyOrganizationScenario: ScenarioBuilder = scenario("RejectVerifyOrganization")
    .feed(OrganizationIDFeeder.organizationIDFeed)

  /*
  .exec(http("RejectVerifyOrganization_GET")
    .get(routes.AddOrganizationController.rejectVerifyOrganizationRequestForm().url)
    .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
  .pause(2)
  .exec(http("RejectVerifyOrganization_POST")
    .post(routes.AddOrganizationController.rejectVerifyOrganizationRequest().url)
    .formParamMap(Map(
      Form.ORGANIZATION_ID ->  "${%s}".format(Test.TEST_ZONE_ID),
      Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
*/
  val blockchainAddOrganizationScenario: ScenarioBuilder = scenario("BlockchainAddOrganization")
    .feed(FromFeeder.fromFeed)
    .feed(ToFeeder.toFeed)
    .feed(OrganizationIDFeeder.organizationIDFeed)
    .feed(ZoneIDFeeder.zoneIDFeed)
    .feed(PasswordFeeder.passwordFeed)
    .exec(http("BlockchainAddOrganization_GET")
      .get(routes.AddOrganizationController.blockchainAddOrganizationForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainAddOrganization_POST")
      .post(routes.AddOrganizationController.blockchainAddOrganization().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)


}