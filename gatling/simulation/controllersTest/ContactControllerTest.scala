package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

object contactControllerTest {

  val addOrUpdateMobileNumberScenario: ScenarioBuilder = scenario("addOrUpdateMobileNumber")
    .feed(MobileNumberFeeder.mobileNumberFeed)
    .exec(http("AddOrUpdateMobileNumberForm_GET")
      .get(routes.ContactController.addOrUpdateMobileNumberForm().url)
      .check(css("legend:contains(%s)".format("Add Mobile Number")).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("AddOrUpdateMobileNumber_POST")
      .post(routes.ContactController.addOrUpdateMobileNumber().url)
      .formParamMap(Map(
        constants.FormField.COUNTRY_CODE.name -> "${%s}".format(Test.TEST_COUNTRY_CODE),
        constants.FormField.MOBILE_NUMBER.name -> "${%s}".format(Test.TEST_MOBILE_NUMBER),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN))
      )
      .check(substring("Mobile Number Updated").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val verifyMobileNumberScenario: ScenarioBuilder = scenario("VerifyMobileNumber")
    .feed(OTPFeeder.otpFeed)
    .exec(http("VerifyMobileNumberForm_GET")
      .get(routes.ContactController.verifyMobileNumberForm().url)
      .check(css("legend:contains(%s)".format("Verify Mobile Number")).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("VerifyMobileNumber_POST")
      .post(routes.ContactController.verifyMobileNumber().url)
      .formParamMap(Map(
        constants.FormField.OTP.name -> "${%s}".format(Test.TEST_OTP),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN))
      )
      .check(substring("Mobile Number verified successfully").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val addOrUpdateEmailAddressScenario: ScenarioBuilder = scenario("addOrUpdateEmailAddress")
    .feed(EmailAddressFeeder.emailAddressFeed)
    .exec(http("AddOrUpdateEmailAddressForm_GET")
      .get(routes.ContactController.addOrUpdateEmailAddressForm().url)
      .check(css("legend:contains(%s)".format("Add Email Address")).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("AddOrUpdateEmailAddress_POST")
      .post(routes.ContactController.addOrUpdateEmailAddress().url)
      .formParamMap(Map(
        constants.FormField.EMAIL_ADDRESS.name -> "${%s}".format(Test.TEST_EMAIL_ADDRESS),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN))
      )
      .check(substring("Email Address Updated").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val verifyEmailAddressScenario: ScenarioBuilder = scenario("verifyEmailAddress")
    .feed(OTPFeeder.otpFeed)
    .exec(http("VerifyEmailAddressForm_GET")
      .get(routes.ContactController.verifyEmailAddressForm().url)
      .check(css("legend:contains(%s)".format("Verify Email Address")).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("VerifyEmailAddress_POST")
      .post(routes.ContactController.verifyEmailAddress().url)
      .formParamMap(Map(
        constants.FormField.OTP.name -> "${%s}".format(Test.TEST_OTP),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN))
      )
      .check(substring("Email Address Verified").exists)
    )
    .pause(Test.REQUEST_DELAY)
}
