package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

object RedeemFiatControllerTest {

  val redeemFiatScenario: ScenarioBuilder = scenario("RedeemFiat")
    .exec(http("Redeem_Fiat_Form_GET")
      .get(routes.RedeemFiatController.redeemFiatForm().url)
      .check(status.is(200))
      .check(css("legend:contains(Withdraw Funds)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Redeem_Fiat_POST")
      .post(routes.RedeemFiatController.redeemFiat().url)
      .formParamMap(Map(
        constants.FormField.REDEEM_AMOUNT.name -> "${%s}".format(Test.TEST_REDEEM_AMOUNT),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)
      ))
      .check(status.is(200))
      .check(substring("Withdrawal Request Sent").exists)
    )
    .pause(Test.REQUEST_DELAY)
}