package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.changeBuyerBidControllerTest.getAddressFromAccountID
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class SendAssetControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = sendAssetControllerTest.sendAssetScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object sendAssetControllerTest {

  val sendAssetScenario: ScenarioBuilder = scenario("SendAsset")

    .feed(GasFeeder.gasFeed)
    .exec(http("Send_Asset_GET")
      .get(session=> routes.SendAssetController.sendAssetForm(session(Test.TEST_BUYER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.SEND_ASSET.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Send_Asset_POST")
      .post(routes.SendAssetController.sendAsset().url)
      .formParamMap(Map(
        Form.PASSWORD ->"${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS ASSET_SENT").exists)
    )
    .pause(3)

  val blockchainSendAssetScenario: ScenarioBuilder = scenario("BlockchainSendAsset")
    .feed(FromFeeder.fromFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(ToFeeder.toFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(ModeFeeder.modeFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("BlockchainSendAsset_GET")
      .get(routes.SendAssetController.blockchainSendAssetForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.BLOCKCHAIN_SEND_ASSET.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainSendAsset_POST")
      .post(routes.SendAssetController.blockchainSendAsset().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.MODE ->"${%s}".format(Test.TEST_MODE),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
  .check(substring("SUCCESS ASSET_SENT").exists)
    )
}