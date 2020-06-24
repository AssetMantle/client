package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.changeBuyerBidControllerTest.getAddressFromAccountID
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

class ChangeSellerBidControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = changeSellerBidControllerTest.blockchainChangeSellerBidScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object changeSellerBidControllerTest {

  val changeSellerBidScenario: ScenarioBuilder = scenario("ChangeSellerBid")
    .feed(SellerBidFeeder.sellerBidFeed)
    .feed(TimeFeeder.timeFeed)
    .exec(http("Change_Seller_Bid_GET")
      .get(session=>routes.ChangeSellerBidController.changeSellerBidForm(session(Test.TEST_BUYER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String]).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(css("[name=%s]".format(Form.REQUEST_ID), "value").saveAs(Form.REQUEST_ID))
    )
    .pause(2)
    .exec(http("Change_Seller_Bid_POST")
      .post(routes.ChangeSellerBidController.changeSellerBid().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.REQUEST_ID -> "${%s}".format(Form.REQUEST_ID),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.BID -> "${%s}".format(Test.TEST_SELLER_BID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD)))
      .check(substring("SUCCESS SELLER_BID_CHANGED").exists)
    )

  val blockchainChangeSellerBidScenario: ScenarioBuilder = scenario("BlockchainChangeSellerBid")
    .feed(FromFeeder.fromFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(ToFeeder.toFeed)
    .feed(SellerBidFeeder.sellerBidFeed)
    .feed(TimeFeeder.timeFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(ModeFeeder.modeFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("BlockchainChangeSellerBid_GET")
      .get(routes.ChangeSellerBidController.blockchainChangeSellerBidForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.BLOCKCHAIN_CHANGE_SELLER_BID.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainChangeSellerBid_POST")
      .post(routes.ChangeSellerBidController.blockchainChangeSellerBid().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.BID -> "${%s}".format(Test.TEST_SELLER_BID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.MODE ->"${%s}".format(Test.TEST_MODE),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS SELLER_BID_CHANGED").exists)
    )
}