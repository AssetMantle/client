package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class SetACLControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = setACLControllerTest.setACLScenario
  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
}

object setACLControllerTest {

  val setACLScenario: ScenarioBuilder = scenario("SetACL")
    .feed(ToFeeder.toFeed)
    .feed(ACLAddressFeeder.aclAddressFeed)
    .feed(OrganizationIDFeeder.organizationIDFeed)
    .exec(http("SetACL_GET")
      .get(routes.SetACLController.setACLForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("SetACL_POST")
      .post(routes.SetACLController.setACL().url)
      .formParamMap(Map(
        Form.TO -> "${%s}".format(Test.TEST_PASSWORD),
        Form.ACL_ADDRESS -> "${%s}".format(Test.TEST_ACL_ADDRESS),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.ISSUE_ASSET -> "${%s}".format(Test.TEST_ISSUE_ASSET), //TODO: Figure out Boolean Treatment
        Form.ISSUE_ASSET -> "${%s}".format(Test.TEST_ISSUE_ASSET),
        Form.ISSUE_FIAT -> "${%s}".format(Test.TEST_ISSUE_FIAT),
        Form.SEND_ASSET -> "${%s}".format(Test.TEST_SEND_ASSET),
        Form.SEND_FIAT -> "${%s}".format(Test.TEST_SEND_FIAT),
        Form.REDEEM_ASSET -> "${%s}".format(Test.TEST_REDEEM_ASSET),
        Form.REDEEM_FIAT -> "${%s}".format(Test.TEST_REDEEM_FIAT),
        Form.SELLER_EXECUTE_ORDER -> "${%s}".format(Test.TEST_SELLER_EXECUTE_ORDER),
        Form.BUYER_EXECUTE_ORDER -> "${%s}".format(Test.TEST_BUYER_EXECUTE_ORDER),
        Form.CHANGE_BUYER_BID -> "${%s}".format(Test.TEST_CHANGE_BUYER_BID),
        Form.CHANGE_SELLER_BID -> "${%s}".format(Test.TEST_CHANGE_SELLER_BID),
        Form.CONFIRM_BUYER_BID -> "${%s}".format(Test.TEST_CONFIRM_BUYER_BID),
        Form.CONFIRM_SELLER_BID -> "${%s}".format(Test.TEST_CONFIRM_SELLER_BID),
        Form.NEGOTIATION -> "${%s}".format(Test.TEST_NEGOTIATION),
        Form.RELEASE_ASSET -> "${%s}".format(Test.TEST_RELEASE_ASSET),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

  val blockchainSetACLScenario: ScenarioBuilder = scenario("BlockchainSetACL")
    .feed(FromFeeder.fromFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(ACLAddressFeeder.aclAddressFeed)
    .feed(OrganizationIDFeeder.organizationIDFeed)
    .exec(http("BlockchainSetACL_GET")
      .get(routes.SetACLController.blockchainSetACLForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainSetACL_POST")
      .post(routes.SetACLController.blockchainSetACL().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.ACL_ADDRESS -> "${%s}".format(Test.TEST_ACL_ADDRESS),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.ISSUE_ASSET -> "${%s}".format(Test.TEST_ISSUE_ASSET),
        Form.ISSUE_FIAT -> "${%s}".format(Test.TEST_ISSUE_FIAT),
        Form.SEND_ASSET -> "${%s}".format(Test.TEST_SEND_ASSET),
        Form.SEND_FIAT -> "${%s}".format(Test.TEST_SEND_FIAT),
        Form.REDEEM_ASSET -> "${%s}".format(Test.TEST_REDEEM_ASSET),
        Form.REDEEM_FIAT -> "${%s}".format(Test.TEST_REDEEM_FIAT),
        Form.SELLER_EXECUTE_ORDER -> "${%s}".format(Test.TEST_SELLER_EXECUTE_ORDER),
        Form.BUYER_EXECUTE_ORDER -> "${%s}".format(Test.TEST_BUYER_EXECUTE_ORDER),
        Form.CHANGE_BUYER_BID -> "${%s}".format(Test.TEST_CHANGE_BUYER_BID),
        Form.CHANGE_SELLER_BID -> "${%s}".format(Test.TEST_CHANGE_SELLER_BID),
        Form.CONFIRM_BUYER_BID -> "${%s}".format(Test.TEST_CONFIRM_BUYER_BID),
        Form.CONFIRM_SELLER_BID -> "${%s}".format(Test.TEST_CONFIRM_SELLER_BID),
        Form.RELEASE_ASSET -> "${%s}".format(Test.TEST_RELEASE_ASSET),
        Form.NEGOTIATION -> "${%s}".format(Test.TEST_NEGOTIATION),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
}