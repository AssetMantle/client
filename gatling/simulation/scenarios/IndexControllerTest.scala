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
    /*
    .exec(http("Transaction_Hash_Scenario_GET")
      .get(routes.IndexController.search(query ="69CA8D2C93750175A22DA6FD58A59A34D4760B310BC8F6F20322FE6C5B58BFEF").url)
      .check(bodyString.saveAs("BODY"))
      .check(status.is(303))
    ).pause(Test.REQUEST_DELAY)
    .exec(session => {
      // val response1 = session("BODY1").as[String]
      val response2 = session("BODY").as[String]
      // println(s"Response body: \n$response1")
      println(s"Response body: \n$response2")
      session
    })

     */
}