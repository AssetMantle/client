package controllersTest

import constants.Test
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scenarios.IndexControllerTest


class IndexControllerTest extends Simulation {
  setUp(
    IndexControllerTest.searchScenario.inject(atOnceUsers(1)),
  ).protocols(http.baseUrl(Test.BASE_URL))
}
