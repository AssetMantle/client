package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

import scala.util.Random

class AssetControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = assetControllerTest.moderatedIssueAssetRequestScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object assetControllerTest {

  val moderatedIssueAssetRequestScenario: ScenarioBuilder = scenario("IssueAssetRequest")
    .feed(AssetDetailFeeder.assetDetailFeed)
    .feed(GasFeeder.gasFeed)
    .feed(ShippingDetailsFeeder.shippingDetailsFeeder)
    .exec(http("IssueAssetDetailForm_GET")
      .get(routes.AssetController.issueForm().url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("IssueAssetRequest_POST")
      .post(routes.AssetController.issue().url)
      .formParamMap(Map(
        constants.FormField.ASSET_TYPE.name -> "${%s}".format(Test.TEST_ASSET_TYPE),
        constants.FormField.ASSET_DESCRIPTION.name -> "${%s}".format(Test.TEST_ASSET_DESCRIPTION),
        constants.FormField.ASSET_QUANTITY.name -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        constants.FormField.QUANTITY_UNIT.name -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
        constants.FormField.ASSET_PRICE_PER_UNIT.name -> "${%s}".format(Test.TEST_ASSET_PRICE),
        constants.FormField.SHIPPING_PERIOD.name -> "${%s}".format(Test.TEST_SHIPPING_PERIOD),
        constants.FormField.PORT_OF_LOADING.name -> "${%s}".format(Test.TEST_PORT_OF_LOADING),
        constants.FormField.PORT_OF_DISCHARGE.name -> "${%s}".format(Test.TEST_PORT_OF_DISCHARGE),
        constants.FormField.MODERATED.name -> true,
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
    )
    .pause(3)

  val sendAsset: ScenarioBuilder = scenario("SendAsset")
    .exec(http("SendAssetForm_GET")
      .get(session => routes.AssetController.sendForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("SendAsset_POST")
      .post(routes.AssetController.send().url)
      .formParamMap(Map(
        constants.FormField.ORDER_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
    )

  val releaseAsset: ScenarioBuilder = scenario("ReleaseAsset")
    .exec(http("ReleaseAssetForm_GET")
      .get(session => routes.AssetController.releaseForm(session(Test.TEST_ASSET_ID).as[String]).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("ReleaseAsset_POST")
      .post(routes.AssetController.release().url)
      .formParamMap(Map(
        constants.FormField.ASSET_ID.name -> "${%s}".format(Test.TEST_ASSET_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
    )

  val redeemAsset: ScenarioBuilder = scenario("RedeemAsset")
    .exec(http("RedeemAssetForm_GET")
      .get(session => routes.AssetController.redeemForm(session(Test.TEST_ASSET_ID).as[String]).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("RedeemAsset_POST")
      .post(routes.AssetController.redeem().url)
      .formParamMap(Map(
        constants.FormField.ASSET_ID.name -> "${%s}".format(Test.TEST_ASSET_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
    )

  def getAssetID(query:String)={
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "id" FROM master."Asset" WHERE "ownerID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getRequestIDForIssueAsset(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "id" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }


  def getDocumentHashForIssueAsset(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "documentHash" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getAssetTypeForIssueAsset(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "assetType" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getAssetPriceForIssueAsset(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "assetPrice" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getQuantityUnitForIssueAsset(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "quantityUnit" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getAssetQuantityForIssueAsset(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "assetQuantity" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getPegHashByOwnerAddress(accountID: String): String = {
    println(accountID)
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "pegHash" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$accountID'),'0') AS "pegHash";""")
    sqlQueryFeeder.apply().next()("pegHash").toString
  }
}
