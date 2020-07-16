package controllersTest

import constants.{Form, Test}
import controllers.routes
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

object TraderControllerTest {

  val traderRelationRequestScenario: ScenarioBuilder = scenario("TraderRelationRequest")
    .exec(http("TraderRelationRequestForm_GET")
      .get(routes.TraderController.traderRelationRequestForm().url)
      .check(status.is(200))
      .check(css("legend:contains(Add Counterparty)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("TraderRelationRequest_POST")
      .post(routes.TraderController.traderRelationRequest().url)
      .formParamMap(Map(
        constants.FormField.ACCOUNT_ID.name -> "${%s}".format(Test.TEST_COUNTER_PARTY_USERNAME),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Counterparty Request Sent").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val acceptTraderRelation: ScenarioBuilder = scenario("AcceptTraderRelation")
    .exec(http("AcceptOrRejectTraderRelationForm_GET")
      .get(session=>routes.TraderController.acceptOrRejectTraderRelationForm(session(Test.FROM_ID).as[String],session(Test.TO_ID).as[String]).url)
      .check(status.is(200))
      .check(css("button:contains(Approve)").exists)
      .check(css("button:contains(Reject)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("AcceptOrRejectTraderRelation_POST")
      .post(routes.TraderController.acceptOrRejectTraderRelation().url)
      .formParamMap(Map(
        constants.FormField.FROM_ID.name ->  "${%s}".format(Test.FROM_ID),
        constants.FormField.TO_ID.name ->  "${%s}".format(Test.TO_ID),
        constants.FormField.STATUS.name -> true,
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(206))
      .check(css("[id=%s]".format(constants.FormField.STATUS.name), "value").is("true"))
      .check(css("button:contains(Reject)").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val rejectTraderRelation: ScenarioBuilder = scenario("RejectTraderRelation")
    .exec(http("AcceptOrRejectTraderRelationForm_GET")
      .get(session=>routes.TraderController.acceptOrRejectTraderRelationForm(session(Test.FROM_ID).as[String],session(Test.TO_ID).as[String]).url)
      .check(status.is(200))
      .check(css("button:contains(Approve)").exists)
      .check(css("button:contains(Reject)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("AcceptOrRejectTraderRelation_POST")
      .post(routes.TraderController.traderRelationRequest().url)
      .formParamMap(Map(
        constants.FormField.FROM_ID.name ->  "${%s}".format(Test.FROM_ID),
        constants.FormField.TO_ID.name ->  "${%s}".format(Test.TO_ID),
        constants.FormField.STATUS.name -> false,
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(206))
      .check(css("[id=%s]".format(constants.FormField.STATUS.name), "value").is("false"))
      .check(css("button:contains(Approve)").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val organizationRejectTrader: ScenarioBuilder = scenario("OrganizationRejectTrader")
    .exec(http("OrganizationRejectTrader_Form_GET")
      .get(session=>routes.TraderController.organizationRejectRequestForm(session(Test.TEST_TRADER_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Reject Trader Request)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("OrganizationRejectTrader_POST")
      .post(routes.TraderController.organizationRejectRequest().url)
      .formParamMap(Map(
        constants.FormField.TRADER_ID.name -> "${%s}".format(Test.TEST_TRADER_ID),
        constants.FormField.COMMENT.name -> "",
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Trader Rejected").exists)
    )
}