package controllersTest

import constants.{Form, Test}
import controllers.routes
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class IndexControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = indexControllerTest.indexScenario
  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
}

object indexControllerTest {

  val indexScenario: ScenarioBuilder = scenario("Index")
   /* .exec(controllersTest.loginControllerTest.loginAfterSignUpScenario)
    .exec(http("Index_GET")
      .get(routes.IndexController.index().url))
    .pause(2)
*/
}
