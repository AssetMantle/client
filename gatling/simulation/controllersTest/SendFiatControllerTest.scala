package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.changeBuyerBidControllerTest.getAddressFromAccountID
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class SendFiatControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = sendFiatControllerTest.sendFiatScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object sendFiatControllerTest {

  val sendFiatScenario: ScenarioBuilder = scenario("SendFiat")
    .exec(http("Send_Fiat_Form_GET")
      .get(session=>routes.SendFiatController.sendFiatForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Send_Fiat_POST")
      .post(routes.SendFiatController.sendFiat().url)
      .formParamMap(Map(
        constants.FormField.NEGOTIATION_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.AMOUNT.name -> "${%s}".format(Test.TEST_ASSET_PRICE),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)
      ))
    )
}