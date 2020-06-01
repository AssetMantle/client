package controllersTest

import constants.{Form, Test}
import controllers.routes
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class TraderControllerTest extends Simulation {
  val scenarioBuilder: ScenarioBuilder =  traderControllerTest.traderRelationRequestScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object traderControllerTest {

  val traderRelationRequestScenario: ScenarioBuilder = scenario("TraderRelationRequest")
    .exec(http("TraderRelationRequestForm_GET")
      .get(routes.TraderController.traderRelationRequestForm().url)
      .check(substring("Add Counterparty").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("TraderRelationRequest_POST")
      .post(routes.TraderController.traderRelationRequest().url)
      .formParamMap(Map(
        constants.FormField.ACCOUNT_ID.name -> "${%s}".format(Test.TEST_COUNTER_PARTY_USERNAME),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
    )
    .pause(2)

  val acceptTraderRelation: ScenarioBuilder = scenario("AcceptTraderRelation")
    .exec(http("AcceptOrRejectTraderRelationForm_GET")
      .get(session=>routes.TraderController.acceptOrRejectTraderRelationForm(session(Test.FROM_ID).as[String],session(Test.TO_ID).as[String]).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("AcceptOrRejectTraderRelation_POST")
      .post(routes.TraderController.acceptOrRejectTraderRelation().url)
      .formParamMap(Map(
        constants.FormField.FROM_ID.name ->  "${%s}".format(Test.FROM_ID),
        constants.FormField.TO_ID.name ->  "${%s}".format(Test.TO_ID),
        constants.FormField.STATUS.name -> true,
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
    )
    .pause(2)

  val rejectTraderRelation: ScenarioBuilder = scenario("RejectTraderRelation")
    .exec(http("AcceptOrRejectTraderRelationForm_GET")
      .get(session=>routes.TraderController.acceptOrRejectTraderRelationForm(session(Test.FROM_ID).as[String],session(Test.TO_ID).as[String]).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("AcceptOrRejectTraderRelation_POST")
      .post(routes.TraderController.traderRelationRequest().url)
      .formParamMap(Map(
        constants.FormField.FROM_ID.name ->  "${%s}".format(Test.FROM_ID),
        constants.FormField.TO_ID.name ->  "${%s}".format(Test.TO_ID),
        constants.FormField.STATUS.name -> false,
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
    )
    .pause(2)


}