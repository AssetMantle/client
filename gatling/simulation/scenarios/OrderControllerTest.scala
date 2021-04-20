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
    .exec(AssetControllerTest.imageFeed)
    .exec(http("Fiat_Proof_Upload_FORM")
    .get(session => routes.FileController.uploadNegotiationForm(constants.File.Negotiation.FIAT_PROOF,session(Test.TEST_NEGOTIATION_ID).as[String]).url)
    .check(status.is(200))
    .check(css("button:contains(Browse)").exists)
    .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
  )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Fiat_Proof_Upload_")
      .post(session => routes.FileController.uploadNegotiation(constants.File.Negotiation.FIAT_PROOF).url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        Form.RESUMABLE_CHUNK_NUMBER -> "1",
        Form.RESUMABLE_CHUNK_SIZE -> "1048576",
        Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
        Form.RESUMABLE_IDENTIFIER -> "document",
        Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", Test.TEST_IMAGE_PATH + "${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm
      .check(status.is(200))
    )
    .exec(
      http("Store_Organization_KYC_" + "${%s}".format(Test.TEST_DOCUMENT_TYPE))
        .get(session => routes.FileController.storeNegotiation(session(Test.TEST_FILE_NAME).as[String], constants.File.Negotiation.FIAT_PROOF,session(Test.TEST_NEGOTIATION_ID).as[String]).url)
        .check(status.is(206))
        //.check(substring("Fiat P").exists)
    )
    .pause(Test.REQUEST_DELAY)
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
