package controllersTest

import constants.Test
import controllers.routes
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration._

class ActorPoc extends Simulation {

//  val httpProtocol = http
//    .baseUrl("http://localhost:9000") // Here is the root for all relative URLs
//    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
//    .acceptEncodingHeader("gzip, deflate")
//    .acceptLanguageHeader("en-US,en;q=0.5")
//    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")
//
//  val scn = scenario("Scenario Name") // A scenario is a chain of requests and pauses
//    .exec(http("request_1")
//      .get("/wallet/persistence1095fgex3h37zl4yjptnsd7qfmspesvav7xhgwt"))
//    .pause(7) // Note that Gatling has recorder real time pauses
//
//  setUp(scn.inject(atOnceUsers(200)).protocols(httpProtocol))


  val AcountScenario: ScenarioBuilder = scenario("Accounts Scenario")
    .exec(http("Account_Scenario_GET")
      .get(routes.ComponentViewController.accountWallet("persistence1n60ttuv05jv8cg8aa8e694f42a3l86d5qmkkxy").url)
      .check(
        css("div[id =ACCOUNT_WALLET_LOADING]").exists,
        status.is(200),
      )
    ).pause(6)

    setUp(AcountScenario.inject(atOnceUsers(2000))).protocols(http.baseUrl("http://localhost:9000"))

}