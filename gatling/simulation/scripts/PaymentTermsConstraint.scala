package scripts

import java.time.LocalDate

import constants.Test
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

import scala.util.Random

class PaymentTermsConstraint extends Simulation {

  setUp(
    passwordMismatch.mismatchPasswordScenario.inject(atOnceUsers(1))
  ).protocols(http.baseUrl(Test.BASE_URL))
}

object InvalidPaymentTermsInput{

  val invalidAdvanceANdCreditInput: ScenarioBuilder = scenario("Invalid Payment Terms")
    .feed(PaymentTermsFeeder.paymentTermsFeed)
    .exec(http("Payment_Terms_Form_GET")
      .get(session => routes.NegotiationController.paymentTermsForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Payment Terms)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Invalid_PaymentTerms_POST")
      .post(routes.NegotiationController.paymentTerms().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.ADVANCE_PERCENTAGE.name ->100.0,
        Test.CREDIT_TENTATIVE_DATE ->"${%s}".format(Test.TEST_TENTATIVE_DATE),
        Test.CREDIT_TENURE -> "${%s}".format(Test.TEST_TENURE),
        Test.CREDIT_REFRENCE -> "${%s}".format(Test.TEST_REFRENCE),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(400))
      .check(substring("INVALID_PAYMENT_TERMS").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val bothTenureAndTentaiveInput: ScenarioBuilder = scenario("SignUp")
    .feed(PaymentTermsFeeder.paymentTermsFeed)
    .exec(http("Payment_Terms_Form_GET")
      .get(session => routes.NegotiationController.paymentTermsForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Payment Terms)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Invalid_PaymentTerms_POST")
      .post(routes.NegotiationController.paymentTerms().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.ADVANCE_PERCENTAGE.name ->"${%s}".format(Test.TEST_ADVANCE_PERCENTAGE),
        Test.CREDIT_TENTATIVE_DATE -> LocalDate.now().toString,
        Test.CREDIT_TENURE -> Random.alphanumeric.filter(_.isDigit).take(2).mkString,
        Test.CREDIT_REFRENCE -> "${%s}".format(Test.TEST_REFRENCE),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(400))
      .check(substring("TENURE_AND_TENTATIVE_DATE_BOTH_FOUND").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val refrenceMissing: ScenarioBuilder = scenario("refrenceMissing")
    .feed(PaymentTermsFeeder.paymentTermsFeed)
    .exec(http("Payment_Terms_Form_GET")
      .get(session => routes.NegotiationController.paymentTermsForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Payment Terms)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Invalid_PaymentTerms_POST")
      .post(routes.NegotiationController.paymentTerms().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.ADVANCE_PERCENTAGE.name ->"${%s}".format(Test.TEST_ADVANCE_PERCENTAGE),
        Test.CREDIT_TENTATIVE_DATE -> "",
        Test.CREDIT_TENURE -> Random.alphanumeric.filter(_.isDigit).take(2).mkString,
        Test.CREDIT_REFRENCE -> "",
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(400))
      .check(substring("REFRENCE_REQUIRED_WITH_TENURE").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val refrenceNotRequired= scenario("refrenceNotRequired")
    .feed(PaymentTermsFeeder.paymentTermsFeed)
    .exec(http("Payment_Terms_Form_GET")
      .get(session => routes.NegotiationController.paymentTermsForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Payment Terms)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Invalid_PaymentTerms_POST")
      .post(routes.NegotiationController.paymentTerms().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.ADVANCE_PERCENTAGE.name ->"${%s}".format(Test.TEST_ADVANCE_PERCENTAGE),
        Test.CREDIT_TENTATIVE_DATE -> LocalDate.now().toString,
        Test.CREDIT_TENURE -> "",
        Test.CREDIT_REFRENCE -> "SHIPPING_DATE",
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(400))
      .check(substring("REFRENCE_NOT_REQUIRED").exists)
    )
    .pause(Test.REQUEST_DELAY)
}
