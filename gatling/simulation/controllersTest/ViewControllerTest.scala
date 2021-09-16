package controllersTest

import constants.Test
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scenarios.ViewControllerTest


class ViewControllerTest extends Simulation {
  val viewControllerTest = scenario("View Controller")
    .exec(ViewControllerTest.blockScenario)
    .exec(ViewControllerTest.transactionsScenario)
    .exec(ViewControllerTest.proposalsScenario)
    .exec(ViewControllerTest.validatorsScenario)

  setUp(
    viewControllerTest.inject(atOnceUsers(1)),
  ).protocols(http.baseUrl(Test.BASE_URL))
}
