package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.changeBuyerBidControllerTest.getAddressFromAccountID
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class BuyerExecuteOrderControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = buyerExecuteOrderControllerTest.moderatedBuyerExecuteOrderScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object buyerExecuteOrderControllerTest {

  val moderatedBuyerExecuteOrderScenario: ScenarioBuilder = scenario("ModeratedBuyerExecuteOrder")
    .exec(http("SendAssetForm_GET")
      .get(session => routes.OrderController.moderatedBuyerExecuteForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("SendAsset_POST")
      .post(routes.AssetController.send().url)
      .formParamMap(Map(
        constants.FormField.ORDER_ID.name -> "${%s}".format(Test.TEST_ORDER_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
    )
    .pause(4)
}
