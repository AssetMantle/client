package scripts

import constants.Test
import controllers.routes
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class DocumentListConstraint extends Simulation {

  setUp(
    passwordMismatch.mismatchPasswordScenario.inject(atOnceUsers(1))
  ).protocols(http.baseUrl(Test.BASE_URL))
}

object documentListConstraint222{

}
