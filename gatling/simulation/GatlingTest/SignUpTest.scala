package simulation.GatlingTest

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.util.Random

class SignUpTest extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:9000")

 val usernameFeeder = Iterator.continually(Map("someUsername" -> (Random.alphanumeric.take(8).mkString)))

  val scn = scenario("SignUp Scenario")
    .feed(usernameFeeder)
    .exec(
      http("requestGET")
        .get("/master/signUp")
        .check(css("[name=csrfToken]","value").saveAs("csrfToken")))
    .exec(
      http("requestPOST")
        .post("/master/signUp")
        .formParamMap(Map("USERNAME" -> "${someUsername}", "PASSWORD" -> Random.alphanumeric.take(8).mkString, "csrfToken" -> "${csrfToken}"))
    )
    .pause(5)

  setUp(
    scn.inject(atOnceUsers(10))
  ).protocols(httpProtocol)
}