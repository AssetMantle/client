package controllersTest

import java.time.LocalDate

import constants.{Form, Test}
import controllers.routes
import feeders.{AssetDetailFeeder, GasFeeder, PaymentTermsFeeder, ShippingDetailsFeeder}
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

import scala.util.Random

object ConstraintTest {

  object SignUp {
    val mismatchPasswordScenario: ScenarioBuilder = scenario("SignUp")
      .exec(http("SignUp_GET")
        .get(routes.AccountController.signUpForm().url)
        .check(status.is(200))
        .check(css("legend:contains(Register)").exists)
        .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
      )
      .pause(Test.REQUEST_DELAY)
      .exec(http("SignUp_POST")
        .post(routes.AccountController.signUp().url)
        .formParamMap(Map(
          constants.FormField.USERNAME.name -> "${%s}".format(Test.TEST_USERNAME),
          constants.FormField.USERNAME_AVAILABLE.name -> true,
          constants.FormField.SIGNUP_PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
          constants.FormField.SIGNUP_CONFIRM_PASSWORD.name -> ("${%s}".format(Test.TEST_PASSWORD) + "XYZ"),
          Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
        .check(status.is(400))
        .check(css("legend:contains(Passwords Do Not Match)").exists)
      )
      .pause(Test.REQUEST_DELAY)

    val usernameUnavailable: ScenarioBuilder = scenario("UsernameUnavailable")
      .exec(http("SignUp_GET")
        .get(routes.AccountController.signUpForm().url)
        .check(status.is(200))
        .check(css("legend:contains(Register)").exists)
        .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
      )
      .pause(Test.REQUEST_DELAY)
      .exec(http("SignUp_POST")
        .post(routes.AccountController.signUp().url)
        .formParamMap(Map(
          constants.FormField.USERNAME.name -> "${%s}".format(Test.TEST_USERNAME),
          constants.FormField.USERNAME_AVAILABLE.name -> true,
          constants.FormField.SIGNUP_PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
          constants.FormField.SIGNUP_CONFIRM_PASSWORD.name -> ("${%s}".format(Test.TEST_PASSWORD) + "XYZ"),
          Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
        .check(status.is(400))
        .check(css("legend:contains(UserName UnAvailable)").exists)
      )
      .pause(Test.REQUEST_DELAY)
  }

  object IssueAsset {

    val gasMissing: ScenarioBuilder = scenario("gasMissing")
      .feed(AssetDetailFeeder.assetDetailFeed)
      .feed(ShippingDetailsFeeder.shippingDetailsFeeder)
      .exec(http("UnmoderatedIssueAssetRequestForm_GET")
        .get(routes.AssetController.issueForm().url)
        .check(status.is(200))
        .check(css("legend:contains(Add Commodity)").exists)
        .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
      )
      .pause(Test.REQUEST_DELAY)
      .exec(http("UnmoderatedIssueAssetRequestWithoutGas_POST")
        .post(routes.AssetController.issue().url)
        .formParamMap(Map(
          constants.FormField.ASSET_TYPE.name -> "${%s}".format(Test.TEST_ASSET_TYPE),
          constants.FormField.ASSET_DESCRIPTION.name -> "${%s}".format(Test.TEST_ASSET_DESCRIPTION),
          constants.FormField.ASSET_QUANTITY.name -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
          constants.FormField.QUANTITY_UNIT.name -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
          constants.FormField.ASSET_PRICE_PER_UNIT.name -> "${%s}".format(Test.TEST_ASSET_PRICE_PER_UNIT),
          constants.FormField.SHIPPING_PERIOD.name -> "${%s}".format(Test.TEST_SHIPPING_PERIOD),
          constants.FormField.PORT_OF_LOADING.name -> "${%s}".format(Test.TEST_PORT_OF_LOADING),
          constants.FormField.PORT_OF_DISCHARGE.name -> "${%s}".format(Test.TEST_PORT_OF_DISCHARGE),
          constants.FormField.MODERATED.name -> false,
          constants.FormField.GAS.name -> "",
          constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
          Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
        .check(status.is(400))
        .check(substring("GAS_NOT_GIVEN").exists)
        .check(css("legend:contains(Add Commodity)").exists)
      )
      .pause(Test.REQUEST_DELAY)

    val passwordMissing: ScenarioBuilder = scenario("PasswordMissing")
      .feed(AssetDetailFeeder.assetDetailFeed)
      .feed(GasFeeder.gasFeed)
      .feed(ShippingDetailsFeeder.shippingDetailsFeeder)
      .exec(http("UnmoderatedIssueAssetRequestForm_GET")
        .get(routes.AssetController.issueForm().url)
        .check(status.is(200))
        .check(css("legend:contains(Add Commodity)").exists)
        .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
      )
      .pause(Test.REQUEST_DELAY)
      .exec(http("UnmoderatedIssueAssetRequestWithoutPassword_POST")
        .post(routes.AssetController.issue().url)
        .formParamMap(Map(
          constants.FormField.ASSET_TYPE.name -> "${%s}".format(Test.TEST_ASSET_TYPE),
          constants.FormField.ASSET_DESCRIPTION.name -> "${%s}".format(Test.TEST_ASSET_DESCRIPTION),
          constants.FormField.ASSET_QUANTITY.name -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
          constants.FormField.QUANTITY_UNIT.name -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
          constants.FormField.ASSET_PRICE_PER_UNIT.name -> "${%s}".format(Test.TEST_ASSET_PRICE_PER_UNIT),
          constants.FormField.SHIPPING_PERIOD.name -> "${%s}".format(Test.TEST_SHIPPING_PERIOD),
          constants.FormField.PORT_OF_LOADING.name -> "${%s}".format(Test.TEST_PORT_OF_LOADING),
          constants.FormField.PORT_OF_DISCHARGE.name -> "${%s}".format(Test.TEST_PORT_OF_DISCHARGE),
          constants.FormField.MODERATED.name -> false,
          constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
          constants.FormField.PASSWORD.name -> "",
          Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
        .check(status.is(400))
        .check(substring("PASSWORD_NOT_GIVEN").exists)
        .check(css("legend:contains(Add Commodity)").exists)
      )
      .pause(Test.REQUEST_DELAY)
  }


  object PaymentTerms{
    val invalidAdvanceAndCreditInput: ScenarioBuilder = scenario("Invalid Payment Terms")
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
          constants.FormField.ADVANCE_PERCENTAGE.name -> 100.0,
          Test.CREDIT_TENTATIVE_DATE -> "${%s}".format(Test.TEST_TENTATIVE_DATE),
          Test.CREDIT_TENURE -> "${%s}".format(Test.TEST_TENURE),
          Test.CREDIT_REFRENCE -> "${%s}".format(Test.TEST_REFRENCE),
          Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
        .check(status.is(400))
        .check(substring("INVALID_PAYMENT_TERMS").exists)
      )
      .pause(Test.REQUEST_DELAY)

    val bothTenureAndTentaiveInput: ScenarioBuilder = scenario("BothTenureAndTentaiveInput")
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
          constants.FormField.ADVANCE_PERCENTAGE.name -> "${%s}".format(Test.TEST_ADVANCE_PERCENTAGE),
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
          constants.FormField.ADVANCE_PERCENTAGE.name -> "${%s}".format(Test.TEST_ADVANCE_PERCENTAGE),
          Test.CREDIT_TENTATIVE_DATE -> "",
          Test.CREDIT_TENURE -> Random.alphanumeric.filter(_.isDigit).take(2).mkString,
          Test.CREDIT_REFRENCE -> "",
          Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
        .check(status.is(400))
        .check(substring("REFRENCE_REQUIRED_WITH_TENURE").exists)
      )
      .pause(Test.REQUEST_DELAY)

    val refrenceNotRequired = scenario("refrenceNotRequired")
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
          constants.FormField.ADVANCE_PERCENTAGE.name -> "${%s}".format(Test.TEST_ADVANCE_PERCENTAGE),
          Test.CREDIT_TENTATIVE_DATE -> LocalDate.now().toString,
          Test.CREDIT_TENURE -> "",
          Test.CREDIT_REFRENCE -> "SHIPPING_DATE",
          Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
        .check(status.is(400))
        .check(substring("REFRENCE_NOT_REQUIRED").exists)
      )
      .pause(Test.REQUEST_DELAY)
  }

  object DocumentListConstraint{


    val physicalDocumentsHandledViaRequired: ScenarioBuilder = scenario("SignUp")
      .exec(http("Document_List_Form_GET")
        .get(session => routes.NegotiationController.documentListForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
        .check(status.is(200))
        .check(css("legend:contains(Sales Quote Documents List)").exists)
        .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
      .pause(Test.REQUEST_DELAY)
      .exec(http("DocumentList_POST")
        .post(routes.NegotiationController.documentList().url)
        .formParamMap(Map(
          constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
          "DOCUMENT_LIST[0]" -> constants.File.Asset.BILL_OF_LADING,
          "DOCUMENT_LIST[1]" -> constants.File.Asset.COO,
          "DOCUMENT_LIST[2]" -> constants.File.Asset.COA,
          "DOCUMENT_LIST[3]" -> constants.File.Negotiation.BILL_OF_EXCHANGE,
          "DOCUMENT_LIST[4]" -> constants.File.Negotiation.INVOICE,
          constants.FormField.PHYSICAL_DOCUMENTS_HANDLED_VIA.name -> "",
          constants.FormField.DOCUMENT_LIST_COMPLETED.name -> true,
          Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
        .check(status.is(400))
        .check(substring("Bank Account Details Updated Successfully").exists)
        .check(css("legend:contains(Review Sales Quote)").exists)
        .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
      )
      .pause(Test.REQUEST_DELAY)
  }


}