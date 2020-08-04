package scenarios

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

object OrderControllerTest {

  val moderatedBuyerExecuteOrderScenario: ScenarioBuilder = scenario("ModeratedBuyerExecuteOrder")
    .exec(http("Moderated_Buyer_Execute_Order_Form_GET")
      .get(session => routes.OrderController.moderatedBuyerExecuteForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Moderated Buyer Execute Order)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Moderated_Buyer_Execute_Order_POST")
      .post(routes.OrderController.moderatedBuyerExecute().url)
      .formParamMap(Map(
        constants.FormField.ORDER_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Buyer Order Executed").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val moderatedSellerExecuteOrderScenario: ScenarioBuilder = scenario("ModeratedSellerExecuteOrder")
    .exec(http("Moderated_Seller_Execute_Order_Form_GET")
      .get(session => routes.OrderController.moderatedSellerExecuteForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Moderated Seller Execute Order)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Moderated_Seller_Execute_Order_POST")
      .post(routes.OrderController.moderatedSellerExecute().url)
      .formParamMap(Map(
        constants.FormField.ORDER_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Seller Order Executed").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val unmoderatedBuyerExecuteOrderScenario: ScenarioBuilder = scenario("UnmoderatedBuyerExecuteOrder")
    .feed(FiatProofHashFeeder.fiatProofHashFeed)
    .exec(http("Unmoderated_Buyer_Execute_Order_Form_GET")
      .get(session => routes.OrderController.buyerExecuteForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Execute Order)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Unmoderated_Buyer_Execute_Order_POST")
      .post(routes.OrderController.buyerExecute().url)
      .formParamMap(Map(
        constants.FormField.ORDER_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.FIAT_PROOF.name -> "${%s}".format(Test.TEST_FIAT_PROOF_HASH),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Buyer Order Executed").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val unmoderatedSellerExecuteOrderScenario: ScenarioBuilder = scenario("UnmoderatedSellerExecuteOrder")
    .exec(http("Unmoderated_Seller_Execute_Order_Form_GET")
      .get(session => routes.OrderController.sellerExecuteForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Execute Order)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Unmoderated_Seller_Execute_Order_POST")
      .post(routes.OrderController.sellerExecute().url)
      .formParamMap(Map(
        constants.FormField.ORDER_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Seller Order Executed").exists)
    )
    .pause(Test.REQUEST_DELAY)


}
