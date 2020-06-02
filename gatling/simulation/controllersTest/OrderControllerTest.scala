package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.changeBuyerBidControllerTest.getAddressFromAccountID
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

class OrderControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = orderControllerTest.moderatedBuyerExecuteOrderScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object orderControllerTest {

  val moderatedBuyerExecuteOrderScenario: ScenarioBuilder = scenario("ModeratedBuyerExecuteOrder")
    .exec(http("ModeratedBuyerExecuteOrderForm_GET")
      .get(session => routes.OrderController.moderatedBuyerExecuteForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("ModeratedBuyerExecuteOrder_POST")
      .post(routes.OrderController.moderatedBuyerExecute().url)
      .formParamMap(Map(
        constants.FormField.ORDER_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
    )
    .pause(4)

  val moderatedSellerExecuteOrderScenario: ScenarioBuilder = scenario("ModeratedSellerExecuteOrder")
    .exec(http("ModeratedSellerExecuteOrderForm_GET")
      .get(session => routes.OrderController.moderatedSellerExecuteForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("ModeratedSellerExecuteOrder_POST")
      .post(routes.OrderController.moderatedSellerExecute().url)
      .formParamMap(Map(
        constants.FormField.ORDER_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
    )
    .pause(4)

  def getOrderStatus(query:String)={
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "status" FROM master."Order" WHERE "id" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }
}
