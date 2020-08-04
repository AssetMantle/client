package simulations

import constants.Test
import scenarios._
import feeders.JDBCFeeder._
import feeders._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class AddZone extends Simulation {

  setUp(
    CreateZone.createZone.inject(atOnceUsers(1))
  ).protocols(http.baseUrl(Test.BASE_URL))
}
