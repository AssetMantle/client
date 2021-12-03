package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import scenarios.{IndexControllerTest, ViewControllerTest}


class IndexControllerTest extends Simulation {
  setUp(
    IndexControllerTest.searchScenario.inject(atOnceUsers(1)),
  ).protocols(http.baseUrl(Test.BASE_URL))
}
