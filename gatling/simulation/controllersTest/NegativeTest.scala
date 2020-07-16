package controllersTest

import constants.{Form, Test}
import controllers.routes
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

object NegativeTest {

  val buyerConfirmBeforeTradeDocumentsUpload

  val sendFiatScenario: ScenarioBuilder = scenario("SendFiat")
    .exec(http("Send_Fiat_Form_GET")
      .get(session=>routes.SendFiatController.sendFiatForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Release Funds)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
      .check(css("[name=%s]".format(constants.FormField.SEND_AMOUNT.name), "value").saveAs(Test.TEST_SEND_AMOUNT))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Send_Fiat_POST")
      .post(routes.SendFiatController.sendFiat().url)
      .formParamMap(Map(
        constants.FormField.NEGOTIATION_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.SEND_AMOUNT.name -> "${%s}".format(Test.TEST_SEND_AMOUNT),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)
      ))
      .check(status.is(200))
      .check(substring("Funds Released").exists)
    )
    .pause(Test.REQUEST_DELAY)
}