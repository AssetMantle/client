package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import scenarios.ViewControllerTest


class ViewControllerTest extends Simulation {

  setUp(
    ViewControllerTest.blockScenario.inject(atOnceUsers(1)),
    ViewControllerTest.transactionsScenario.inject(atOnceUsers(1)),
    ViewControllerTest.proposalsScenario.inject(atOnceUsers(1)),
    ViewControllerTest.validatorsScenario.inject(atOnceUsers(1)),
  ).protocols(http.baseUrl(Test.BASE_URL))




}
