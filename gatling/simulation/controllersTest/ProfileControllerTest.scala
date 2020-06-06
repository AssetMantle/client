/*
package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

class ProfileControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = profileControllerTest.addIdentification
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object profileControllerTest {




  def getAddressFromAccountID(accountID: String):String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://18.136.170.155:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "accountAddress" FROM master."Account" WHERE "id" = '$accountID'),'0') AS "accountAddress";""")
    sqlQueryFeeder.apply().next()("accountAddress").toString
  }
}
*/
