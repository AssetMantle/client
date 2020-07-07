package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

object sendFiatControllerTest {

  val sendFiatScenario: ScenarioBuilder = scenario("SendFiat")
    .exec(http("Send_Fiat_Form_GET")
      .get(session=>routes.SendFiatController.sendFiatForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(css("legend:contains(Release Funds)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Send_Fiat_POST")
      .post(routes.SendFiatController.sendFiat().url)
      .formParamMap(Map(
        constants.FormField.NEGOTIATION_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.SEND_AMOUNT.name -> "${%s}".format(Test.TEST_ASSET_PRICE),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)
      ))
      .check(substring("Funds Released").exists)
    )
    .pause(Test.REQUEST_DELAY)
}