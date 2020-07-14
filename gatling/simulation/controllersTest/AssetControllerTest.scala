package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

import scala.util.Random

object assetControllerTest {

  val moderatedIssueAssetRequestScenario: ScenarioBuilder = scenario("IssueAssetRequest")
    .feed(AssetDetailFeeder.assetDetailFeed)
    .feed(ShippingDetailsFeeder.shippingDetailsFeeder)
    .exec(http("ModeratedIssueAssetRequestForm_GET")
      .get(routes.AssetController.issueForm().url)
      .check(status.is(200))
      .check(css("legend:contains(Add Commodity)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("ModeratedIssueAssetRequest_POST")
      .post(routes.AssetController.issue().url)
      .formParamMap(Map(
        constants.FormField.ASSET_TYPE.name -> "${%s}".format(Test.TEST_ASSET_TYPE),
        constants.FormField.ASSET_DESCRIPTION.name -> "${%s}".format(Test.TEST_ASSET_DESCRIPTION),
        constants.FormField.ASSET_QUANTITY.name -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        constants.FormField.QUANTITY_UNIT.name -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
        constants.FormField.ASSET_PRICE_PER_UNIT.name -> "${%s}".format(Test.TEST_ASSET_PRICE_PER_UNIT),
        constants.FormField.SHIPPING_PERIOD.name -> "${%s}".format(Test.TEST_SHIPPING_PERIOD),
        constants.FormField.PORT_OF_LOADING.name -> "${%s}".format(Test.TEST_PORT_OF_LOADING),
        constants.FormField.PORT_OF_DISCHARGE.name -> "${%s}".format(Test.TEST_PORT_OF_DISCHARGE),
        constants.FormField.MODERATED.name -> true,
        constants.FormField.GAS.name -> "",
        constants.FormField.PASSWORD.name -> "",
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(206))
      .check(css("legend:contains(Create Sales Quote)").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val unmoderatedIssueAssetRequestScenario: ScenarioBuilder = scenario("IssueAssetRequest")
    .feed(AssetDetailFeeder.assetDetailFeed)
    .feed(GasFeeder.gasFeed)
    .feed(ShippingDetailsFeeder.shippingDetailsFeeder)
    .exec(http("UnmoderatedIssueAssetRequestForm_GET")
      .get(routes.AssetController.issueForm().url)
      .check(status.is(200))
      .check(css("legend:contains(Add Commodity)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("UnmoderatedIssueAssetRequest_POST")
      .post(routes.AssetController.issue().url)
      .formParamMap(Map(
        constants.FormField.ASSET_TYPE.name -> "${%s}".format(Test.TEST_ASSET_TYPE),
        constants.FormField.ASSET_DESCRIPTION.name -> "${%s}".format(Test.TEST_ASSET_DESCRIPTION),
        constants.FormField.ASSET_QUANTITY.name -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        constants.FormField.QUANTITY_UNIT.name -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
        constants.FormField.ASSET_PRICE_PER_UNIT.name -> "${%s}".format(Test.TEST_ASSET_PRICE_PER_UNIT),
        constants.FormField.SHIPPING_PERIOD.name -> "${%s}".format(Test.TEST_SHIPPING_PERIOD),
        constants.FormField.PORT_OF_LOADING.name -> "${%s}".format(Test.TEST_PORT_OF_LOADING),
        constants.FormField.PORT_OF_DISCHARGE.name -> "${%s}".format(Test.TEST_PORT_OF_DISCHARGE),
        constants.FormField.MODERATED.name -> false,
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(206))
      .check(css("legend:contains(Create Sales Quote)").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val sendAsset: ScenarioBuilder = scenario("SendAsset")
    .exec(http("SendAssetForm_GET")
      .get(session => routes.AssetController.sendForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Confirm Trade)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("SendAsset_POST")
      .post(routes.AssetController.send().url)
      .formParamMap(Map(
        constants.FormField.NEGOTIATION_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Trade Confirmed").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val releaseAsset: ScenarioBuilder = scenario("ReleaseAsset")
    .exec(http("ReleaseAssetForm_GET")
      .get(session => routes.AssetController.releaseForm(session(Test.TEST_ASSET_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Release Asset)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("ReleaseAsset_POST")
      .post(routes.AssetController.release().url)
      .formParamMap(Map(
        constants.FormField.ASSET_ID.name -> "${%s}".format(Test.TEST_ASSET_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Asset Released").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val redeemAsset: ScenarioBuilder = scenario("RedeemAsset")
    .exec(http("RedeemAssetForm_GET")
      .get(session => routes.AssetController.redeemForm(session(Test.TEST_ASSET_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Redeem Asset)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("RedeemAsset_POST")
      .post(routes.AssetController.redeem().url)
      .formParamMap(Map(
        constants.FormField.ASSET_ID.name -> "${%s}".format(Test.TEST_ASSET_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Trade Complete").exists)
    )


}
