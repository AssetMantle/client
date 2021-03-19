/*

package controllersTest

import constants.{Form, Test}
import controllers.routes
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class FaucetRequestTest extends Simulation {

  val signUpToApproveFaucetRequest=scenario("signUpToApproveFaucetRequest")
    .exec(loginControllerTest.loginAfterSignUpScenario)
    .exec(sendCoinControllerTest.FaucetRequestScenario)
    .exec(sendCoinControllerTest.approveFaucetRequestScenario)

  val scenarioBuilder: ScenarioBuilder = signUpToApproveFaucetRequest
  setUp(scenarioBuilder.inject(rampUsers(10) during 20)).protocols(http.baseUrl(Test.BASE_URL))
}

object requestFaucetTest {

  val requestFaucetScenario: ScenarioBuilder = scenario("FaucetRequest")
    .exec(controllersTest.loginControllerTest.loginAfterSignUpScenario)
    .exec(http("RequestFaucet_GET")
      .get(routes.SendCoinController.faucetRequestForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("RequestFaucet_POST")
      .post(routes.SendCoinController.faucetRequest().url)
      .formParamMap(Map(
        Form.COUPON -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))

  val approveFaucetRequest: ScenarioBuilder = scenario("ApproveFaucetRequest")
    .exec(controllersTest.loginControllerTest.loginMain)
    .exec(http("RequestFaucet_GET")
      .get(routes.SendCoinController.approveFaucetRequestsForm("${%s}".format(Test.TEST_USERNAME),"").url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("RequestFaucet_POST")
      .post(routes.SendCoinController.faucetRequest().url)
      .formParamMap(Map(Form.COUPON -> "", Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))

}



*/
