package scenarios

import constants.Test
import controllers.routes
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._


object IndexControllerTest{
  val searchScenario: ScenarioBuilder = scenario("Search Scenario")
    .exec(http("Search_Scenario_GET")
      .get(routes.IndexController.search(query = "").url)
      .check(status.is(401))
    ).pause(Test.REQUEST_DELAY)
    .exec(http("Transaction_Hash_Scenario_GET")
      .get(routes.IndexController.search(query ="69CA8D2C93750175A22DA6FD58A59A34D4760B310BC8F6F20322FE6C5B58BFEF").url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)

}