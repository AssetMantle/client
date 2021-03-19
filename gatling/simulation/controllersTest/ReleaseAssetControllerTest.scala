/*
package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.changeBuyerBidControllerTest.getAddressFromAccountID
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class ReleaseAssetControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = releaseAssetControllerTest.releaseAssetScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object releaseAssetControllerTest {

  val releaseAssetScenario: ScenarioBuilder = scenario("ReleaseAsset")

    .exec(http("Release_Asset_Form_GET")
      .get(session=> routes.ReleaseAssetController.releaseAssetForm(session(Test.TEST_SELLER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.RELEASE_ASSET.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Release_Asset_POST")
      .post(routes.ReleaseAssetController.releaseAsset().url)
      .formParamMap(Map(
        Form.BLOCKCHAIN_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD ->  "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS ASSET_RELEASED").exists)
    )
    .pause(3)

  val blockchainReleaseAssetScenario: ScenarioBuilder = scenario("blockchainReleaseAsset")
    .feed(FromFeeder.fromFeed)
    .feed(ToFeeder.toFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(GasFeeder.gasFeed)
    .feed(ModeFeeder.modeFeed)
    .exec(http("BlockchainReleaseAsset_GET")
      .get(routes.ReleaseAssetController.blockchainReleaseAssetForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.BLOCKCHAIN_RELEASE_ASSET.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainReleaseAsset_POST")
      .post(routes.ReleaseAssetController.blockchainReleaseAsset().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.MODE ->"${%s}".format(Test.TEST_MODE),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS ASSET_RELEASED").exists)
    )
}
*/
